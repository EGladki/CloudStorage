package com.gladkiei.cloudstorage.services;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.enums.Type;
import com.gladkiei.cloudstorage.exceptions.BadRequestException;
import com.gladkiei.cloudstorage.exceptions.FileAlreadyExistsException;
import com.gladkiei.cloudstorage.exceptions.InternalFileStorageException;
import com.gladkiei.cloudstorage.exceptions.NotFoundException;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.File;
import com.gladkiei.cloudstorage.models.Resource;
import io.minio.*;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Service
public class FileStorageMinioService implements FileStorageService {

    private final MinioClient minioClient;
    @Value("${minio.root-bucket}")
    private String rootBucket;

    public FileStorageMinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void createRootDirectory(UserResponseDto dto) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath(dto.getId()))
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
    }

    @Override
    public Directory createDirectory(String input, UserResponseDto userResponseDto) {
        validateDirectoryPath(input);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath(userResponseDto.getId()) + input)
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return extractDirectory(input);
    }

    public List<Resource> getContent(String path, UserResponseDto userResponseDto) {
        validateDirectoryPath(path);
        Iterable<Result<Item>> results;
        List<Resource> list = new ArrayList<>();
        String specificUserPath = specificUserPath(userResponseDto.getId());

        try {
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .prefix(specificUserPath + path)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    list.add(new Directory(path, extractFileName(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                } else {
                    list.add(new File(path, extractFileName(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }

        if (list.isEmpty()) {
            throw new NotFoundException("Directory not found");
        }
        return list;
    }

    @Override
    public List<Resource> getResource(String path, UserResponseDto userResponseDto) {
        validateFilePath(path);

        return getResourcesByPath(path, userResponseDto);
    }

    @Override
    public void delete(String path, UserResponseDto userResponseDto) {
        validateFilePath(path);
        String specificUserPath = specificUserPath(userResponseDto.getId());
        getResourcesByPath(path, userResponseDto);

        try {
            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + path)
                            .build());
        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
    }

    private List<Resource> getResourcesByPath(String path, UserResponseDto userResponseDto) {
        Iterable<Result<Item>> results;
        String specificUserPath = specificUserPath(userResponseDto.getId());
        List<Resource> list = new ArrayList<>();
        try {
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .prefix(specificUserPath + path)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir() && !item.objectName().equals(specificUserPath)) {
                    list.add(new File(path, extractFileName(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }

        if (list.isEmpty()) {
            throw new NotFoundException("Resource not found");
        }
        return list;
    }

    @Override
    public byte[] downloadFile(String path, UserResponseDto userResponseDto) {
        validateFilePath(path);
        getResourcesByPath(path, userResponseDto);
        String specificUserPath = specificUserPath(userResponseDto.getId());

        byte[] result;
        try {
            InputStream stream = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + path)
                            .build());

            result = stream.readAllBytes();
        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return result;
    }

    public byte[] downloadDirectoryAsZip(String path, UserResponseDto userResponseDto) throws IOException {
        validateDirectoryPath(path);
        List<Resource> resources = getResourcesByPath(path, userResponseDto);
        String specificUserPath = specificUserPath(userResponseDto.getId());

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipOutputStream zipOut = new ZipOutputStream(baos);

        for (Resource resource : resources) {
            String resourceName = resource.getName();
            try {
                InputStream inputStream = minioClient.getObject(
                        GetObjectArgs.builder()
                                .bucket(rootBucket)
                                .object(specificUserPath + path + resourceName)
                                .build());

                ZipEntry zipEntry = new ZipEntry(resourceName);
                zipOut.putNextEntry(zipEntry);
                inputStream.transferTo(zipOut);
                zipOut.closeEntry();

            } catch (Exception e) {
                throw new InternalFileStorageException();
            }
        }
        zipOut.close();
        return baos.toByteArray();
    }

    @Override
    public List<Resource> moveFile(String from, String to, UserResponseDto userResponseDto) {
        validateFilePath(from);
        validateFilePath(to);
        getResourcesByPath(from, userResponseDto);

        if (alreadyExist(to, userResponseDto)) {
            throw new FileAlreadyExistsException(to);
        }

        String specificUserPath = specificUserPath(userResponseDto.getId());

        try {
            InputStream is = minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + from)
                            .build()
            );

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            is.transferTo(baos);
            byte[] byteArray = baos.toByteArray();

            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + to)
                            .stream(new ByteArrayInputStream(byteArray), byteArray.length, -1)
                            .build()
            );

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + from)
                            .build()
            );

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }

        return getResourcesByPath(to, userResponseDto);
    }

    @Override
    public List<Resource> upload(String path, MultipartFile file, UserResponseDto userResponseDto) throws IOException {
        String specificUserPath = specificUserPath(userResponseDto.getId());
        byte[] bytes = file.getBytes();

        Iterable<Result<Item>> results;
        List<Resource> list = new ArrayList<>();

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + path + file.getOriginalFilename())
                            .stream(new ByteArrayInputStream(bytes), bytes.length, -1)
                            .contentType(file.getContentType())
                            .build()
            );

            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .prefix(specificUserPath + path + file.getOriginalFilename())
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    list.add(new Directory(path, extractFileName(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                } else {
                    list.add(new File(path, extractFileName(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return list;
    }

    private void validateDirectoryPath(String path) {
        if (path.isEmpty()) {
            return;
        }

        if (!path.endsWith("/") || path.trim().equals("/") || path.startsWith("/")) {
            throw new BadRequestException("Invalid path. Directory must ends with '/'");
        }
    }

    private void validateFilePath(String path) {
        if (path == null || path.isBlank() || path.endsWith("/")) {
            throw new BadRequestException("Invalid path");
        }
    }

    private Directory extractDirectory(String input) {
        int idx = input.lastIndexOf("/", input.length() - 2);
        String path = input.substring(0, idx + 1);
        String name = input.substring(idx + 1);

        return new Directory(path, name, Type.DIRECTORY);
    }

    private String extractFileName(String objectName, String specificUserPath, String path) {
        return objectName.substring(specificUserPath.length() + path.length());
    }

    private String specificUserPath(Long id) {
        return "user-" + id + "-files/";
    }

    private boolean alreadyExist(String path, UserResponseDto userResponseDto) {
        Iterable<Result<Item>> results;
        String specificUserPath = specificUserPath(userResponseDto.getId());
        List<Resource> list = new ArrayList<>();
        try {
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .prefix(specificUserPath + path)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (!item.isDir() && !item.objectName().equals(specificUserPath)) {
                    list.add(new File(path, extractFileName(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return !list.isEmpty();
    }
}

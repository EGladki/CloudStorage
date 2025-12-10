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
    public Directory createDirectory(String path, UserResponseDto userResponseDto) {
        validateCreationDirectoryPath(path);

        if (!isDirectoryExist(extractParentDirectory(path), userResponseDto)) {
            throw new NotFoundException("Parent directory doesn't exist");
        }

        if (isDirectoryExist(path, userResponseDto)) {
            throw new FileAlreadyExistsException("Such directory already exists");
        }

        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath(userResponseDto.getId()) + path)
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return extractDirectory(path);
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
                    list.add(new Directory(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                } else {
                    list.add(new File(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
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
        return getFiles(path, userResponseDto);
    }

    @Override
    public List<Resource> search(String path, UserResponseDto userResponseDto) {
        Iterable<Result<Item>> results;
        List<Resource> list = new ArrayList<>();
        String specificUserPath = specificUserPath(userResponseDto.getId());

        try {
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .prefix(specificUserPath + path)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    list.add(new Directory(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                } else {
                    list.add(new File(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
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
    public void delete(String path, UserResponseDto userResponseDto) {
        if (isDirectory(path)) {
            deleteDirectory(path, userResponseDto);
        } else {
            deleteFile(path, userResponseDto);
        }
    }

    public void deleteFile(String path, UserResponseDto userResponseDto) {
        validateFilePath(path);

        if (!isFileExist(path, userResponseDto)) {
            throw new NotFoundException("File '" + path + "' not found");
        }

        String specificUserPath = specificUserPath(userResponseDto.getId());
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

    public void deleteDirectory(String path, UserResponseDto userResponseDto) {
        validateCreationDirectoryPath(path);
        String specificUserPath = specificUserPath(userResponseDto.getId());

        if (!isDirectoryExist(path, userResponseDto)) {
            throw new NotFoundException("Directory '" + path + "' not found");
        }

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

    public List<Resource> getFiles(String path, UserResponseDto userResponseDto) {
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
                    String extractedPath = extractPathFromFileName(path);
                    list.add(new File(extractedPath, extractFileNameFromObject(item.objectName(), specificUserPath, extractedPath), item.size(), Type.FILE));
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

    public List<Resource> getDirectories(String path, UserResponseDto userResponseDto) {
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
                if (item.isDir() || (item.objectName().equals(specificUserPath + path) && item.size() == 0)) {
                    list.add(new Directory(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), Type.DIRECTORY));
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
    public byte[] download(String path, UserResponseDto userResponseDto) throws IOException {
        if (isDirectory(path)) {
            return downloadDirectoryAsZip(path, userResponseDto);
        } else {
            return downloadFile(path, userResponseDto);
        }
    }

    public byte[] downloadFile(String path, UserResponseDto userResponseDto) {
        validateFilePath(path);

        if (!isFileExist(path, userResponseDto)) {
            throw new NotFoundException("File '" + path + "' not found");
        }

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
        List<Resource> resources = getFiles(path, userResponseDto);
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
    public List<Resource> move(String from, String to, UserResponseDto userResponseDto) {
        if (isDirectory(from)) {
            return moveDirectory(from, to, userResponseDto);
        } else {
            return moveFile(from, to, userResponseDto);
        }
    }

    private List<Resource> moveFile(String from, String to, UserResponseDto userResponseDto) {
        validateFilePath(from);
        validateFilePath(to);

        if (!isFileExist(from, userResponseDto)) {
            throw new NotFoundException("File '" + from + "' not found");
        }

        if (isFileExist(to, userResponseDto)) {
            throw new FileAlreadyExistsException("File '" + to + "' already exists");
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
        return getFiles(to, userResponseDto);
    }

    private List<Resource> moveDirectory(String from, String to, UserResponseDto userResponseDto) {
        validateDirectoryPath(from);
        validateDirectoryPath(to);

        if (!isDirectoryExist(from, userResponseDto)) {
            throw new NotFoundException("Directory '" + from + "' not found");
        }

        if (isDirectoryExist(to, userResponseDto)) {
            throw new FileAlreadyExistsException("Directory '" + to + "' already exists");
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
                            .object(specificUserPath + from) //удалить вложенные файлы? todo
                            .build()
            );

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return getDirectories(to, userResponseDto);
    }

    @Override
    public List<Resource> upload(String path, MultipartFile file, UserResponseDto userResponseDto) throws IOException {
        String specificUserPath = specificUserPath(userResponseDto.getId());

        if (isFileExist(path + file.getOriginalFilename(), userResponseDto)) {
            throw new FileAlreadyExistsException("File '" + file.getOriginalFilename() + "' already exists");
        }

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
                    list.add(new Directory(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                } else {
                    list.add(new File(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return list;
    }



    private void validateCreationDirectoryPath(String path) {
        if (!path.endsWith("/") || path.trim().equals("/") || path.startsWith("/") || path.trim().isBlank() || path.matches(".*[:|<>*?\"].*")) {
            throw new BadRequestException("Invalid directory name");
        }
    }

    private void validateDirectoryPath(String path) {
        if (path.isEmpty()) {
            return;
        }

        if (!path.endsWith("/") || path.trim().equals("/") || path.startsWith("/") || path.matches(".*[:|<>*?\"].*")) {
            throw new BadRequestException("Invalid directory name");
        }
    }

    private void validateFilePath(String path) {
        if (path == null || path.isBlank() || path.matches(".*[:|<>*?\"/].*")) {
            throw new BadRequestException("Invalid file name");
        }
    }

    private Directory extractDirectory(String input) {
        int idx = input.lastIndexOf("/", input.length() - 2);
        String path = input.substring(0, idx + 1);
        String name = input.substring(idx + 1);

        return new Directory(path, name, Type.DIRECTORY);
    }

    private String extractPathFromFileName(String fileName) {
        int idx = fileName.lastIndexOf("/");
        if (idx == -1) {
            return "";
        }

        return fileName.substring(0, idx + 1);
    }

    private String extractFileNameFromObject(String objectName, String specificUserPath, String path) {
        return objectName.substring(specificUserPath.length() + path.length());
    }

    public static String extractParentDirectory(String path) {
        if (path == null || path.isEmpty()) return "";

        String normalized = path.endsWith("/") ? path.substring(0, path.length() - 1) : path;

        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash == -1) {
            return "";
        }

        return normalized.substring(0, lastSlash + 1);
    }

    private String specificUserPath(Long id) {
        return "user-" + id + "-files/";
    }

    private boolean isFileExist(String path, UserResponseDto userResponseDto) {
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
                    list.add(new File(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return !list.isEmpty();
    }

    private boolean isDirectoryExist(String path, UserResponseDto userResponseDto) {
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
                if (item.isDir() || item.objectName().equals(specificUserPath + path)) {
                    list.add(new Directory(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return !list.isEmpty();
    }

    private boolean isSystemFile(Item item, String specificUserPath, String path) {
        return !item.isDir() && extractFileNameFromObject(item.objectName(), specificUserPath, path).isBlank() && item.size() == 0;
    }

    private static boolean isDirectory(String path) {
        return path.endsWith("/") || path.isEmpty();
    }
}

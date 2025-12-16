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
                } else if (!item.isDir() && !isSystemFile(item, specificUserPath, path)) {
                    list.add(new File(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException();
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
        validateSearch(path);
        Iterable<Result<Item>> results;
        List<Resource> list = new ArrayList<>();
        String specificUserPath = specificUserPath(userResponseDto.getId());

        try {
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .startAfter(specificUserPath + path)
                            .prefix(specificUserPath + path)
                            .recursive(true)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir() || isSystemFile(item, specificUserPath, path)) {
                    String extractedPath = extractDirectoryNameFromObject(item.objectName());
                    String extractedName = extractDirectoryNameFromObject(item.objectName());

                    list.add(new Directory(extractedPath, extractedName, Type.DIRECTORY));
                } else {
                    String extractedPath = extractPathFromObject(item.objectName(), specificUserPath);
                    String extractedName = extractFileNameFromObject(item.objectName());

                    list.add(new File(extractedPath, extractedName, item.size(), Type.FILE));
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

        List<Resource> content = getContent(path, userResponseDto);
        for (Resource resource : content) {
            delete(resource.getPath() + resource.getName(), userResponseDto);
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
                if (!item.isDir() && !isDirectory(item.objectName())) {
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

    public List<Resource> getMovedDirectory(String path, UserResponseDto userResponseDto) {
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
                    list.add(new Directory(extractParentDirectory(path), extractDirectoryFromPath(path), Type.DIRECTORY));
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
        List<Resource> resources;

        try {
            resources = getFiles(path, userResponseDto);
        } catch (NotFoundException e) {
            throw new NotFoundException("Directory has no files to download");
        }

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

        if (!isDirectoryExist(extractParentDirectory(extractPathFromFileName(to)), userResponseDto)) {
            throw new NotFoundException("Directory '" + extractPathFromFileName(to) + "' not found");
        }

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
        validateDirectoryPathToMove(from);
        validateDirectoryPathToMove(to);
        validateMove(from, to);

        if (!isDirectoryExist(from, userResponseDto)) {
            throw new NotFoundException("Directory '" + from + "' not found");
        }

        if (isDirectoryExist(to, userResponseDto)) {
            throw new FileAlreadyExistsException("Directory '" + to + "' already exists");
        }

        List<Resource> content = getContent(from, userResponseDto);
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

            for (Resource resource : content) {
                move(from + resource.getName(), to + resource.getName(), userResponseDto);
            }

            minioClient.removeObject(
                    RemoveObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath + from)
                            .build()
            );

        } catch (Exception e) {
            throw new InternalFileStorageException();
        }
        return getMovedDirectory(to, userResponseDto);
    }

    @Override
    public List<Resource> upload(String path, MultipartFile[] files, UserResponseDto userResponseDto) throws IOException {
        validateDirectoryPath(path);

        if (!isDirectoryExist(path, userResponseDto)) {
            throw new NotFoundException("Directory '" + path + "' not found");
        }

        String specificUserPath = specificUserPath(userResponseDto.getId());
        Iterable<Result<Item>> results;
        List<Resource> list = new ArrayList<>();

        for (MultipartFile file : files) {
            if (isFileExist(path + file.getOriginalFilename(), userResponseDto)) {
                throw new FileAlreadyExistsException("File '" + file.getOriginalFilename() + "' already exists");
            }

            List<String> directories = extractDirectories(file.getOriginalFilename());

            for (String directory : directories) {
                if (!isDirectoryExist(path + directory, userResponseDto)) {
                    createDirectory(path + directory, userResponseDto);
                }
            }

            byte[] bytes = file.getBytes();

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
                    if (item.isDir() || (item.objectName().equals(specificUserPath + path) && item.size() == 0)) {
                        list.add(new Directory(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), Type.DIRECTORY));
                    } else {
                        list.add(new File(path, extractFileNameFromObject(item.objectName(), specificUserPath, path), item.size(), Type.FILE));
                    }
                }
            } catch (Exception e) {
                throw new InternalFileStorageException();
            }
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

    private void validateDirectoryPathToMove(String path) {
        if (path.isEmpty()) {
            throw new BadRequestException("Invalid directory name");
        }

        if (!path.endsWith("/") || path.trim().equals("/") || path.startsWith("/") || path.matches(".*[:|<>*?\"].*")) {
            throw new BadRequestException("Invalid directory name");
        }
    }

    private void validateFilePath(String path) {
        if (path == null || path.isBlank() || path.matches(".*[:|<>*?\"].*") || path.endsWith("/")) {
            throw new BadRequestException("Invalid file name");
        }
    }

    private void validateSearch(String path) {
        if (path == null || path.isBlank() || path.matches(".*[:|<>*?\"].*") || path.length() > 100) {
            throw new BadRequestException("Invalid search request");
        }
    }

    private void validateMove(String from, String to) {
        if (to.equals(from)) {
            throw new BadRequestException("Cannot move directory into itself");
        }

        if (to.startsWith(from)) {
            throw new BadRequestException(
                    "Cannot move directory '" + from + "' into its subdirectory '" + to + "'"
            );
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

    private String extractFileNameFromObject(String objectName) {
        int idx = objectName.lastIndexOf("/");

        return objectName.substring(idx + 1);
    }

    private String extractDirectoryNameFromObject(String objectName) {
        String normalized = objectName.endsWith("/") ? objectName.substring(0, objectName.length() - 1) : objectName;
        int idx = normalized.lastIndexOf("/");

        return objectName.substring(idx + 1);
    }

    private String extractPathFromObject(String objectName, String specificUserPath) {
        int idx = objectName.lastIndexOf("/");

        return objectName.substring(specificUserPath.length(), idx + 1);
    }

    private List<String> extractDirectories(String fileName) {
        List<String> result = new ArrayList<>();

        if (fileName == null || fileName.isBlank()) {
            return result;
        }

        int lastSlash = fileName.lastIndexOf('/');
        if (lastSlash <= 0) {
            return result;
        }

        String path = fileName.substring(0, lastSlash);
        String[] parts = path.split("/");

        StringBuilder current = new StringBuilder();
        for (String part : parts) {
            current.append(part).append("/");
            result.add(current.toString());
        }

        return result;
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

    public static String extractDirectoryFromPath(String path) {
        String normalized = path.substring(0, path.length() - 1);
        String parentPath = path.substring(0, normalized.lastIndexOf('/') + 1);

        return path.substring(parentPath.length());
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
                if (!item.isDir() && !isDirectory(item.objectName())) {
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

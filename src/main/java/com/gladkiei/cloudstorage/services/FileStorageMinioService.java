package com.gladkiei.cloudstorage.services;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.enums.Type;
import com.gladkiei.cloudstorage.exceptions.BadRequestException;
import com.gladkiei.cloudstorage.exceptions.InternalFileStorageException;
import com.gladkiei.cloudstorage.exceptions.NotFoundException;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.File;
import com.gladkiei.cloudstorage.models.Resource;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

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
            e.printStackTrace();
        }
    }

    @Override
    public Directory createDirectory(String input, UserResponseDto userResponseDto) {
        validate(input);
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(specificUserPath(userResponseDto.getId()) + input)
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return parse(input);
    }

    public List<Resource> getContent(String path, UserResponseDto userResponseDto) {
        validate(path);
        Iterable<Result<Item>> results;
        List<Resource> list = new ArrayList<>();

        try {
            results = minioClient.listObjects(
                    ListObjectsArgs.builder()
                            .bucket(rootBucket)
                            .prefix(specificUserPath(userResponseDto.getId()) + path)
                            .build());

            for (Result<Item> result : results) {
                Item item = result.get();
                if (item.isDir()) {
                    list.add(new Directory(path, item.objectName(), Type.DIRECTORY));
                } else {
                    list.add(new File(path, item.objectName(), item.size(), Type.FILE));
                }
            }

        } catch (Exception e) {
            throw new InternalFileStorageException("Unknown file storage exception");
        }

        if (list.isEmpty()) {
            throw new NotFoundException("Directory not found");
        }

        return list;
    }

    @Override
    public void upload() {

    }

    @Override
    public void download() {

    }

    private void validate(String path) {
        if (!path.endsWith("/") || path.trim().equals("/") || path.startsWith("/")) {
            throw new BadRequestException("Invalid path. Directory must ends with '/'");
        }
    }

    private Directory parse(String input) {
        int idx = input.lastIndexOf("/", input.length() - 2);
        String path = input.substring(0, idx + 1);
        String name = input.substring(idx + 1);

        return new Directory(path, name, Type.DIRECTORY);
    }

//    private String parseLastFileName(String name) {
//        "user-20-files/1/2/"
//    }

    private String specificUserPath(Long id) {
        return "user-" + id + "-files/";
    }
}

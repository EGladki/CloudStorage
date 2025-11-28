package com.gladkiei.cloudstorage.services;

import com.gladkiei.cloudstorage.exceptions.BadRequestException;
import io.minio.ListObjectsArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.Result;
import io.minio.messages.Item;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.util.List;

@Service
public class FileStorageMinioService implements FileStorageService {

    @Value("${minio.root-bucket}")
    private String rootBucket;

    private final MinioClient minioClient;

    public FileStorageMinioService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @Override
    public void createDirectory(String path) {
        validate(path);
//        Iterable<Result<Item>> allDirectories = getAllDirectories();
        int x = 5;
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(rootBucket)
                            .object(path)
                            .stream(
                                    new ByteArrayInputStream(new byte[]{}), 0, -1)
                            .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void upload() {

    }

    @Override
    public void download() {

    }

//    private Iterable<Result<Item>> getAllDirectories() {
//        Iterable<Result<Item>> results = minioClient.listObjects(
//                ListObjectsArgs.builder()
//                        .bucket("my-bucketname")
////                        .startAfter(rootBucket)
////                        .prefix("E")
////                        .maxKeys(100)
//                        .build());
//
//        return results;
//    }

    private void validate(String path) {
        if (!path.endsWith("/")) {
            throw new BadRequestException("Invalid path. Directory must ends with '/'");
        }
    }
}

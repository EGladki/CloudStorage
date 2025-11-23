package com.gladkiei.cloudstorage.services;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
public class MinioRootBucketInitializer {

    private final MinioClient minioClient;

    public MinioRootBucketInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    public void init() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket("user-files").build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket("user-files").build());
        } else {
            System.out.println("Bucket already exists");
        }

    }
}

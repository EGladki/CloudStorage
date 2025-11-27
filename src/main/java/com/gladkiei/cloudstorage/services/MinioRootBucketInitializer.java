package com.gladkiei.cloudstorage.services;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.errors.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@Service
@Slf4j
public class MinioRootBucketInitializer {

    private final MinioClient minioClient;
    @Value("${minio.root-bucket}")
    private String root ;

    @Autowired
    public MinioRootBucketInitializer(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostConstruct
    private void initRootBucket() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        boolean found = minioClient.bucketExists(BucketExistsArgs.builder().bucket(root).build());
        if (!found) {
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(root).build());
            log.info("Minio: root bucket ' {} ' created", root);
        } else {
            log.info("Minio: root bucket ' {} ' already exists", root);
        }

    }
}

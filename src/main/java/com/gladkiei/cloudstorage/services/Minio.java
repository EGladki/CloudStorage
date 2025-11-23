package com.gladkiei.cloudstorage.services;

import io.minio.MinioClient;

public class Minio {

        MinioClient minioClient = MinioClient
                .builder()
                .endpoint("http://127.0.0.1:9000/")
                .credentials("minioadmin", "minioadmin")
                .build();

}

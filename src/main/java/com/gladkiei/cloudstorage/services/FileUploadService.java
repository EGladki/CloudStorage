package com.gladkiei.cloudstorage.services;

import io.minio.MinioClient;
import org.springframework.stereotype.Service;

@Service
public class FileUploadService {
    private final MinioClient minioClient;

    public FileUploadService(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    void createBucket() {

    }

    void deleteBucket() {

    }



}

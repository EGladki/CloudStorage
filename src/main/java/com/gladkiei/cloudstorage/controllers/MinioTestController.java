package com.gladkiei.cloudstorage.controllers;

import io.minio.*;
import io.minio.errors.*;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

@RestController
@RequestMapping("/api")
public class MinioTestController {

    private final MinioClient minioClient;

    @Value("${minio.root-bucket}")
    private String root;

    public MinioTestController(MinioClient minioClient) {
        this.minioClient = minioClient;
    }

    @PostMapping("/upload-file")
    @Operation(summary = "test upload")
    public void upload() throws IOException, ServerException, InsufficientDataException, ErrorResponseException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        File file = new File("C:\\Dev\\data\\text.txt");
        StringBuilder builder = new StringBuilder();
        builder.append("From ");
        builder.append("builder");
        byte[] data = builder.toString().getBytes(StandardCharsets.UTF_8);

        minioClient.uploadObject(UploadObjectArgs.builder()
                .bucket("user-files")
                .object("text.txt")
                .filename("C:\\Dev\\data\\text.txt")
                .build());
    }

    @PostMapping("/delete-file")
    @Operation(summary = "test delete")
    public void delete() throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {

        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket("user-files")
                .object("text.txt")
                .build());
    }

    @PostMapping("/create-bucket")
    @Operation(summary = "create bucket")
    public void createBucket(@RequestBody String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.makeBucket(MakeBucketArgs.builder()
                .bucket(root + name)
                .build());

    }

    @PostMapping("/delete-bucket")
    @Operation(summary = "delete bucket")
    public void deleteBucket(@RequestBody String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.removeBucket(RemoveBucketArgs.builder()
                .bucket(root + name)
                .build());

    }

    @PostMapping("/put-object")
    @Operation(summary = "put object or create directory")
    public void putObject(@RequestBody String name) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("user-files")
                        .object(name)
                        .stream(
                                new ByteArrayInputStream(new byte[] {}), 0, -1)
                        .build());

    }

}

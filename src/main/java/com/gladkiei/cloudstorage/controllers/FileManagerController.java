package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.services.FileStorageMinioService;
import com.gladkiei.cloudstorage.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class FileManagerController {

//    private FileStorageMinioService fileStorageMinioService;
//
//    public FileManagerController(FileStorageMinioService fileStorageMinioService) {
//        this.fileStorageMinioService = fileStorageMinioService;
//    }

    private final FileStorageService fileStorageService;

    public FileManagerController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Create empty directory")
    @PostMapping("/directory")
    public void createEmptyDirectory(@RequestParam String path) {
        fileStorageService.createDirectory(path);

    }

}

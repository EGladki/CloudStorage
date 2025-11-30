package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.mapper.UserMapper;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.Resource;
import com.gladkiei.cloudstorage.security.UserDetailsImpl;
import com.gladkiei.cloudstorage.services.FileStorageService;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FileManagerController {

    @Value("${minio.root-bucket}")
    private String root;

    private final FileStorageService fileStorageService;

    public FileManagerController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/directory")
    @Operation(summary = "create directory")
    public ResponseEntity<?> createDirectory(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        Directory directory = fileStorageService.createDirectory(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }

    @GetMapping("/directory")
    @Operation(summary = "get info about directory content")
    public ResponseEntity<?> getContent(@RequestParam String path) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);

        List<Resource> content = fileStorageService.getContent(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }


}

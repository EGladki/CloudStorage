package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.mapper.UserMapper;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.Resource;
import com.gladkiei.cloudstorage.security.UserDetailsImpl;
import com.gladkiei.cloudstorage.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/resource")
    @Operation(summary = "get info about resource")
    public ResponseEntity<?> getResource(@RequestParam String path) {
        UserResponseDto userResponseDto = getUserDtoFromAuthentication();
        List<Resource> resources = fileStorageService.getResource(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(resources);
    }

    @DeleteMapping("/resource")
    @Operation(summary = "delete resource")
    public ResponseEntity<?> deleteResource(@RequestParam String path) {
        UserResponseDto userResponseDto = getUserDtoFromAuthentication();
        fileStorageService.delete(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/directory")
    @Operation(summary = "create directory")
    public ResponseEntity<?> createDirectory(@RequestParam String path) {
        UserResponseDto userResponseDto = getUserDtoFromAuthentication();
        Directory directory = fileStorageService.createDirectory(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }

    @GetMapping("/directory")
    @Operation(summary = "get info about directory content")
    public ResponseEntity<?> getContent(@RequestParam(defaultValue = "") String path) {
        UserResponseDto userResponseDto = getUserDtoFromAuthentication();
        List<Resource> content = fileStorageService.getContent(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }

    private UserResponseDto getUserDtoFromAuthentication() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl principal = (UserDetailsImpl) authentication.getPrincipal();
        return UserMapper.INSTANCE.principalToUserResponseDto(principal);
    }


}

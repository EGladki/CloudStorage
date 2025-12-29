package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.mapper.UserMapper;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.Resource;
import com.gladkiei.cloudstorage.security.UserDetailsImpl;
import com.gladkiei.cloudstorage.services.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api")
public class FileManagerController {

    private final FileStorageService fileStorageService;

    public FileManagerController(FileStorageService fileStorageService) {
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/directory")
    @Operation(summary = "create directory")
    public ResponseEntity<?> createDirectory(@RequestParam String path,
                                             @AuthenticationPrincipal UserDetailsImpl principal) {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        Directory directory = fileStorageService.createDirectory(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(directory);
    }

    @GetMapping("/directory")
    @Operation(summary = "get info about directory content")
    public ResponseEntity<?> getContent(@RequestParam(defaultValue = "") String path,
                                        @AuthenticationPrincipal UserDetailsImpl principal) {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        List<Resource> content = fileStorageService.getContent(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(content);
    }

    @GetMapping("/resource")
    @Operation(summary = "get info about resource")
    public ResponseEntity<?> getResource(@RequestParam String path,
                                         @AuthenticationPrincipal UserDetailsImpl principal) {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        List<Resource> resource = fileStorageService.getResource(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @DeleteMapping("/resource")
    @Operation(summary = "delete resource")
    public ResponseEntity<?> deleteResource(@RequestParam String path,
                                            @AuthenticationPrincipal UserDetailsImpl principal) {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        fileStorageService.delete(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping(value = "/resource/download")
    @Operation(summary = "download resource (single file or zip archive)")
    public ResponseEntity<byte[]> download(@RequestParam(defaultValue = "") String path,
                                           @AuthenticationPrincipal UserDetailsImpl principal) throws IOException {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        byte[] downloaded = fileStorageService.download(path, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.CONTENT_DISPOSITION, extractName(path))
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(downloaded);
    }

    @GetMapping("/resource/move")
    @Operation(summary = "rename / move resource")
    public ResponseEntity<?> move(@RequestParam(defaultValue = "") String from,
                                  @RequestParam String to,
                                  @AuthenticationPrincipal UserDetailsImpl principal) {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        List<Resource> resource = fileStorageService.move(from, to, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    @PostMapping(value = "/resource", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(summary = "upload resource")
    public ResponseEntity<?> upload(@RequestParam(defaultValue = "") String path,
                                    @RequestParam("object") MultipartFile[] files,
                                    @AuthenticationPrincipal UserDetailsImpl principal) throws IOException {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        List<Resource> uploaded = fileStorageService.upload(path, files, userResponseDto);

        return ResponseEntity.status(HttpStatus.CREATED).body(uploaded);
    }

    @GetMapping(value = "/resource/search")
    @Operation(summary = "search")
    public ResponseEntity<?> search(@RequestParam String query,
                                    @AuthenticationPrincipal UserDetailsImpl principal) {
        UserResponseDto userResponseDto = UserMapper.INSTANCE.principalToUserResponseDto(principal);
        List<Resource> resource = fileStorageService.search(query, userResponseDto);

        return ResponseEntity.status(HttpStatus.OK).body(resource);
    }

    private String extractName(String path) {
        if (isDirectory(path)) {
            return extractDirectoryName(path);
        } else {
            return extractFileName(path);
        }
    }

    private String extractFileName(String path) {
        int idx = path.lastIndexOf("/");
        String name = path.substring(idx + 1);

        return "attachment; filename=\"" + name + "\"";
    }

    private String extractDirectoryName(String path) {
        if (path.isEmpty()) {
            return "attachment; filename=\"" + "root" + ".zip\"";
        }
        int idx = path.lastIndexOf("/", path.length() - 2);
        String name = path.substring(idx + 1, path.length() - 1);

        return "attachment; filename=\"" + name + ".zip\"";
    }

    private static boolean isDirectory(String path) {
        return path.endsWith("/") || path.isEmpty();
    }
}

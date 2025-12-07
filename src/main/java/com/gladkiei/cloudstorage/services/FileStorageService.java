package com.gladkiei.cloudstorage.services;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

public interface FileStorageService {

    Directory createDirectory(String path, UserResponseDto userResponseDto);

    void createRootDirectory(UserResponseDto userResponseDto);

    List<Resource> getContent(String path, UserResponseDto userResponseDto);

    List<Resource> getResource(String path, UserResponseDto userResponseDto);

    void delete(String path, UserResponseDto userResponseDto);

    byte[] downloadFile(String path, UserResponseDto userResponseDto);

    byte[] downloadDirectoryAsZip(String path, UserResponseDto userResponseDto) throws IOException;

    List<Resource> upload(String path, MultipartFile file, UserResponseDto userResponseDto) throws IOException;

    List<Resource> move(String from, String to, UserResponseDto userResponseDto);
}

package com.gladkiei.cloudstorage.services;

import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.models.Directory;
import com.gladkiei.cloudstorage.models.Resource;

import java.util.List;
import java.util.Map;

public interface FileStorageService {

    Directory createDirectory(String path, UserResponseDto userResponseDto);

    void createRootDirectory(UserResponseDto dto);

    List<Resource> getContent(String path, UserResponseDto userResponseDto);

    List<Resource> getResource(String path, UserResponseDto userResponseDto);

    void delete(String path, UserResponseDto userResponseDto);

    void upload();

    void download();
}

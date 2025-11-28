package com.gladkiei.cloudstorage.services;

public interface FileStorageService {

    void createDirectory(String path);

    void upload();

    void download();

}

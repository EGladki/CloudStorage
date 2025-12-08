package com.gladkiei.cloudstorage.exceptions;

import org.springframework.http.HttpStatus;

public class FileAlreadyExistsException extends CustomException {
    public FileAlreadyExistsException(String fileName) {
        super("File '" + fileName + "' already exists" , HttpStatus.CONFLICT);
    }
}

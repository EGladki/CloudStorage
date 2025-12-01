package com.gladkiei.cloudstorage.exceptions;

import org.springframework.http.HttpStatus;

public class InternalFileStorageException extends CustomException {

    public InternalFileStorageException(String message) {
        super(message, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

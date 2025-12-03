package com.gladkiei.cloudstorage.exceptions;

import org.springframework.http.HttpStatus;

public class InternalFileStorageException extends CustomException {

    public InternalFileStorageException() {
        super("Unknown file storage exception", HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

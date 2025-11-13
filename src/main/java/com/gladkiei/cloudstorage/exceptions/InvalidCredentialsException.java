package com.gladkiei.cloudstorage.exceptions;

import org.springframework.http.HttpStatus;

public class InvalidCredentialsException extends CustomException {
    public InvalidCredentialsException() {
        super("Invalid credentials", HttpStatus.UNAUTHORIZED);
    }
}

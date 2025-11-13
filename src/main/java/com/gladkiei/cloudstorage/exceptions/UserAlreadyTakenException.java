package com.gladkiei.cloudstorage.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyTakenException extends CustomException {

    public UserAlreadyTakenException(String username) {
        super("Username '" + username + "' already taken" , HttpStatus.CONFLICT);
    }
}

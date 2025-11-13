package com.gladkiei.cloudstorage.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.Map;

@Controller
@RequestMapping("/api")
public class HelloController {

    @GetMapping("/hello")
    public ResponseEntity<?> hello() {
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", "Hello"));
    }

//    @GetMapping("/exception")
//    public CustomException throwException() {
//        throw new CustomException("Custom exception thrown");
//    }
}

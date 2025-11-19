package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import com.gladkiei.cloudstorage.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/sign-in")
    public ResponseEntity<?> processLogin(@Valid @RequestBody RegisterRequestDto dto, HttpServletRequest request, HttpServletResponse response) {
        authService.authenticate(dto, request, response);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("username", dto.getUsername()));
    }

    @PostMapping("/sign-up")
    public ResponseEntity<?> processRegistration(@Valid @RequestBody RegisterRequestDto dto) {
        authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", dto.getUsername()));
    }

    @PostMapping("/sign-out")
    public HttpStatus logout(HttpServletRequest request, HttpServletResponse response) {
        authService.logout(request,response);

        return HttpStatus.NO_CONTENT;
    }


}

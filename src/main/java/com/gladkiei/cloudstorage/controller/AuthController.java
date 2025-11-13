package com.gladkiei.cloudstorage.controller;

import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import com.gladkiei.cloudstorage.security.UserDetailsImpl;
import com.gladkiei.cloudstorage.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/showUserDetails")
    public ResponseEntity<?> showUserDetails() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

        return ResponseEntity.status(HttpStatus.OK).body(Map.of("message", userDetails.getUser()));
    }

}

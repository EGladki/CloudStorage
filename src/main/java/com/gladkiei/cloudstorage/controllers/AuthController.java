package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.services.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @Operation(summary = "Login using existing credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<?> processLogin(@Valid @RequestBody RegisterRequestDto dto, HttpServletRequest request, HttpServletResponse response) {
        authService.authenticate(dto, request, response);
        return ResponseEntity.status(HttpStatus.OK).body(Map.of("username", dto.getUsername()));
    }

    @Operation(summary = "New user registration", description = "Create new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "409", description = "Username already taken")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> processRegistration(@Valid @RequestBody RegisterRequestDto dto) {
        UserResponseDto responseDto = authService.register(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(Map.of("username", responseDto.getUsername()));
    }

    @Operation(summary = "Logout")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "No content, logout successfully"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    @PostMapping("/sign-out")
    public ResponseEntity<?> logout(HttpServletRequest request, HttpServletResponse response) {
        return authService.logout(request, response);
    }

}

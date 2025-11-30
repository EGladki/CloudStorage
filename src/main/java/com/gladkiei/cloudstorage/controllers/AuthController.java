package com.gladkiei.cloudstorage.controllers;

import com.gladkiei.cloudstorage.dto.AuthRequestDto;
import com.gladkiei.cloudstorage.dto.AuthResponseDto;
import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.mapper.DtoMapper;
import com.gladkiei.cloudstorage.services.AuthService;
import com.gladkiei.cloudstorage.services.FileStorageService;
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
    private final FileStorageService fileStorageService;

    public AuthController(AuthService authService, FileStorageService fileStorageService) {
        this.authService = authService;
        this.fileStorageService = fileStorageService;
    }

    @Operation(summary = "Login using existing credentials")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successfully"),
            @ApiResponse(responseCode = "401", description = "Bad credentials")
    })
    @PostMapping("/sign-in")
    public ResponseEntity<?> processLogin(@Valid @RequestBody AuthRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        AuthResponseDto responseDto = authService.authenticate(requestDto, request, response);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "New user registration", description = "Create new user")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Created"),
            @ApiResponse(responseCode = "409", description = "Username already taken")
    })
    @PostMapping("/sign-up")
    public ResponseEntity<?> processRegistration(@Valid @RequestBody AuthRequestDto requestDto) {
        UserResponseDto userResponseDto = authService.register(requestDto);
        fileStorageService.createRootDirectory(userResponseDto);

        AuthResponseDto responseDto = DtoMapper.INSTANCE.requestDtoToResponseDto(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
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

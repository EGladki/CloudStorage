package com.gladkiei.cloudstorage.services;

import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.exceptions.InvalidCredentialsException;
import com.gladkiei.cloudstorage.exceptions.UserAlreadyTakenException;
import com.gladkiei.cloudstorage.mapper.UserMapper;
import com.gladkiei.cloudstorage.models.User;
import com.gladkiei.cloudstorage.repositories.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder encoder;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public AuthService(UserRepository userRepository, AuthenticationManager authenticationManager, PasswordEncoder encoder) {
        this.userRepository = userRepository;
        this.authenticationManager = authenticationManager;
        this.encoder = encoder;
    }

    public void authenticate(RegisterRequestDto requestDto, HttpServletRequest request, HttpServletResponse response) {
        Authentication authRequest = new UsernamePasswordAuthenticationToken(
                requestDto.getUsername(),
                requestDto.getPassword()
        );
        Authentication authentication;

        try {
            authentication = authenticationManager.authenticate(authRequest);
        } catch (BadCredentialsException e) {
            throw new InvalidCredentialsException();
        }

        SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
        securityContext.setAuthentication(authentication);
        securityContextRepository.saveContext(securityContext, request, response);
        SecurityContextHolder.setContext(securityContext);
    }

    public UserResponseDto register(RegisterRequestDto requestDto) {
        if (userRepository.existsByUsername(requestDto.getUsername())) {
            throw new UserAlreadyTakenException(requestDto.getUsername());
        }
        User user = UserMapper.INSTANCE.registerRequesDtoToUser(requestDto);
        user.setPassword(encoder.encode(requestDto.getPassword()));
        return UserMapper.INSTANCE.userToUserResponseDto(userRepository.save(user));
    }

}

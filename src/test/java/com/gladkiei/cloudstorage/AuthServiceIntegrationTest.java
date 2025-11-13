package com.gladkiei.cloudstorage;

import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import com.gladkiei.cloudstorage.dto.UserResponseDto;
import com.gladkiei.cloudstorage.exceptions.UserAlreadyTakenException;
import com.gladkiei.cloudstorage.models.User;
import com.gladkiei.cloudstorage.repositories.UserRepository;
import com.gladkiei.cloudstorage.services.AuthService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
public class AuthServiceIntegrationTest extends AbstractIntegrationTest {
    private static final String NAME = "TestName";
    private static final String PASSWORD = "123456";

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private AuthService authService;

    @Test
    void shouldRegisterNewUser() {
        RegisterRequestDto requestDto = new RegisterRequestDto(NAME, PASSWORD);
        UserResponseDto responseDto = authService.register(requestDto);

        assertTrue(userRepository.existsByUsername(NAME));

        User user = userRepository.findByUsername(NAME).get();
        assertEquals(NAME, responseDto.getUsername());
        assertEquals(NAME, user.getUsername());
        assertNotEquals(PASSWORD, user.getPassword());
    }

    @Test
    void shouldThrowExceptionWhenUsernameTaken() {
        RegisterRequestDto requestDto = new RegisterRequestDto(NAME, PASSWORD);
        authService.register(requestDto);

        UserAlreadyTakenException exception = assertThrows(UserAlreadyTakenException.class, () -> authService.register(requestDto));
        assertEquals(UserAlreadyTakenException.class, exception.getClass());
    }

}

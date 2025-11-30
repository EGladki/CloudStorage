package com.gladkiei.cloudstorage;

import com.gladkiei.cloudstorage.dto.AuthRequestDto;
import com.gladkiei.cloudstorage.exceptions.InvalidCredentialsException;
import com.gladkiei.cloudstorage.exceptions.UserAlreadyTakenException;
import com.gladkiei.cloudstorage.models.User;
import com.gladkiei.cloudstorage.repositories.UserRepository;
import com.gladkiei.cloudstorage.services.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder encoder;

    @Mock
    private SecurityContextRepository securityContextRepository;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void givenValidRequest_whenAuthenticate_thenSuccess() {
        AuthRequestDto validRequestDto = createRegisterRequestDto();
        Authentication authenticated = new UsernamePasswordAuthenticationToken(validRequestDto.getUsername(), null, List.of());
        when(authenticationManager.authenticate(any(Authentication.class))).thenReturn(authenticated);

        assertThatNoException().isThrownBy(() -> authService.authenticate(validRequestDto, request, response));
        verify(authenticationManager, times(1)).authenticate(any());
        verify(securityContextRepository, times(1)).saveContext(any(), eq(request), eq(response));
    }

    @Test
    void givenInvalidRequest_whenAuthenticate_thenInvalidCredentialsException() {
        AuthRequestDto invalidRequestDto = createRegisterRequestDto();
        when(authenticationManager.authenticate(any(Authentication.class))).thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> authService.authenticate(invalidRequestDto, request, response))
                .isInstanceOf(InvalidCredentialsException.class);

        verify(authenticationManager, times(1)).authenticate(any());
        verify(securityContextRepository, times(0)).saveContext(any(), any(), any());
    }

    @Test
    void givenValidRequest_whenRegister_thanSuccess() {
        AuthRequestDto validRequestDto = createRegisterRequestDto();
        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);

        when(userRepository.existsByUsername(validRequestDto.getUsername())).thenReturn(false);
        when(encoder.encode(validRequestDto.getPassword())).thenReturn("Encoded password");

        assertThatNoException().isThrownBy(() -> authService.register(validRequestDto));
        verify(userRepository, times(1)).save(captor.capture());
        verify(encoder, times(1)).encode(validRequestDto.getPassword());

        User saved = captor.getValue();
        assertThat(saved.getUsername()).isEqualTo(validRequestDto.getUsername());
        assertThat(saved.getPassword()).isEqualTo("Encoded password");
    }

    @Test
    void givenInvalidRequest_whenRegisterWithAlreadyTakenName_thanUserAlreadyTakenException() {
        AuthRequestDto invalidRequestDto = createRegisterRequestDto();

        when(userRepository.existsByUsername(invalidRequestDto.getUsername())).thenReturn(true);

        assertThatThrownBy(() -> authService.register(invalidRequestDto))
                .isInstanceOf(UserAlreadyTakenException.class);

        verify(userRepository, times(0)).save(any());
        verify(encoder, times(0)).encode(invalidRequestDto.getPassword());
    }

    private AuthRequestDto createRegisterRequestDto() {
        return AuthRequestDto.builder()
                .username("username")
                .password("password")
                .build();
    }

}

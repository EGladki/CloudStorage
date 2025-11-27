package com.gladkiei.cloudstorage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gladkiei.cloudstorage.dto.RegisterRequestDto;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.session.web.http.CookieHttpSessionIdResolver;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
//@PropertySource("classpath:application-test.properties")
public class AuthIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CookieHttpSessionIdResolver sessionIdResolver;

    @Autowired
    private StringRedisTemplate redis;

    @Autowired
    private MockMvc mockMvc;

    @Value("${redis.data.key-prefix}")
    private String redisKeyPrefix;


    @Test
    void givenValidCredentials_whenRegister_then201() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.username").value(dto.getUsername()));
    }

    @Test
    void givenUsernameIsBlank_whenRegister_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").doesNotExist());
    }

    @Test
    void givenPasswordIsBlank_whenRegister_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.username").doesNotExist());
    }

    @Test
    void givenUsernameIsOneLetter_whenRegister_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("a", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").doesNotExist());
    }

    @Test
    void givenPasswordIsOneSymbol_whenRegister_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "1");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.username").doesNotExist());
    }

    @Test
    void givenAlreadyTakenCredentials_whenRegister_then409() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").exists());
    }

    @Test
    void givenValidCredentials_whenLogin_then200() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value(dto.getUsername()));
    }

    @Test
    void givenUsernameIsBlank_whenLogin_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("", "pass");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").doesNotExist());
    }

    @Test
    void givenPasswordIsBlank_whenLogin_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.username").doesNotExist());
    }

    @Test
    void givenUsernameIsOneLetter_whenLogin_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("a", "pass");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.username").exists())
                .andExpect(jsonPath("$.errors.password").doesNotExist());
    }

    @Test
    void givenPasswordIsOneSymbol_whenLogin_then400() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "1");

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors.password").exists())
                .andExpect(jsonPath("$.errors.username").doesNotExist());
    }

    @Test
    void givenInvalidCredentials_whenLogin_then401() throws Exception {
        RegisterRequestDto validDto = new RegisterRequestDto("name", "pass");
        RegisterRequestDto invalidDto = new RegisterRequestDto("testName", "testPass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validDto)))
                .andExpect(status().isCreated());

        mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDto)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenAuthorizedUser_whenLogout_then204() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("SESSION");

        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(cookie))
                .andExpect(status().isNoContent());
    }

    @Test
    void givenAuthorizedUser_whenShowUserDetails_then200() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("SESSION");

        mockMvc.perform(get("/api/user/me")
                        .cookie(cookie))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("name"));
    }

    @Test
    void givenUnauthorizedUser_whenShowUserDetails_then401() throws Exception {
        mockMvc.perform(get("/api/user/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void givenAuthorizedUser_whenLoggedIn_thenSessionExists() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("SESSION");
        MockHttpServletRequestBuilder requestBuilder = get("/api/user/me")
                .cookie(cookie);

        MvcResult meResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        String sessionId = sessionIdResolver.resolveSessionIds(meResult.getRequest()).get(0);
        String redisKey = redisKeyPrefix + sessionId;
        assertThat(redis.hasKey(redisKey)).isTrue();
    }

    @Test
    void givenAuthorizedUser_whenLogout_thenSessionDelete() throws Exception {
        RegisterRequestDto dto = new RegisterRequestDto("name", "pass");

        mockMvc.perform(post("/api/auth/sign-up")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());

        MvcResult result = mockMvc.perform(post("/api/auth/sign-in")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk())
                .andReturn();

        Cookie cookie = result.getResponse().getCookie("SESSION");
        MockHttpServletRequestBuilder requestBuilder = get("/api/user/me")
                .cookie(cookie);

        MvcResult meResult = mockMvc.perform(requestBuilder)
                .andExpect(status().isOk())
                .andReturn();

        String sessionId = sessionIdResolver.resolveSessionIds(meResult.getRequest()).get(0);
        String redisKey = redisKeyPrefix + sessionId;
        assertThat(redis.hasKey(redisKey)).isTrue();

        mockMvc.perform(post("/api/auth/sign-out")
                        .cookie(cookie))
                .andExpect(status().isNoContent());

        assertThat(redis.hasKey(redisKey)).isFalse();
    }

}

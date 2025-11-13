package com.gladkiei.cloudstorage.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class RegisterRequestDto {
    @NotBlank(message = "Username is blank")
    @Size(min = 3, max = 25, message = "Username must be 3 - 25 letters length")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ\\s]+$", message = "Only letters allowed")
    private String username;

    @NotBlank(message = "Password is blank")
    @Size(min = 3, max = 25, message = "Password must be between 3 and 25 length")
    private String password;
}

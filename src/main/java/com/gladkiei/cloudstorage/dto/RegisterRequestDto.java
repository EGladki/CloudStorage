package com.gladkiei.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User registration Dto")
public class RegisterRequestDto {

    @Schema(description = "Username", example = "Tom")
    @NotBlank(message = "Username is blank")
    @Size(min = 3, max = 25, message = "Username must be 3 - 25 letters length")
    @Pattern(regexp = "^[a-zA-Zа-яА-ЯёЁ\\s]+$", message = "Only letters allowed")
    private String username;

    @Schema(description = "Password", example = "12345")
    @NotBlank(message = "Password is blank")
    @Size(min = 3, max = 25, message = "Password must be between 3 and 25 length")
    private String password;
}

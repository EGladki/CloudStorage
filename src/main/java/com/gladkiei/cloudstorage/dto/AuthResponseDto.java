package com.gladkiei.cloudstorage.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Schema(description = "User auth response dto")
public class AuthResponseDto {
    private String username;
}

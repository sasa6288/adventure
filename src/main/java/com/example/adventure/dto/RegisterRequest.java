package com.example.adventure.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record RegisterRequest(
        @NotBlank @Size(min = 3, max = 50)
        @Pattern(regexp = "^[A-Za-z0-9_]+$", message = "用户名只能包含字母、数字和下划线")
        String username,
        @NotBlank @Size(min = 8, max = 72)
        String password,
        @NotBlank @Size(max = 50)
        String nickname
) {
}

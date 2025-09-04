package com.example.uvideo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class GenerateResetCodeRequest {
    @NotNull(message = "UserId must not be null")
    private Long userId;

    @NotBlank(message = "Email must not be blank")
    @Email
    private String email;

    public Long getUserId() {
        return userId;
    }

    public String getEmail() {
        return email;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
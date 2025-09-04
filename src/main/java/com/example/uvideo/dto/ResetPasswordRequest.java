package com.example.uvideo.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class ResetPasswordRequest {
    @NotNull(message = "UserId must not be null")
    private Long userId;

    @NotBlank(message = "Code must not be blank")
    private String code;

    @NotBlank(message = "Password must not be blank")
    private String password;

    public Long getUserId() {
        return userId;
    }

    public String getCode() {
        return code;
    }

    public String getPassword() {
        return password;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
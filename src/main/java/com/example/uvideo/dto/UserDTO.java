package com.example.uvideo.dto;

public class UserDTO {
    private Long id;
    private String phone;
    private String displayName;

    public UserDTO(Long id, String phone, String displayName) {
        this.id = id;
        this.phone = phone;
        this.displayName = displayName;
    }

    public Long getId() {
        return id;
    }

    public String getPhone() {
        return phone;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }
}
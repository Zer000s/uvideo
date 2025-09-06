package com.example.uvideo.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import org.springframework.beans.factory.annotation.Value;
import java.time.Instant;

@Entity
@Table(name = "users")
public class User {
    @Value("${SERVER_URL}")
    private String SERVER_URL;

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Email is not null")
    @Column(nullable = false, unique = true)
    private String email;

    @NotBlank(message = "Password is not null")
    @Column(nullable = false)
    private String password;

    @Column(length = 200)
    private String displayName;

    @Column()
    private String avatarUrl = SERVER_URL + "/user.png";

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    public Long getId() {
        return id;
    }

    public String getPassword() {
        return password;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getEmail() {
        return email;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String phone) {
        this.email = phone;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
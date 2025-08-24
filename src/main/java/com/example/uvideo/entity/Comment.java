package com.example.uvideo.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "comments")
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long videoId;

    @Column(nullable = false)
    private Long userId;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String text;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    // Getters and Setters
    public Long getId() { return id; }
    public Long getVideoId() { return videoId; }
    public Long getUserId() { return userId; }
    public String getText() { return text; }
    public Instant getCreatedAt() { return createdAt; }

    public void setId(Long id) { this.id = id; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setText(String text) { this.text = text; }
    public void setCreatedAt(Instant createdAt) { this.createdAt = createdAt; }
}
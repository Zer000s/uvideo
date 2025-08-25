package com.example.uvideo.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "video_views")
public class VideoView {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
    private Long videoId;

    @Column(nullable = false)
    private Long watchTime = 0L;

    @Column(nullable = false)
    private Instant viewedAt = Instant.now();

    public Long getId() { return id; }
    public Long getUserId() { return userId; }
    public Long getVideoId() { return videoId; }
    public Long getWatchTime() { return watchTime; }
    public Instant getViewedAt() { return viewedAt; }

    public void setId(Long id) { this.id = id; }
    public void setUserId(Long userId) { this.userId = userId; }
    public void setVideoId(Long videoId) { this.videoId = videoId; }
    public void setWatchTime(Long watchTime) { this.watchTime = watchTime; }
    public void setViewedAt(Instant viewedAt) { this.viewedAt = viewedAt; }
}
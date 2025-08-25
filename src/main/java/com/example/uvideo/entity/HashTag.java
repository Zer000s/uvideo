package com.example.uvideo.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "hash_tags")
public class HashTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long videoId;

    @Column(nullable = false)
    private String tag;

    public Long getId() {
        return id;
    }

    public Long getVideoId() {
        return videoId;
    }

    public String getTag() {
        return tag;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setTag(String tag) {
        this.tag = tag;
    }

    public void setVideoId(Long videoId) {
        this.videoId = videoId;
    }
}
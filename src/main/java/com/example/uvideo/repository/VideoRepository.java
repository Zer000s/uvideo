package com.example.uvideo.repository;

import com.example.uvideo.entity.Video;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface VideoRepository extends JpaRepository<Video, Long> {
    Optional<Video> findVideoById(Long id);
}
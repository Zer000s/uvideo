package com.example.uvideo.repository;

import com.example.uvideo.entity.VideoView;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VideoViewRepository extends JpaRepository<VideoView, Long> {

    List<VideoView> findByUserId(Long userId);

    List<VideoView> findByVideoId(Long videoId);

    boolean existsByUserIdAndVideoId(Long userId, Long videoId);
}
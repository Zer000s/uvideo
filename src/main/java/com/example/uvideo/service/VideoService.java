package com.example.uvideo.service;

import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.entity.Video;
import com.example.uvideo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class VideoService {
    private final VideoRepository videoRepository;

    public VideoService(VideoRepository videoRepository) {
        this.videoRepository = videoRepository;
    }

    @Transactional
    public void deleteVideo(Long videoId, Long userId) {
        Optional<Video> video = videoRepository.findVideoById(videoId);
        if (video.isEmpty()) {
            throw new GlobalException("Video with id " + videoId + " not found");
        }

        if (!video.get().getUserId().equals(userId)) {
            throw new GlobalException("You don't have permission to delete this video");
        }

        videoRepository.delete(video.get());
    }
}
package com.example.uvideo.controller;

import com.example.uvideo.entity.Video;
import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.repository.VideoRepository;
import com.example.uvideo.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;

@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoRepository videoRepository;

    @Autowired
    private VideoService videoService;

    private static final String VIDEO_DIR = "src/main/resources/static/videos";

    //work with video
    @PostMapping("/upload")
    public ResponseEntity<Object> uploadVideo(
            @RequestParam("file") @Valid MultipartFile file,
            @RequestParam("title") @Valid String title,
            @RequestParam(value = "description", required = false) @Valid String description,
            @RequestParam("userId") @Valid Long userId)
    {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());
        if (!originalFilename.toLowerCase().endsWith(".mp4")) {
            return ResponseEntity.badRequest().body("Only MP4 extension is supported");
        }

        try {
            String filename = System.currentTimeMillis() + "_" + originalFilename.replaceAll("[^a-zA-Z0-9\\.\\-_]", "_");
            File uploadDir = new File(System.getProperty("user.dir"), VIDEO_DIR);
            if (!uploadDir.exists()) {
                uploadDir.mkdirs();
            }

            File destination = new File(uploadDir, filename);
            file.transferTo(destination);

            Video video = new Video();
            video.setUserId(userId);
            video.setTitle(title);
            video.setDescription(description);
            video.setVideoUrl("/video/" + filename);
            video.setCreatedAt(Instant.now());

            videoRepository.save(video);

            return ResponseEntity.ok(video);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> uploadVideo(@RequestParam @Valid Long videoId) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userPrincipal = auth.getPrincipal().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userNode = mapper.readTree(userPrincipal);
        Long userId = userNode.get("id").asLong();

        videoService.deleteVideo(videoId, userId);

        return ResponseEntity.ok("Video has been deleted");
    }

    //search
    @GetMapping("/get_popular_videos")
    public ResponseEntity<Object> getRecommendedVideos() {
        try {
            List<Video> popularVideos = videoRepository.findTop10ByOrderByViewsDesc();
            return ResponseEntity.ok(popularVideos);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }

    @GetMapping("/get_video_by_id")
    public ResponseEntity<Object> getVideoById(@RequestParam("videoId") @Valid Long videoId)
    {
        try {
            Optional<Video> existingVideo = videoRepository.findVideoById(videoId);
            if (existingVideo.isEmpty()) {
                return ResponseEntity.badRequest().body("Video is not found");
            }
            return ResponseEntity.ok(existingVideo);
        }
        catch (Exception e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
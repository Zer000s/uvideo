package com.example.uvideo.controller;

import com.example.uvideo.entity.Video;
import com.example.uvideo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.time.Instant;

@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "*")
public class VideoController {

    @Autowired
    private VideoRepository videoRepository;

    private static final String VIDEO_DIR = "src/main/resources/static/videos";

    @PostMapping("/upload")
    public ResponseEntity<?> uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description,
            @RequestParam("userId") Long userId)
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
        } catch (IOException e) {
            return ResponseEntity.status(500).body(e.getMessage());
        }
    }
}
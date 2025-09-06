package com.example.uvideo.controller;

import com.example.uvideo.entity.Video;
import com.example.uvideo.service.AuthService;
import com.example.uvideo.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.io.*;

@RestController
@RequestMapping("/video")
@CrossOrigin(origins = "*")
public class VideoController {
    private final VideoService videoService;
    private final AuthService authService;

    @Autowired
    public VideoController(VideoService videoService, AuthService authService) {
        this.videoService = videoService;
        this.authService = authService;
    }

    @PostMapping(value = "/upload", consumes = "application/octet-stream")
    public ResponseEntity<Object> uploadStreamVideo(
            HttpServletRequest request,
            @RequestParam("title") @Valid String title,
            @RequestParam(value = "description", required = false) @Valid String description) throws IOException {

        Long userId = authService.getUserIdFromAuthentication();
        Video video = videoService.handleUploadVideo(userId, title, description, request.getInputStream());
        return ResponseEntity.ok(video);
    }

    @PutMapping(value = "/update")
    public ResponseEntity<Object> updateVideo(
            @RequestParam("videoId") Long videoId,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) throws IOException {

        Video video = videoService.updateVideo(videoId, title, description);
        return ResponseEntity.ok(video);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteVideo(@RequestParam @Valid Long videoId) throws JsonProcessingException {
        Long userId = authService.getUserIdFromAuthentication();
        videoService.deleteVideo(videoId, userId);
        return ResponseEntity.ok("Video has been deleted");
    }

    @GetMapping(value = "/static/{filename}", produces = "video/mp4")
    public ResponseEntity<Resource> getStreamVideoByFilename(
            @PathVariable @Valid String filename,
            @RequestHeader(value = "Range", required = false) @Valid String rangeHeader) throws IOException {

        return videoService.getVideoStream(filename, rangeHeader);
    }
}
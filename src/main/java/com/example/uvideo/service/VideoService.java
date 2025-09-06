package com.example.uvideo.service;

import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.entity.Video;
import com.example.uvideo.repository.VideoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.Optional;
import static com.example.uvideo.utils.Utils.VIDEO_DIR;

@Service
public class VideoService {
    private final VideoRepository videoRepository;
    private final AuthService authService;

    @Autowired
    public VideoService(VideoRepository videoRepository, AuthService authService) {
        this.videoRepository = videoRepository;
        this.authService = authService;
    }

    public Video handleUploadVideo(Long userId, String title, String description, InputStream inputStream) throws IOException {
        String filename = uploadStreamVideo(inputStream);
        return saveVideo(userId, title, description, filename);
    }

    public ResponseEntity<Resource> getVideoStream(String filename, String rangeHeader) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir"), VIDEO_DIR, filename);
        File file = path.toFile();

        if (!file.exists()) throw new GlobalException("Video is not found");

        long fileLength = file.length();
        long rangeStart = 0;
        long rangeEnd = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) rangeEnd = Long.parseLong(ranges[1]);
        }
        if (rangeEnd >= fileLength) rangeEnd = fileLength - 1;

        long contentLength = rangeEnd - rangeStart + 1;
        InputStream inputStream = new FileInputStream(file);
        inputStream.skip(rangeStart);
        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.status(rangeHeader == null ? 200 : 206)
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(contentLength))
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength)
                .body(resource);
    }

    @Transactional
    public void deleteVideo(Long videoId, Long userId) {
        Optional<Video> existingVideo = videoRepository.findVideoById(videoId);
        if (existingVideo.isEmpty()) {
            throw new GlobalException("Video with id " + videoId + " not found in DB");
        }
        if (!existingVideo.get().getUserId().equals(userId)) {
            throw new GlobalException("You don't have permission to delete this video");
        }
        File deletedVideo = new File(System.getProperty("user.dir"),"/" + VIDEO_DIR + "/" + existingVideo.get().getVideoUrl());
        if(!deletedVideo.exists()) {
            throw new GlobalException("Video with id " + videoId + " not found in FS");
        }
        boolean deleted = deletedVideo.delete();
        if (!deleted) {
            throw new GlobalException("Failed to delete video file from disk");
        }

        videoRepository.deleteById(videoId);
    }

    public String uploadStreamVideo(InputStream inputStream) throws IOException {
        String filename = System.currentTimeMillis() + ".mp4";

        File uploadDir = new File(System.getProperty("user.dir"), VIDEO_DIR);
        if (!uploadDir.exists()) {
            uploadDir.mkdirs();
        }

        File destination = new File(uploadDir, filename);

        try (FileOutputStream outputStream = new FileOutputStream(destination)) {
            byte[] buffer = new byte[8192]; // 8KB
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        }

        return filename;
    }

    public Video saveVideo(Long userId, String title, String description, String filename) {
        Video video = new Video();
        video.setUserId(userId);
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(filename);
        video.setCreatedAt(Instant.now());

        videoRepository.save(video);

        return video;
    }

    public Video updateVideo(Long videoId, String title, String description) throws IOException {
        Long userId = authService.getUserIdFromAuthentication();

        Optional<Video> existingVideo = videoRepository.findVideoById(videoId);
        if (existingVideo.isEmpty()) {
            throw new GlobalException("Video with id " + videoId + " not found in DB");
        }

        if (!existingVideo.get().getUserId().equals(userId)) {
            throw new GlobalException("You don't have permission to delete this video");
        }

        if (title != null && !title.isBlank()) {
            existingVideo.get().setTitle(title);
        }
        if (description != null && !description.isBlank()) {
            existingVideo.get().setDescription(description);
        }

        videoRepository.save(existingVideo.get());

        return existingVideo.get();
    }
}
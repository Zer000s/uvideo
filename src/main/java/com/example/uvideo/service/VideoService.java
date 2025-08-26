package com.example.uvideo.service;

import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.entity.Video;
import com.example.uvideo.repository.VideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.Instant;
import java.util.Optional;
import static com.example.uvideo.utils.Utils.VIDEO_DIR;

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

    public String saveVideoStream(InputStream inputStream) throws IOException {
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
}
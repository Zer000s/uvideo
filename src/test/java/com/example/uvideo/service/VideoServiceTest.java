package com.example.uvideo.service;

import com.example.uvideo.entity.Video;
import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.repository.VideoRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class VideoServiceTest {

    @Mock
    private VideoRepository videoRepository;

    @Mock
    private AuthService authService;

    @InjectMocks
    private VideoService videoService;

    private Path testDir;

    @BeforeEach
    void setUp() throws IOException {
        MockitoAnnotations.openMocks(this);
        testDir = Paths.get(System.getProperty("user.dir"), "test_videos");
        Files.createDirectories(testDir);
    }

    @Test
    void testHandleUploadVideo() throws IOException {
        byte[] data = "test video data".getBytes();
        Video video = videoService.handleUploadVideo(1L, "title", "desc", new ByteArrayInputStream(data));

        assertNotNull(video);
        assertEquals("title", video.getTitle());
        verify(videoRepository, times(1)).save(any(Video.class));
    }

    @Test
    void testDeleteVideo_FileNotExists() {
        Video video = new Video();
        video.setUserId(1L);
        when(videoRepository.findVideoById(1L)).thenReturn(Optional.of(video));

        assertThrows(GlobalException.class, () -> videoService.deleteVideo(1L, 1L));
    }

    @Test
    void testGetVideoStream_FileNotFound() {
        assertThrows(GlobalException.class, () -> videoService.getVideoStream("nofile.mp4", null));
    }

    @Test
    void testUpdateVideo_WrongUser() throws JsonProcessingException {
        Video video = new Video();
        video.setUserId(2L);
        when(videoRepository.findVideoById(1L)).thenReturn(Optional.of(video));
        when(authService.getUserIdFromAuthentication()).thenReturn(1L);

        assertThrows(GlobalException.class, () -> videoService.updateVideo(1L, "new", "new desc"));
    }
}
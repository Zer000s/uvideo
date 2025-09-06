package com.example.uvideo.controller;

import com.example.uvideo.entity.Video;
import com.example.uvideo.service.AuthService;
import com.example.uvideo.service.VideoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(VideoController.class)
@AutoConfigureMockMvc(addFilters = false) // отключает фильтры безопасности
public class VideoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private VideoService videoService;

    @MockitoBean
    private AuthService authService;

    @Test
    void testUploadVideo() throws Exception {
        when(authService.getUserIdFromAuthentication()).thenReturn(1L);
        when(videoService.handleUploadVideo(any(), any(), any(), any())).thenReturn(new Video());

        mockMvc.perform(post("/video/upload")
                        .content("test video".getBytes())
                        .param("title", "title")
                        .contentType("application/octet-stream"))
                .andExpect(status().isOk());
    }

    @Test
    void testDeleteVideo() throws Exception {
        when(authService.getUserIdFromAuthentication()).thenReturn(1L);

        mockMvc.perform(delete("/video/delete")
                        .param("videoId", "1"))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdateVideo() throws Exception {
        Video updated = new Video();
        updated.setId(1L);
        updated.setTitle("new title");
        when(videoService.updateVideo(anyLong(), anyString(), anyString())).thenReturn(updated);

        mockMvc.perform(put("/video/update")
                        .param("videoId", "1")
                        .param("title", "new title")
                        .param("description", "desc"))
                .andExpect(status().isOk());
    }

    @Test
    void testStreamVideoWithoutRange() throws Exception {
        when(videoService.getVideoStream(eq("video.mp4"), isNull()))
                .thenReturn(ResponseEntity.ok().build());

        mockMvc.perform(get("/video/static/video.mp4"))
                .andExpect(status().isOk());
    }

    @Test
    void testStreamVideoWithRange() throws Exception {
        when(videoService.getVideoStream(eq("video.mp4"), any()))
                .thenReturn(ResponseEntity.status(206).build());

        mockMvc.perform(get("/video/static/video.mp4")
                        .header("Range", "bytes=0-100"))
                .andExpect(status().isPartialContent());
    }
}

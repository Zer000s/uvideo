package com.example.uvideo.controller;

import com.example.uvideo.entity.Video;
import com.example.uvideo.dto.UserDTO;
import com.example.uvideo.exceptions.GlobalException;
import com.example.uvideo.repository.VideoRepository;
import com.example.uvideo.service.VideoService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.file.Path;
import java.nio.file.Paths;
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

    //work with video
    @PostMapping(value = "/upload", consumes = "application/octet-stream")
    public ResponseEntity<Object> uploadVideoStream(
            HttpServletRequest request,
            @RequestParam("title") String title,
            @RequestParam(value = "description", required = false) String description) throws IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userPrincipal = auth.getPrincipal().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userNode = mapper.readTree(userPrincipal);
        Long userId = userNode.get("id").asLong();

        String filename = videoService.saveVideoStream(request.getInputStream());

        Video video = videoService.saveVideo(userId, title, description, filename);
        return ResponseEntity.ok(video);
    }

    @DeleteMapping("/delete")
    public ResponseEntity<Object> deleteVideo(@RequestParam @Valid Long videoId) throws JsonProcessingException {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String userPrincipal = auth.getPrincipal().toString();
        ObjectMapper mapper = new ObjectMapper();
        JsonNode userNode = mapper.readTree(userPrincipal);
        Long userId = userNode.get("id").asLong();

        videoService.deleteVideo(videoId, userId);

        return ResponseEntity.ok("Video has been deleted");
    }

    //search
    @GetMapping(value = "/static/{filename}", produces = "video/mp4")
    public ResponseEntity<Resource> getVideoByFilename(
            @PathVariable String filename,
            @RequestHeader(value = "Range", required = false) String rangeHeader
    ) throws IOException {
        Path path = Paths.get(System.getProperty("user.dir"), "src/main/resources/static/data/", filename);
        File file = path.toFile();

        if (!file.exists()) {
            throw new GlobalException("Video is not found");
        }

        long fileLength = file.length();
        long rangeStart = 0;
        long rangeEnd = fileLength - 1;

        if (rangeHeader != null && rangeHeader.startsWith("bytes=")) {
            String[] ranges = rangeHeader.substring(6).split("-");
            rangeStart = Long.parseLong(ranges[0]);
            if (ranges.length > 1 && !ranges[1].isEmpty()) {
                rangeEnd = Long.parseLong(ranges[1]);
            }
        }

        if (rangeEnd >= fileLength) {
            rangeEnd = fileLength - 1;
        }

        long contentLength = rangeEnd - rangeStart + 1;

        InputStream inputStream = new FileInputStream(file);
        inputStream.skip(rangeStart);

        Resource resource = new InputStreamResource(inputStream);

        return ResponseEntity.status(rangeHeader == null ? 200 : 206) // 200 or 206 Partial Content
                .header("Content-Type", "video/mp4")
                .header("Accept-Ranges", "bytes")
                .header("Content-Length", String.valueOf(contentLength))
                .header("Content-Range", "bytes " + rangeStart + "-" + rangeEnd + "/" + fileLength)
                .body(resource);
    }
}
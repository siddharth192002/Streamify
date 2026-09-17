package com.netflix.videoservice.controller;

import com.netflix.videoservice.service.VideoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

@RestController
@RequestMapping("/api/v1/videos")
@RequiredArgsConstructor
@Slf4j
public class VideoController {

    private final VideoService videoService;

    /**
     * Upload video file for a movie.
     * Accepts multipart file upload.
     * <p>
     * POST /api/v1/videos/upload/{movieId}
     */
    @PostMapping("/upload/{movieId}")
    public ResponseEntity<String> uploadVideo(
            @PathVariable String movieId,
            @RequestParam("file") MultipartFile file) throws IOException {

        log.info("Video upload request for movie: {} file size: {}MB",
                movieId, file.getSize() / (1024 * 1024));

        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body("File is empty");
        }

        String videoKey = videoService.uploadVideo(movieId, file);

        return ResponseEntity.ok(
                "Video uploaded successfully! Key: " + videoKey +
                        " — Encoding started automatically via Kafka"
        );
    }
}
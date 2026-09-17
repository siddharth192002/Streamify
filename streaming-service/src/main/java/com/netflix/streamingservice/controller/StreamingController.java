package com.netflix.streamingservice.controller;


import com.netflix.streamingservice.dto.StreamingResponse;
import com.netflix.streamingservice.service.StreamingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/stream")
@RequiredArgsConstructor
@Slf4j
public class StreamingController {

    private final StreamingService streamingService;
    private final RedisTemplate<String, String> redisTemplate;

    private static final String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    // Get streaming URL for movie
    @GetMapping("/{movieId}")
    public ResponseEntity<StreamingResponse> getStreamingUrl(
            @PathVariable String movieId) {

        try {
            StreamingResponse response = streamingService
                    .getStreamingUrl(movieId);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /**
     * Serve signed m3u8 playlist content.
     * Called by HLS player for each quality playlist.
     * <p>
     * GET /api/v1/stream/{movieId}/playlist?path=encoded/movieId/1080p/playlist.m3u8
     */
    @GetMapping("/{movieId}/playlist")
    public ResponseEntity<String> getSignedPlaylist(
            @PathVariable String movieId,
            @RequestParam String path) {

        log.info("Playlist request for movie: {} path: {}", movieId, path);

        String signedPlaylist = streamingService
                .getSignedPlaylist(movieId, path);

        return ResponseEntity.ok()
                .header("Content-Type", "application/x-mpegURL")
                .body(signedPlaylist);
    }
}

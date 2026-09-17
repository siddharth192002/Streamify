package com.netflix.streamingservice.service;


import com.netflix.streamingservice.dto.StreamingResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class StreamingService {

    private final S3Client s3Client;
    private final S3Presigner s3Presigner;
    private final RedisTemplate<String, String> redisTemplate;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.presigned-url-expiry}")
    private long presignedUrlExpiry;

    private static final String STREAMING_URL_CACHE_PREFIX = "streaming:url:";
    private static final String MASTER_PLAYLIST_KEY_PREFIX = "streaming:playlist:";

    /**
     * Get streaming URL for a movie.
     * Reads master.m3u8 from S3
     * Signs every segment URL individually
     * Returns modified m3u8 as a string
     */
    public StreamingResponse getStreamingUrl(String movieId) {
        log.info("Getting streaming URL for movie: {}", movieId);

        // Check Redis cache first
        String cacheKey = STREAMING_URL_CACHE_PREFIX + movieId;
        String cachedUrl = redisTemplate.opsForValue().get(cacheKey);

        if (cachedUrl != null) {
            log.info("Returning cached streaming URL for movie: {}", movieId);
            return new StreamingResponse(movieId, cachedUrl,
                    "1080p, 720p, 480p, 360p", presignedUrlExpiry);
        }

        // Get master playlist key from Redis
        String playlistKey = redisTemplate.opsForValue()
                .get(MASTER_PLAYLIST_KEY_PREFIX + movieId);

        if (playlistKey == null) {
            throw new RuntimeException("Movie not ready for streaming: " + movieId);
        }

        // Generate presigned URL for master playlist
        String presignedMasterUrl = generatePresignedUrl(playlistKey);

        // Cache in Redis for 55 minutes
        redisTemplate.opsForValue().set(
                cacheKey,
                presignedMasterUrl,
                55,
                TimeUnit.MINUTES
        );

        log.info("Streaming URL generated and cached for movie: {}", movieId);

        return new StreamingResponse(
                movieId,
                presignedMasterUrl,
                "1080p, 720p, 480p, 360p",
                presignedUrlExpiry
        );
    }

    /**
     * Read m3u8 file from S3 and rewrite all
     * segment URLs with presigned URLs.
     * <p>
     * This is the KEY method that makes everything secure.
     * <p>
     * Original m3u8:
     * #EXTM3U
     * 1080p/playlist.m3u8
     * 720p/playlist.m3u8
     * <p>
     * Rewritten m3u8:
     * #EXTM3U
     * https://s3.../1080p/playlist.m3u8?X-Amz-Signature=...
     * https://s3.../720p/playlist.m3u8?X-Amz-Signature=...
     */
    public String getSignedPlaylist(String movieId, String playlistPath) {
        log.info("Getting signed playlist for movie: {} path: {}",
                movieId, playlistPath);

        // Get base path for this playlist
        String basePath = playlistPath.substring(0,
                playlistPath.lastIndexOf("/") + 1);

        // Read m3u8 content from S3
        String m3u8Content = readFromS3(playlistPath);

        // Rewrite each line that is a segment or playlist reference
        String signedContent = rewriteM3u8WithSignedUrls(
                m3u8Content, basePath);

        return signedContent;
    }

    /**
     * Rewrite m3u8 content replacing relative paths
     * with presigned S3 URLs.
     */
    private String rewriteM3u8WithSignedUrls(
            String m3u8Content, String basePath) {

        StringBuilder rewritten = new StringBuilder();

        for (String line : m3u8Content.split("\n")) {
            String trimmed = line.trim();

            // Skip empty lines and comments
            if (trimmed.isEmpty() || trimmed.startsWith("#")) {
                rewritten.append(line).append("\n");
                continue;
            }

            // This is a segment or playlist reference
            // Build full S3 key and sign it
            String fullKey = basePath + trimmed;
            String signedUrl = generatePresignedUrl(fullKey);

            rewritten.append(signedUrl).append("\n");
        }

        return rewritten.toString();
    }

    /**
     * Read file content from S3.
     */
    private String readFromS3(String s3Key) {
        GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        ResponseInputStream<GetObjectResponse> response =
                s3Client.getObject(request);

        return new BufferedReader(new InputStreamReader(response))
                .lines()
                .collect(Collectors.joining("\n"));
    }

    /**
     * Generate presigned URL for S3 object.
     */
    private String generatePresignedUrl(String s3Key) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .build();

        GetObjectPresignRequest presignRequest =
                GetObjectPresignRequest.builder()
                        .signatureDuration(Duration.ofMinutes(presignedUrlExpiry))
                        .getObjectRequest(getObjectRequest)
                        .build();

        return s3Presigner.presignGetObject(presignRequest)
                .url()
                .toString();
    }

    /**
     * Invalidate cached URL.
     */
    public void invalidateCache(String movieId) {
        redisTemplate.delete(STREAMING_URL_CACHE_PREFIX + movieId);
        log.info("Cache invalidated for movie: {}", movieId);
    }
}

package com.netflix.encodingservice.event;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Published to Kafka topic: video.encoded
 * Consumed by Content Service to update HLS URL.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEncodedEvent {
    private String movieId;
    private String hlsUrl;          // Master playlist URL for streaming
    private String masterPlaylistKey; // S3 key of master.m3u8
    private boolean success;
    private String errorMessage;    // If encoding failed
}

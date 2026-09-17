package com.netflix.streamingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consumed from Kafka topic: video.encoded
 * Published by Encoding Service after FFmpeg processing.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoEncodedEvent {
    private String movieId;
    private String hlsUrl;
    private String masterPlaylistKey;
    private boolean success;
    private String errorMessage;
}

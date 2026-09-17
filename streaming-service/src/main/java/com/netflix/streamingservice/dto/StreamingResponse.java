package com.netflix.streamingservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class StreamingResponse {
    private String movieId;
    private String streamingUrl;      // Presigned HLS master playlist URL
    private String quality;           // Available qualities
    private long expiresInMinutes;    // URL expiry time
}

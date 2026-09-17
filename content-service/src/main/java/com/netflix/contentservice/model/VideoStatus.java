package com.netflix.contentservice.model;

public enum VideoStatus {
    PENDING,    // Movie added but no video uploaded yet
    UPLOADED,   // Raw video uploaded to S3
    ENCODING,   // FFmpeg is encoding the video
    ENCODED,    // Encoding complete
    READY,      // HLS playlist ready — can be streamed
    FAILED      // Encoding failed
}

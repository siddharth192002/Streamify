package com.netflix.encodingservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consumed from Kafka topic: video.uploaded
 * Published by Video Service after S3 upload.
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadedEvent {
    private String movieId;
    private String videoKey;
    private String bucketName;
    private String originalFileName;
    private long fileSizeBytes;
}

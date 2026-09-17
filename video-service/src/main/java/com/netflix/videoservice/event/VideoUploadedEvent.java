package com.netflix.videoservice.event;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VideoUploadedEvent {
    private String movieId;
    private String videoKey;      // S3 key of uploaded raw video
    private String bucketName;    // S3 bucket name
    private String originalFileName;
    private long fileSizeBytes;
}

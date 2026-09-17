package com.netflix.contentservice.service;

import com.netflix.contentservice.model.VideoStatus;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@Slf4j
@RequiredArgsConstructor
public class VideoUploadedEncodedEventConsumer {

    private final ContentService contentService;

    @KafkaListener(
            topics = "video.uploaded"
    )
    public void consumerVideoUploadedEvent(
            @Payload Map<String, Object> payload) {
        String movieId = (String) payload.get("movieId");
        String videoKey = (String) payload.get("videoKey");

        log.info("Video uploaded for movie: {} key: {}", movieId, videoKey);
        contentService.updateVideoKey(movieId, videoKey);
    }

    @KafkaListener(
            topics = "video.encoded"
    )
    public void consumeVideoEncodedEvent(
            @Payload Map<String, Object> payload) {
        String movieId = (String) payload.get("movieId");
        String hlsUrl = (String) payload.get("hlsUrl");
        boolean success = (Boolean) payload.get("success");

        if (success) {
            contentService.updateHlsUrl(movieId, hlsUrl);
        } else {
            String errorMessage = (String) payload.get("errorMessage");
            contentService.updateVideoStatus(movieId, VideoStatus.FAILED);
        }
    }


}

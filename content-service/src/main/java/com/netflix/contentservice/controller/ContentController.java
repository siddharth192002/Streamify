package com.netflix.contentservice.controller;

import com.netflix.contentservice.dto.MovieRequest;
import com.netflix.contentservice.dto.MovieResponse;
import com.netflix.contentservice.model.Genre;
import com.netflix.contentservice.service.ContentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/movies")
@RequiredArgsConstructor
@Slf4j
public class ContentController {

    private final ContentService contentService;

    // Add new movie to catalog
    @PostMapping
    public ResponseEntity<MovieResponse> addMovie(
            @Valid @RequestBody MovieRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(contentService.addMovie(request));
    }

    // Get all movies
    @GetMapping
    public ResponseEntity<List<MovieResponse>> getAllMovies() {
        return ResponseEntity.ok(contentService.getAllMovies());
    }

    // Get movie by ID
    @GetMapping("/{movieId}")
    public ResponseEntity<MovieResponse> getMovieById(
            @PathVariable String movieId) {
        return ResponseEntity.ok(contentService.getMovieById(movieId));
    }

    // Get movies by genre
    @GetMapping("/genre/{genre}")
    public ResponseEntity<List<MovieResponse>> getMoviesByGenre(
            @PathVariable Genre genre) {
        return ResponseEntity.ok(contentService.getMoviesByGenre(genre));
    }

    // Search movies by title
    @GetMapping("/search")
    public ResponseEntity<List<MovieResponse>> searchMovies(
            @RequestParam String title) {
        return ResponseEntity.ok(contentService.searchMovies(title));
    }
}
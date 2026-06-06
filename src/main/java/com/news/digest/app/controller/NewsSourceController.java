package com.news.digest.app.controller;


import com.news.digest.app.dto.ApiResponse;
import com.news.digest.app.model.NewsSource;
import com.news.digest.app.repository.NewsSourceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * NewsSource endpoints:
 *  GET  /api/sources              → all active sources (public)
 *  GET  /api/sources/{id}         → source by ID (public)
 *  GET  /api/sources/category/{c} → sources by category (public)
 *  POST /api/sources              → add source (admin)
 *  PUT  /api/sources/{id}         → update source (admin)
 *  DELETE /api/sources/{id}       → delete source (admin)
 */
@RestController
@RequestMapping("/api/sources")
@RequiredArgsConstructor
public class NewsSourceController {

    private final NewsSourceRepository newsSourceRepository;

    @GetMapping
    public ResponseEntity<ApiResponse<List<NewsSource>>> getAllSources() {
        return ResponseEntity.ok(ApiResponse.success("Sources fetched",
                newsSourceRepository.findByIsActiveTrue()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsSource>> getSourceById(@PathVariable Long id) {
        return newsSourceRepository.findById(id)
                .map(s -> ResponseEntity.ok(ApiResponse.success("Source found", s)))
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/category/{category}")
    public ResponseEntity<ApiResponse<List<NewsSource>>> getByCategory(@PathVariable String category) {
        return ResponseEntity.ok(ApiResponse.success("Sources fetched",
                newsSourceRepository.findByCategory(category)));
    }

    @GetMapping("/language/{language}")
    public ResponseEntity<ApiResponse<List<NewsSource>>> getByLanguage(@PathVariable String language) {
        return ResponseEntity.ok(ApiResponse.success("Sources fetched",
                newsSourceRepository.findByLanguage(language)));
    }

    @GetMapping("/country/{country}")
    public ResponseEntity<ApiResponse<List<NewsSource>>> getByCountry(@PathVariable String country) {
        return ResponseEntity.ok(ApiResponse.success("Sources fetched",
                newsSourceRepository.findByCountry(country)));
    }

    @PostMapping
    public ResponseEntity<ApiResponse<NewsSource>> createSource(@RequestBody NewsSource source) {
        if (newsSourceRepository.findByName(source.getName()).isPresent()) {
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("Source with this name already exists"));
        }
        NewsSource saved = newsSourceRepository.save(source);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Source created", saved));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<NewsSource>> updateSource(
            @PathVariable Long id, @RequestBody NewsSource updated) {

        return newsSourceRepository.findById(id).map(source -> {
            source.setName(updated.getName());
            source.setDescription(updated.getDescription());
            source.setUrl(updated.getUrl());
            source.setLogoUrl(updated.getLogoUrl());
            source.setCategory(updated.getCategory());
            source.setLanguage(updated.getLanguage());
            source.setCountry(updated.getCountry());
            source.setIsActive(updated.getIsActive());
            source.setIsPremium(updated.getIsPremium());
            source.setTrustScore(updated.getTrustScore());
            return ResponseEntity.ok(ApiResponse.success("Source updated",
                    newsSourceRepository.save(source)));
        }).orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteSource(@PathVariable Long id) {
        if (!newsSourceRepository.existsById(id)) return ResponseEntity.notFound().build();
        newsSourceRepository.deleteById(id);
        return ResponseEntity.ok(ApiResponse.success("Source deleted", null));
    }
}

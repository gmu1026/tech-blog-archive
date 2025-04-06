package net.cproduction.techblogarchive.controller;

import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.crawler.RssCrawlerService;
import net.cproduction.techblogarchive.model.BlogSourceDto;
import net.cproduction.techblogarchive.service.BlogSourceService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api/blog-sources")
@RequiredArgsConstructor
public class BlogSourceController {
    private final RssCrawlerService rssCrawlerService;
    private final BlogSourceService blogSourceService;

    @GetMapping("/test-rss")
    public ResponseEntity<?> testRssFeed(
            @RequestParam("feedUrl") String feedUrl,
            @RequestParam(value = "limit", defaultValue = "5") int limit) {
        try {
            log.info("Testing RSS feed: {}", feedUrl);
            Map<String, Object> result = rssCrawlerService.testRssFeed(feedUrl, limit);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            log.error("Error testing RSS feed: {}", feedUrl, e);
            return ResponseEntity.badRequest().body(Map.of(
                    "error", e.getMessage(),
                    "feedUrl", feedUrl
            ));
        }
    }

    @GetMapping("/validate-rss")
    public ResponseEntity<?> validateRssFeed(@RequestParam("feedUrl") String feedUrl) {
        log.info("Validating RSS feed: {}", feedUrl);
        boolean isValid = rssCrawlerService.isValidRssFeed(feedUrl);

        Map<String, Object> response = new HashMap<>();
        response.put("feedUrl", feedUrl);
        response.put("valid", isValid);

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<BlogSourceDto>> getAllBlogSources() {
        log.info("Received request to get all blog sources");

        return ResponseEntity.ok(blogSourceService.getAllBlogSources());
    }

    @GetMapping("/active")
    public ResponseEntity<List<BlogSourceDto>> getActiveBlogSources() {
        log.info("Received request to get active blog sources");

        return ResponseEntity.ok(blogSourceService.getActiveBlogSources());
    }

    @GetMapping("/{id}")
    public ResponseEntity<BlogSourceDto> getBlogSourceById(@PathVariable("id") Long id) {
        log.info("Received request to get blog source with ID: {}", id);

        return blogSourceService.getBlogSourceById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PostMapping
    public ResponseEntity<?> createBlogSource(@Valid @RequestBody BlogSourceDto blogSourceDto) {
        log.info("Received request to create new blog source: {}", blogSourceDto.getName());
        try {
            BlogSourceDto createdSource = blogSourceService.createBlogSource(blogSourceDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(createdSource);
        } catch (IllegalArgumentException e) {
            log.error("Error creating blog source: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateBlogSource(
            @PathVariable("id") Long id,
            @Valid @RequestBody BlogSourceDto blogSourceDto) {
        log.info("Received request to update blog source with ID: {}", id);
        try {
            return blogSourceService.updateBlogSource(id, blogSourceDto)
                    .map(ResponseEntity::ok)
                    .orElse(ResponseEntity.notFound().build());
        } catch (IllegalArgumentException e) {
            log.error("Error updating blog source: {}", e.getMessage());
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PatchMapping("/{id}/active")
    public ResponseEntity<?> updateBlogSourceActiveStatus(
            @PathVariable("id") Long id,
            @RequestParam("active") boolean active) {
        log.info("Received request to update active status of blog source with ID: {} to {}", id, active);

        return blogSourceService.updateBlogSourceActiveStatus(id, active)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBlogSource(@PathVariable("id") Long id) {
        log.info("Received request to delete blog source with ID: {}", id);
        boolean deleted = blogSourceService.deleteBlogSource(id);

        return deleted ? ResponseEntity.noContent().build() : ResponseEntity.notFound().build();
    }
}

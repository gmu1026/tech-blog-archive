package net.cproduction.techblogarchive.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.service.BlogPostService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@RestController
public class BlogPostController {
    private final BlogPostService blogPostService;

    @GetMapping
    public ResponseEntity<?> getAllBlogPost(Pageable pageable, @RequestParam(required = false) String query) {
        if (query != null) {
            log.info("Searching for blog posts with query {}", query);
            return ResponseEntity.ok(blogPostService.getBlogPostsForQuery(pageable, query));
        } else {
            log.info("Received request to get all blog posts");
            return ResponseEntity.ok(blogPostService.getAllBlogPosts(pageable));
        }
    }
}

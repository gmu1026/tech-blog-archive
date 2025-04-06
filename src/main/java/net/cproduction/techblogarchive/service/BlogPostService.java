package net.cproduction.techblogarchive.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.entity.BlogPost;
import net.cproduction.techblogarchive.entity.BlogSource;
import net.cproduction.techblogarchive.model.BlogPostDto;
import net.cproduction.techblogarchive.repository.BlogPostRepository;
import net.cproduction.techblogarchive.repository.BlogSourceRepository;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class BlogPostService {
    private final BlogPostRepository blogPostRepository;
    private final BlogSourceRepository blogSourceRepository;

    @Transactional
    public int processAndSavePosts(List<BlogPost> newPosts, BlogSource source) {
        List<BlogPost> uniquePosts = filterDuplicatePosts(newPosts, source);

        if (!uniquePosts.isEmpty()) {
            blogPostRepository.saveAll(uniquePosts);
            log.info("블로그 '{}': {} 개의 새 포스트 추가 (중복 제외)",
                    source.getName(), uniquePosts.size());
        } else {
            log.info("블로그 '{}': 새 포스트 없음", source.getName());
        }

        source.setLastCrawledAt(LocalDateTime.now());
        blogSourceRepository.save(source);

        return uniquePosts.size();
    }

    private List<BlogPost> filterDuplicatePosts(List<BlogPost> newPosts, BlogSource source) {
        List<BlogPost> uniquePosts = new ArrayList<>();

        for (BlogPost post : newPosts) {
            post.setBlogSource(source);

            boolean exists;
            if (post.getLink() != null && !post.getLink().isEmpty()) {
                exists = blogPostRepository.existsByLink(post.getLink());
            } else {
                exists = blogPostRepository.existsByTitleAndBlogSource(post.getTitle(), source);
            }
            if (!exists) {
                uniquePosts.add(post);
            }
        }

        return uniquePosts;
    }

    @Transactional(readOnly = true)
    public Slice<BlogPostDto> getAllBlogPosts(Pageable pageable) {
        Slice<BlogPost> posts = blogPostRepository.findAllBy(pageable);

        return posts.map(this::convertToDto);
    }

    @Transactional(readOnly = true)
    public Slice<BlogPostDto> getBlogPostsForQuery(Pageable pageable, String query) {
        Slice<BlogPost> posts = blogPostRepository.findAllByTitleContaining(query);
        return posts.map(this::convertToDto);
    }


    private BlogPostDto convertToDto(BlogPost blogPost) {
        return BlogPostDto.builder()
                .id(blogPost.getId())
                .title(blogPost.getTitle())
                .link(blogPost.getLink())
                .content(blogPost.getContent())
                .publishedAt(blogPost.getPublishedAt())
                .blogSourceName(blogPost.getBlogSource().getName())
                .build();
    }
}

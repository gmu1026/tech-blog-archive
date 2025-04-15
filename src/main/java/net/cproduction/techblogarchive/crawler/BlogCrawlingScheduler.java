package net.cproduction.techblogarchive.crawler;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.entity.BlogPost;
import net.cproduction.techblogarchive.entity.BlogSource;
import net.cproduction.techblogarchive.model.NotificationEvent;
import net.cproduction.techblogarchive.repository.BlogSourceRepository;
import net.cproduction.techblogarchive.service.BlogPostService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Slf4j
@Component
public class BlogCrawlingScheduler {
    private final HtmlCrawlerService htmlCrawlerService;
    private final RssCrawlerService rssCrawlerService;
    private final BlogPostService blogPostService;
    private final BlogSourceRepository blogSourceRepository;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @Value("${crawler.max-pages:100}")
    private int maxPagesToProcess;

    @Value("${crawler.thread-pool-size:10}")
    private int threadPoolSize;

    /**
     * 활성화된 블로그 포스트를 주기적으로 크롤링합니다. 기본값으로 10분(600000ms)마다 실행됩니다.
     */
    @Scheduled(fixedRateString = "${crawler.schedule.rate:600000}")
    public void crawlBlogPosts() {
        log.info("블로그 크롤링 작업 시작");
        List<BlogSource> sources = blogSourceRepository.findAll();

        if (sources.isEmpty()) {
            log.info("크롤링할 블로그 소스가 없습니다");
            return;
        }

        ExecutorService executor = Executors.newFixedThreadPool(threadPoolSize);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);
        AtomicInteger totalPostsCount = new AtomicInteger(0);

        List<CompletableFuture<Void>> futures = new ArrayList<>();
        for (BlogSource source : sources) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    log.info("블로그 소스 크롤링 시작: {}", source.getName());

                    List<BlogPost> newPosts = Collections.emptyList();
                    if (source.getUseRss()) {
                        newPosts = rssCrawlerService.crawlRssFeed(source);
                    } else {
                        newPosts = htmlCrawlerService.crawlBlogPostsWithPaginationLinks(source, maxPagesToProcess);
                    }

                    int uniquePostsCount = blogPostService.processAndSavePosts(newPosts, source);
                    totalPostsCount.addAndGet(uniquePostsCount);

                    successCount.incrementAndGet();
                    log.info("블로그 '{}' 크롤링 완료: {} 개의 새 포스트", source.getName(), uniquePostsCount);
                } catch (IOException e) {
                    log.error("블로그 '{}'의 크롤링 중 I/O 오류 발생: {}", source.getName(), e.getMessage(), e);
                    failCount.incrementAndGet();
                } catch (Exception e) {
                    log.error("블로그 '{}'의 크롤링 중 예외 발생: {}", source.getName(), e.getMessage(), e);
                    failCount.incrementAndGet();
                }
            }, executor);

            futures.add(future);
        }

        CompletableFuture<Void> allFutures = CompletableFuture.allOf(
                futures.toArray(new CompletableFuture[0])
        );

        try {
            allFutures.join();

            if (totalPostsCount.get() > 0) {
                publishNewBlogPostsEvent(totalPostsCount.get());
            }
            log.info("블로그 크롤링 작업 완료: 성공 {}, 실패 {}, 총 수집 포스트 {}개",
                    successCount.get(), failCount.get(), totalPostsCount.get());
        } finally {
            executor.shutdown();
        }
    }

    private void publishNewBlogPostsEvent(int postCount) {
        NotificationEvent event = new NotificationEvent();
        event.setId(UUID.randomUUID().toString());
        event.setTitle("새 글 등록");
        event.setPostCount(postCount);
        event.setTimestamp(Instant.now());

        kafkaTemplate.send("blog-posts", "new-posts", event);
        log.info("새로운 게시글 {}개 등록 이벤트 발행", postCount);
    }
}
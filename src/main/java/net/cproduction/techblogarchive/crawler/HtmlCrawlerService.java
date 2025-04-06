package net.cproduction.techblogarchive.crawler;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.entity.BlogPost;
import net.cproduction.techblogarchive.entity.BlogSource;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Slf4j
@Service
public class HtmlCrawlerService {

    private static final int TIMEOUT_SECONDS = 10;

    /**
     * 크롤링에 사용할 User-Agent 문자열
     */
    private static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/91.0.4472.124 Safari/537.36";

    public String fetchHtmlFromUrl(String urlString) throws IOException {
        log.info("Fetching HTML from URL: {}", urlString);
        Document document = getDocument(urlString);
        return document.html();
    }

    public List<BlogPost> crawlBlogPostsWithPaginationLinks(BlogSource blogSource, int maxPages) throws IOException {
        List<BlogPost> allBlogPosts = new ArrayList<>();
        Set<String> visitedUrls = new HashSet<>();

        String currentUrl = blogSource.getUrl();
        if (currentUrl == null || currentUrl.isEmpty()) {
            log.error("URL is null or empty for blog source: {}", blogSource.getName());
            return allBlogPosts;
        }

        int pageCount = 0;

        while (pageCount < maxPages && currentUrl != null && !visitedUrls.contains(currentUrl)) {
            log.info("Crawling page {} of {}: {}", pageCount + 1, maxPages, currentUrl);
            visitedUrls.add(currentUrl);

            try {
                BlogSource currentPageSource = createBlogSourceWithNewUrl(blogSource, currentUrl);

                Document document = getDocument(currentUrl);
                List<BlogPost> pagePosts = crawlBlogPostsFromDocument(currentPageSource, document);

                if (pagePosts != null && !pagePosts.isEmpty()) {
                    pagePosts.forEach(post -> post.setBlogSource(blogSource));
                    allBlogPosts.addAll(pagePosts);
                    log.info("Added {} posts from page {}", pagePosts.size(), pageCount + 1);
                }

                pageCount++;

                currentUrl = findNextPageUrl(document, blogSource.getPaginationSelector(),
                        blogSource.getNextPageSelector(), currentUrl);

                if (currentUrl == null) {
                    log.info("No next page found after page {}", pageCount);
                    break;
                }

                sleepBetweenRequests(blogSource.getCrawlDelay());
            } catch (InterruptedException e) {
                log.error("Thread interrupted while crawling: {}", currentUrl, e);
                Thread.currentThread().interrupt();
                break;
            } catch (IOException e) {
                log.error("IO error while crawling page {}: {}", pageCount + 1, currentUrl, e);
                currentUrl = null;
            } catch (Exception e) {
                log.error("Unexpected error crawling page {}: {}", pageCount + 1, currentUrl, e);
                currentUrl = null;
            }
        }

        log.info("Completed crawling {} posts from {} pages of {}",
                allBlogPosts.size(), pageCount, blogSource.getName());

        return allBlogPosts;
    }

    public List<BlogPost> crawlBlogPostsWithPaginationLinks(BlogSource blogSource) throws IOException {
        return crawlBlogPostsWithPaginationLinks(blogSource, 30);
    }

    private Document getDocument(String urlString) throws IOException {
        validateUrl(urlString);

        try {
            Document document = Jsoup.connect(urlString)
                    .timeout((int) TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS))
                    .userAgent(USER_AGENT)
                    .get();

            log.info("Successfully fetched HTML from URL: {}", urlString);
            return document;
        } catch (IOException e) {
            log.error("Failed to fetch HTML from URL: {}", urlString, e);
            throw new IOException("HTML 콘텐츠를 가져오는 데 실패했습니다: " + urlString, e);
        }
    }

    private void sleepBetweenRequests(Long delay) throws InterruptedException {
        Thread.sleep(delay != null ? delay : 1000);
    }

    private List<BlogPost> crawlBlogPostsFromDocument(BlogSource blogSource, Document document) {
        List<BlogPost> blogPosts = new ArrayList<>();

        if (blogSource.getPostSelector() == null || blogSource.getPostSelector().isEmpty()) {
            log.warn("Post selector is missing for blog source: {}", blogSource.getName());
            return blogPosts;
        }

        Elements postElements = document.select(blogSource.getPostSelector());
        if (postElements.isEmpty()) {
            log.warn("No posts found using selector '{}' for blog source: {}",
                    blogSource.getPostSelector(), blogSource.getName());
            return blogPosts;
        }

        DateTimeFormatter formatter = null;
        if (StringUtils.hasText(blogSource.getDateFormat())) {
            try {
                formatter = DateTimeFormatter.ofPattern(blogSource.getDateFormat());
            } catch (IllegalArgumentException e) {
                log.warn("Invalid date format pattern: {}", blogSource.getDateFormat(), e);
            }
        }

        for (Element post : postElements) {
            try {
                String title = extractElementText(post, blogSource.getTitleSelector());
                if (!StringUtils.hasText(title)) {
                    log.debug("Skipping post with empty title using selector: {}", blogSource.getTitleSelector());
                    continue;
                }

                String content = extractElementText(post, blogSource.getContentSelector());
                if (content.length() > 200) {
                    content = content.substring(0, 197) + "...";
                }

                String link = extractElementLink(post, blogSource.getLinkSelector(), blogSource.getUrl());
                if (!StringUtils.hasText(link)) {
                    log.debug("Skipping post with empty link using selector: {}", blogSource.getLinkSelector());
                    continue;
                }

                LocalDate publishedAt = null;
                if (formatter != null) {
                    String dateText = extractElementText(post, blogSource.getDateSelector());
                    publishedAt = parseDate(dateText, formatter);
                }

                BlogPost blogPost = BlogPost.builder()
                        .title(title)
                        .content(content)
                        .link(link)
                        .publishedAt(publishedAt)
                        .build();

                blogPosts.add(blogPost);
            } catch (Exception e) {
                log.warn("Error parsing post element: {}", e.getMessage());
            }
        }

        return blogPosts;
    }

    private String findNextPageUrl(Document document, String paginationSelector,
                                   String nextPageSelector, String currentUrl) {
        try {
            Element paginationElement = null;

            if (StringUtils.hasText(paginationSelector)) {
                paginationElement = document.selectFirst(paginationSelector);
            } else {
                paginationElement = document;
            }

            if (paginationElement == null) {
                return null;
            }

            Element nextPageLink = null;

            if (StringUtils.hasText(nextPageSelector)) {
                nextPageLink = paginationElement.selectFirst(nextPageSelector);
            } else {
                nextPageLink = paginationElement.select("a:containsOwn(다음), a:containsOwn(next), a:containsOwn(Next)")
                        .first();

                if (nextPageLink == null) {
                    nextPageLink = paginationElement.select(
                            "a[aria-label*=다음], a[aria-label*=next], a[aria-label*=Next]").first();
                }

                if (nextPageLink == null) {
                    Elements pageLinks = paginationElement.select("a.active, a.current, span.current, li.active > a");
                    if (!pageLinks.isEmpty()) {
                        Element currentPageElement = pageLinks.first();
                        Element parentElement = currentPageElement.parent();
                        if (parentElement != null) {
                            Element nextSibling = parentElement.nextElementSibling();
                            if (nextSibling != null) {
                                nextPageLink = nextSibling.selectFirst("a");
                            }
                        }
                    }
                }
            }

            if (nextPageLink != null && nextPageLink.hasAttr("href")) {
                String nextUrl = nextPageLink.attr("href");

                if (!nextUrl.startsWith("http")) {
                    nextUrl = new URL(new URL(currentUrl), nextUrl).toString();
                }

                return nextUrl;
            }
        } catch (Exception e) {
            log.warn("Error finding next page URL: {}", e.getMessage());
        }

        return null;
    }

    private String extractElementText(Element parent, String selector) {
        if (!StringUtils.hasText(selector)) {
            return "";
        }
        try {
            Element element = parent.selectFirst(selector);
            return element != null ? element.text().trim() : "";
        } catch (Exception e) {
            log.warn("Error extracting text with selector '{}': {}", selector, e.getMessage());
            return "";
        }
    }

    private String extractElementLink(Element parent, String selector, String baseUrl) {
        if (!StringUtils.hasText(selector) || !StringUtils.hasText(baseUrl)) {
            return "";
        }

        try {
            Element element = parent.selectFirst(selector);
            if (element == null) {
                return "";
            }

            String link = element.hasAttr("href") ? element.attr("href").trim() : "";
            if (link.isEmpty()) {
                return "";
            }

            if (!link.startsWith("http")) {
                try {
                    link = new URL(new URL(baseUrl), link).toString();
                } catch (MalformedURLException e) {
                    log.warn("Failed to convert relative URL '{}' to absolute using base URL '{}': {}",
                            link, baseUrl, e.getMessage());
                }
            }

            return link;
        } catch (Exception e) {
            log.warn("Error extracting link with selector '{}': {}", selector, e.getMessage());
            return "";
        }
    }

    private LocalDate parseDate(String dateText, DateTimeFormatter formatter) {
        if (!StringUtils.hasText(dateText) || formatter == null) {
            return null;
        }

        try {
            return LocalDate.parse(dateText.trim(), formatter);
        } catch (DateTimeParseException e) {
            log.warn("Failed to parse date: '{}' using format pattern: '{}': {}",
                    dateText, formatter, e.getMessage());
            return null;
        }
    }

    private BlogSource createBlogSourceWithNewUrl(BlogSource original, String newUrl) {
        return BlogSource.builder()
                .name(original.getName())
                .url(newUrl)
                .postSelector(original.getPostSelector())
                .titleSelector(original.getTitleSelector())
                .contentSelector(original.getContentSelector())
                .linkSelector(original.getLinkSelector())
                .dateSelector(original.getDateSelector())
                .dateFormat(original.getDateFormat())
                .paginationSelector(original.getPaginationSelector())
                .nextPageSelector(original.getNextPageSelector())
                .crawlDelay(original.getCrawlDelay())
                .build();
    }

    public Map<String, Object> testSelectors(String urlString, String postSelector, String titleSelector,
                                             String contentSelector, String linkSelector, String dateSelector,
                                             int limit) throws IOException {
        log.info("Testing selectors for URL: {}", urlString);
        Document document = getDocument(urlString);

        Map<String, Object> result = new HashMap<>();

        Elements postElements = document.select(postSelector);
        result.put("totalPosts", postElements.size());

        List<Map<String, String>> posts = new ArrayList<>();
        int count = 0;

        for (Element post : postElements) {
            if (count >= limit) {
                break;
            }

            Map<String, String> postData = new HashMap<>();

            if (StringUtils.hasText(titleSelector)) {
                String title = extractElementText(post, titleSelector);
                postData.put("title", title);
            }

            if (StringUtils.hasText(contentSelector)) {
                String content = extractElementText(post, contentSelector);
                if (content.length() > 200) {
                    content = content.substring(0, 197) + "...";
                }
                postData.put("content", content);
            }

            if (StringUtils.hasText(linkSelector)) {
                String link = extractElementLink(post, linkSelector, urlString);
                postData.put("link", link);
            }

            if (StringUtils.hasText(dateSelector)) {
                String date = extractElementText(post, dateSelector);
                postData.put("date", date);
            }

            posts.add(postData);
            count++;
        }

        result.put("posts", posts);
        result.put("url", urlString);
        result.put("selectors", Map.of(
                "postSelector", postSelector,
                "titleSelector", titleSelector != null ? titleSelector : "",
                "contentSelector", contentSelector != null ? contentSelector : "",
                "linkSelector", linkSelector != null ? linkSelector : "",
                "dateSelector", dateSelector != null ? dateSelector : ""
        ));

        log.info("Selector test completed for URL: {}, found {} posts", urlString, postElements.size());

        return result;
    }

    private void validateUrl(String urlString) throws IOException {
        if (!StringUtils.hasText(urlString)) {
            throw new IOException("URL은 비어있을 수 없습니다.");
        }

        try {
            URL url = new URL(urlString);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("HEAD");
            connection.setConnectTimeout((int) TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            connection.setReadTimeout((int) TimeUnit.SECONDS.toMillis(TIMEOUT_SECONDS));
            connection.setRequestProperty("User-Agent", USER_AGENT);

            int responseCode = connection.getResponseCode();
            if (responseCode >= 400) {
                throw new IOException("URL returned error code: " + responseCode);
            }
        } catch (IOException e) {
            log.error("Failed to validate URL: {}", urlString, e);
            throw new IOException("유효하지 않거나 접근할 수 없는 URL: " + urlString, e);
        }
    }
}

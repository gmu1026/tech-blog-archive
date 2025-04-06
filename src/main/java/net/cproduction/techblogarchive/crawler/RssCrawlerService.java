package net.cproduction.techblogarchive.crawler;

import com.rometools.rome.feed.synd.SyndCategory;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.FeedException;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.entity.BlogPost;
import net.cproduction.techblogarchive.entity.BlogSource;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class RssCrawlerService {

    public boolean isValidRssFeed(String feedUrl) {
        try {
            log.info("Validating RSS feed URL: {}", feedUrl);
            URL url = new URI(feedUrl).toURL();
            SyndFeedInput input = new SyndFeedInput();
            input.build(new XmlReader(url));
            return true;
        } catch (Exception e) {
            log.error("Invalid RSS feed URL: {}", feedUrl, e);
            return false;
        }
    }

    public List<BlogPost> crawlRssFeed(BlogSource source) {
        try {
            URL feedUrl = new URI(source.getFeedUrl()).toURL();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));
            List<BlogPost> posts = new ArrayList<>();
            for (SyndEntry entry : feed.getEntries()) {
                String content = "";
                LocalDate publishedDate = null;
                String title = entry.getTitle() == null ? "" : entry.getTitle().trim();
                String link = entry.getLink() == null ? "" : entry.getLink().trim();

                if (entry.getPublishedDate() != null) {
                    publishedDate =(LocalDate.from(entry.getPublishedDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()));
                } else if (entry.getUpdatedDate() != null) {
                    publishedDate =(LocalDate.from(entry.getUpdatedDate().toInstant()
                            .atZone(ZoneId.systemDefault())
                            .toLocalDate()));
                }

                if (entry.getDescription() != null && !entry.getDescription().getValue().isEmpty()) {
                    content = entry.getDescription().getValue();
                } else if (entry.getContents() != null && !entry.getContents().isEmpty()) {
                    content = entry.getContents().getFirst().getValue();
                }

                if (content != null && content.length() > 200) {
                    content = content.substring(0, 197) + "...";
                }

                BlogPost post = BlogPost.builder()
                        .publishedAt(publishedDate)
                        .link(link)
                        .title(title)
                        .content(content)
                        .blogSource(source)
                        .build();
                posts.add(post);
            }

            return posts;
        } catch (Exception e) {
            throw new RuntimeException("RSS 피드 크롤링 중 오류 발생: " + source.getName(), e);
        }
    }

    public Map<String, Object> testRssFeed(String feedUrl, int limit)
            throws IOException, FeedException, URISyntaxException {
        log.info("Testing RSS feed: {}", feedUrl);
        Map<String, Object> result = new HashMap<>();

        try {
            URL url = new URI(feedUrl).toURL();
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(url));
            
            result.put("title", feed.getTitle());
            result.put("description", feed.getDescription());
            result.put("link", feed.getLink());
            result.put("feedType", feed.getFeedType());
            result.put("totalEntries", feed.getEntries().size());
            
            List<Map<String, Object>> entries = feed.getEntries().stream()
                    .limit(limit)
                    .map(this::convertEntryToMap)
                    .collect(Collectors.toList());
            
            result.put("entries", entries);
            result.put("url", feedUrl);
            log.info("RSS feed test completed for: {}, found {} entries", feedUrl, feed.getEntries().size());
            
            return result;
        } catch (Exception e) {
            log.error("Error testing RSS feed: {}", feedUrl, e);
            throw e;
        }
    }

    private Map<String, Object> convertEntryToMap(SyndEntry entry) {
        Map<String, Object> entryMap = new HashMap<>();
        entryMap.put("title", entry.getTitle());
        entryMap.put("link", entry.getLink());
        entryMap.put("publishedDate", entry.getPublishedDate());
        entryMap.put("updatedDate", entry.getUpdatedDate());
        
        if (entry.getDescription() != null) {
            String content = entry.getDescription().getValue();
            if (content != null && content.length() > 200) {
                content = content.substring(0, 197) + "...";
            }
            entryMap.put("description", content);
        }
        
        if (entry.getCategories() != null && !entry.getCategories().isEmpty()) {
            List<String> categories = entry.getCategories().stream()
                    .map(SyndCategory::getName)
                    .collect(Collectors.toList());
            entryMap.put("categories", categories);
        } else {
            entryMap.put("categories", new ArrayList<String>());
        }
        
        return entryMap;
    }
}

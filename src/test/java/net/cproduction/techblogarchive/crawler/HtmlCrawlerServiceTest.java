package net.cproduction.techblogarchive.crawler;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class HtmlCrawlerServiceTest {

    @InjectMocks
    private HtmlCrawlerService htmlCrawlerService;

    @Test
    void fetchHtmlFromUrl_withValidUrl_shouldReturnHtml() {
        // Given
        String validUrl = "https://www.google.com";

        // When & Then
        assertDoesNotThrow(() -> {
            String html = htmlCrawlerService.fetchHtmlFromUrl(validUrl);
            assertNotNull(html);
            assertFalse(html.isEmpty());
            assertTrue(html.contains("<html"));
        });
    }

    @Test
    void fetchHtmlFromUrl_withInvalidUrl_shouldThrowException() {
        // Given
        String invalidUrl = "https://invalid-url-that-does-not-exist-123456789.com";

        // When & Then
        Exception exception = assertThrows(IOException.class, () -> {
            htmlCrawlerService.fetchHtmlFromUrl(invalidUrl);
        });
        
        assertTrue(exception.getMessage().contains("Invalid") || 
                   exception.getMessage().contains("Unable to resolve host") ||
                   exception.getMessage().contains("UnknownHostException"));
    }
}

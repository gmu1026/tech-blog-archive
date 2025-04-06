package net.cproduction.techblogarchive.controller;

import net.cproduction.techblogarchive.crawler.HtmlCrawlerService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(BlogSourceController.class)
class BlogSourceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HtmlCrawlerService htmlCrawlerService;

    @Test
    void fetchHtml_withValidUrl_shouldReturnHtml() throws Exception {
        // Given
        String url = "https://example.com";
        String expectedHtml = "<html><body>Example content</body></html>";
        when(htmlCrawlerService.fetchHtmlFromUrl(url)).thenReturn(expectedHtml);

        // When & Then
        mockMvc.perform(get("/api/crawler/fetch-html")
                .param("url", url))
                .andExpect(status().isOk())
                .andExpect(content().string(expectedHtml));
    }

    @Test
    void fetchHtml_withInvalidUrl_shouldReturnBadRequest() throws Exception {
        // Given
        String invalidUrl = "https://invalid-url.com";
        when(htmlCrawlerService.fetchHtmlFromUrl(invalidUrl))
                .thenThrow(new IOException("Invalid URL"));

        // When & Then
        mockMvc.perform(get("/api/crawler/fetch-html")
                .param("url", invalidUrl))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Failed to fetch HTML: Invalid URL"));
    }
}

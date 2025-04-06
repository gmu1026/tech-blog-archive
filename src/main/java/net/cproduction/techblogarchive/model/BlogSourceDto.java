package net.cproduction.techblogarchive.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogSourceDto {
    private Long id;

    @NotBlank(message = "블로그 이름은 필수입니다")
    private String name;
    
    @NotBlank(message = "블로그 URL은 필수입니다")
    @Pattern(regexp = "^(https?://)(www\\.)?[a-zA-Z0-9-]+(\\.[a-zA-Z0-9-]+)+(/?.*)?$", 
        message = "유효한 URL 형식이 아닙니다")
    private String url;
    
    @NotNull(message = "RSS 피드 사용 여부는 필수입니다")
    private Boolean useRss;
    
    private String feedUrl;
    
    // HTML 크롤링 관련 필드 (RSS가 없을 경우 사용)
    private String postSelector;
    private String titleSelector;
    private String contentSelector;
    private String linkSelector;
    private String dateSelector;
    private String dateFormat;
    private String paginationSelector;
    private String nextPageSelector;
    
    private Boolean active;
    private Long crawlDelay;
    private LocalDateTime lastCrawledAt;
}

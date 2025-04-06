package net.cproduction.techblogarchive.model;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPostDto {
    private Long id;
    private String title;
    private String content;
    private String link;
    private LocalDate publishedAt;
    private String blogSourceName;
}

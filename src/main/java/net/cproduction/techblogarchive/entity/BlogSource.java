package net.cproduction.techblogarchive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

@Entity
@Table(name = "blog_sources")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogSource {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(nullable = false)
    private String url;
    
    @Column(nullable = false)
    private Boolean useRss;
    
    @Column
    private String feedUrl;
    
    @Column
    private String postSelector;
    
    @Column
    private String titleSelector;
    
    @Column
    private String contentSelector;
    
    @Column
    private String linkSelector;
    
    @Column
    private String dateSelector;
    
    @Column
    private String dateFormat;

    @Column
    private String paginationSelector;

    @Column
    private String nextPageSelector;
    
    @Column(nullable = false)
    @Builder.Default
    private Boolean active = true;
    
    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    private LocalDateTime updatedAt;
    
    @Column
    private LocalDateTime lastCrawledAt;

    @Column
    @Builder.Default
    private Long crawlDelay = 1500L;

    @OneToMany(mappedBy = "blogSource")
    private List<BlogPost> posts = new ArrayList<>();
}

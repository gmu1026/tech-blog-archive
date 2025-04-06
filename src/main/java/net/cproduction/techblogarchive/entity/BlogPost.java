package net.cproduction.techblogarchive.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "blog_posts")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BlogPost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    private String title;

    @Column
    private String link;

    @Column
    private String content;

    @Column(updatable = false)
    private LocalDate publishedAt;

    @ManyToOne
    @JoinColumn(name = "blog_source_id")
    private BlogSource blogSource;
}

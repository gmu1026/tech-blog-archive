package net.cproduction.techblogarchive.repository;

import java.time.LocalDate;
import java.util.List;
import net.cproduction.techblogarchive.entity.BlogPost;
import net.cproduction.techblogarchive.entity.BlogSource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long> {

    boolean existsByLink(String link);

    boolean existsByTitleAndBlogSource(String title, BlogSource blogSource);

    Page<BlogPost> findByBlogSourceId(Long blogSourceId, Pageable pageable);

    Page<BlogPost> findByPublishedAtBetween(LocalDate startDate, LocalDate endDate, Pageable pageable);

    @Query("SELECT p FROM BlogPost p WHERE " +
            "LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
            "LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<BlogPost> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    long countByBlogSourceId(Long blogSourceId);

    @Query("SELECT p FROM BlogPost p WHERE p.blogSource.id = :blogSourceId " +
            "ORDER BY p.publishedAt DESC NULLS LAST")
    List<BlogPost> findLatestPostsByBlogSourceId(@Param("blogSourceId") Long blogSourceId, Pageable pageable);

    Slice<BlogPost> findAllBy(Pageable pageable);

    Slice<BlogPost> findAllByTitle(String title);

    Slice<BlogPost> findAllByTitleContaining(String title);
}

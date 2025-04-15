package net.cproduction.techblogarchive.repository;

import net.cproduction.techblogarchive.entity.BlogPost;
import net.cproduction.techblogarchive.entity.BlogSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogPostRepository extends JpaRepository<BlogPost, Long>, BlogPostCustomRepository {
    boolean existsByLink(String link);
    boolean existsByTitleAndBlogSource(String title, BlogSource blogSource);
}

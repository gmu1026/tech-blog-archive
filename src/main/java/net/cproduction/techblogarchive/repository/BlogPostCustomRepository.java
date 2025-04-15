package net.cproduction.techblogarchive.repository;

import net.cproduction.techblogarchive.model.BlogPostDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;

public interface BlogPostCustomRepository {
    Slice<BlogPostDto> findBySearchCriteria(String query, String sourceName, Boolean isRecentOnly, Pageable pageable);
}

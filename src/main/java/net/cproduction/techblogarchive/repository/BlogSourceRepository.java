package net.cproduction.techblogarchive.repository;

import java.util.List;
import java.util.Optional;
import net.cproduction.techblogarchive.entity.BlogSource;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BlogSourceRepository extends JpaRepository<BlogSource, Long> {
    Optional<BlogSource> findByName(String name);

    Optional<BlogSource> findByUrl(String url);

    List<BlogSource> findByActiveTrue();

    List<BlogSource> findByActiveTrueAndUseRssTrue();

    List<BlogSource> findByActiveTrueAndUseRssFalse();

    List<BlogSource> findAllByUseRssIsFalse();
}

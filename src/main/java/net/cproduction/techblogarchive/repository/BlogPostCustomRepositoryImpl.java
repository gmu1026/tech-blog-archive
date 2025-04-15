package net.cproduction.techblogarchive.repository;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import net.cproduction.techblogarchive.entity.QBlogPost;
import net.cproduction.techblogarchive.entity.QBlogSource;
import net.cproduction.techblogarchive.model.BlogPostDto;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

@RequiredArgsConstructor
@Repository
public class BlogPostCustomRepositoryImpl implements BlogPostCustomRepository {
    private final JPAQueryFactory queryFactory;

    @Override
    public Slice<BlogPostDto> findBySearchCriteria(String query, String sourceName, Boolean isRecentOnly,
                                                   Pageable pageable) {
        QBlogPost blogPost = QBlogPost.blogPost;
        QBlogSource blogSource = QBlogSource.blogSource;

        BooleanBuilder predicate = new BooleanBuilder();
        if (StringUtils.hasText(query)) {
            predicate.and(blogPost.title.contains(query));
        }

        if (StringUtils.hasText(sourceName)) {
            predicate.and(blogPost.blogSource.name.eq(sourceName));
        }

        if (isRecentOnly != null && isRecentOnly) {
            LocalDate sinceDate = LocalDate.now().minusDays(7);
            predicate.and(blogPost.publishedAt.goe(sinceDate));
        }

        List<BlogPostDto> content = queryFactory.select(
                        Projections.constructor(BlogPostDto.class,
                                blogPost.id,
                                blogPost.title,
                                blogPost.content,
                                blogPost.link,
                                blogPost.publishedAt,
                                blogPost.blogSource.name))
                .from(blogPost)
                .leftJoin(blogPost.blogSource, blogSource)
                .where(predicate)
                .orderBy(blogPost.publishedAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize() + 1)
                .fetch();

        boolean hasNext = false;
        if (content.size() > pageable.getPageSize()) {
            content.removeLast();
            hasNext = true;
        }

        return new SliceImpl<>(content, pageable, hasNext);
    }
}

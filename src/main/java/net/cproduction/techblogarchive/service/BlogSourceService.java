package net.cproduction.techblogarchive.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.entity.BlogSource;
import net.cproduction.techblogarchive.model.BlogSourceDto;
import net.cproduction.techblogarchive.repository.BlogSourceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class BlogSourceService {
    private final BlogSourceRepository blogSourceRepository;

    @Transactional(readOnly = true)
    public List<BlogSourceDto> getAllBlogSources() {
        return blogSourceRepository.findAll().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<BlogSourceDto> getActiveBlogSources() {
        return blogSourceRepository.findByActiveTrue().stream()
                .map(this::convertToDto)
                .toList();
    }

    @Transactional(readOnly = true)
    public Optional<BlogSourceDto> getBlogSourceById(Long id) {
        return blogSourceRepository.findById(id)
                .map(this::convertToDto);
    }

    @Transactional
    public BlogSourceDto createBlogSource(BlogSourceDto blogSourceDto) {
        validateRequiredFields(blogSourceDto);
        checkForDuplicates(blogSourceDto, null);
        validateCrawlingConfig(blogSourceDto);

        BlogSource blogSource = convertToEntity(blogSourceDto);
        blogSource.setActive(true);

        BlogSource savedBlogSource = blogSourceRepository.save(blogSource);
        log.info("새 블로그 소스 생성: {}", savedBlogSource.getName());

        return convertToDto(savedBlogSource);
    }

    @Transactional
    public Optional<BlogSourceDto> updateBlogSource(Long id, BlogSourceDto blogSourceDto) {
        validateRequiredFields(blogSourceDto);

        return blogSourceRepository.findById(id)
                .map(existingSource -> {
                    checkForDuplicates(blogSourceDto, id);
                    validateCrawlingConfig(blogSourceDto);
                    updateSourceFields(existingSource, blogSourceDto);

                    log.info("블로그 소스 업데이트: {}", existingSource.getName());

                    return convertToDto(existingSource);
                });
    }

    @Transactional
    public Optional<BlogSourceDto> updateBlogSourceActiveStatus(Long id, boolean active) {
        return blogSourceRepository.findById(id)
                .map(blogSource -> {
                    blogSource.setActive(active);
                    BlogSource savedSource = blogSourceRepository.save(blogSource);
                    log.info("블로그 소스 '{}' 활성화 상태 변경: {}", savedSource.getName(), active);
                    return convertToDto(savedSource);
                });
    }

    @Transactional
    public boolean deleteBlogSource(Long id) {
        if (blogSourceRepository.existsById(id)) {
            blogSourceRepository.deleteById(id);
            log.info("블로그 소스 삭제: ID {}", id);
            return true;
        }
        log.warn("삭제 실패: 블로그 소스 ID {}를 찾을 수 없습니다", id);

        return false;
    }

    private BlogSourceDto convertToDto(BlogSource blogSource) {
        return BlogSourceDto.builder()
                .id(blogSource.getId())
                .name(blogSource.getName())
                .url(blogSource.getUrl())
                .useRss(blogSource.getUseRss())
                .feedUrl(blogSource.getFeedUrl())
                .postSelector(blogSource.getPostSelector())
                .titleSelector(blogSource.getTitleSelector())
                .contentSelector(blogSource.getContentSelector())
                .linkSelector(blogSource.getLinkSelector())
                .dateSelector(blogSource.getDateSelector())
                .dateFormat(blogSource.getDateFormat())
                .paginationSelector(blogSource.getPaginationSelector())
                .nextPageSelector(blogSource.getNextPageSelector())
                .active(blogSource.getActive())
                .crawlDelay(blogSource.getCrawlDelay())
                .lastCrawledAt(blogSource.getLastCrawledAt())
                .build();
    }

    private BlogSource convertToEntity(BlogSourceDto dto) {
        return BlogSource.builder()
                .name(dto.getName())
                .url(dto.getUrl())
                .useRss(dto.getUseRss())
                .feedUrl(dto.getFeedUrl())
                .postSelector(dto.getPostSelector())
                .titleSelector(dto.getTitleSelector())
                .contentSelector(dto.getContentSelector())
                .linkSelector(dto.getLinkSelector())
                .dateSelector(dto.getDateSelector())
                .dateFormat(dto.getDateFormat())
                .paginationSelector(dto.getPaginationSelector())
                .nextPageSelector(dto.getNextPageSelector())
                .active(true)
                .crawlDelay(dto.getCrawlDelay() != null ? dto.getCrawlDelay() : 1500L)
                .build();
    }

    private void updateSourceFields(BlogSource existingSource, BlogSourceDto dto) {
        existingSource.setName(dto.getName());
        existingSource.setUrl(dto.getUrl());
        existingSource.setUseRss(dto.getUseRss());
        existingSource.setFeedUrl(dto.getFeedUrl());
        existingSource.setPostSelector(dto.getPostSelector());
        existingSource.setTitleSelector(dto.getTitleSelector());
        existingSource.setContentSelector(dto.getContentSelector());
        existingSource.setLinkSelector(dto.getLinkSelector());
        existingSource.setDateSelector(dto.getDateSelector());
        existingSource.setDateFormat(dto.getDateFormat());
        existingSource.setPaginationSelector(dto.getPaginationSelector());
        existingSource.setNextPageSelector(dto.getNextPageSelector());

        if (dto.getCrawlDelay() != null) {
            existingSource.setCrawlDelay(dto.getCrawlDelay());
        }
    }

    private void validateRequiredFields(BlogSourceDto dto) {
        if (!StringUtils.hasText(dto.getName())) {
            throw new IllegalArgumentException("블로그 이름은 필수입니다.");
        }

        if (!StringUtils.hasText(dto.getUrl())) {
            throw new IllegalArgumentException("블로그 URL은 필수입니다.");
        }

        if (dto.getUseRss() == null) {
            throw new IllegalArgumentException("RSS 피드 사용 여부는 필수입니다.");
        }
    }

    private void checkForDuplicates(BlogSourceDto dto, Long currentId) {
        Optional<BlogSource> existingSourceByUrl = blogSourceRepository.findByUrl(dto.getUrl());
        if (existingSourceByUrl.isPresent() &&
                (currentId == null || !existingSourceByUrl.get().getId().equals(currentId))) {
            throw new IllegalArgumentException("이미 등록된 URL입니다: " + dto.getUrl());
        }

        Optional<BlogSource> existingSourceByName = blogSourceRepository.findByName(dto.getName());
        if (existingSourceByName.isPresent() &&
                (currentId == null || !existingSourceByName.get().getId().equals(currentId))) {
            throw new IllegalArgumentException("이미 등록된 블로그 이름입니다: " + dto.getName());
        }
    }

    private void validateCrawlingConfig(BlogSourceDto dto) {
        if (Boolean.FALSE.equals(dto.getUseRss())) {
            validateHtmlSelectors(dto);
        } else if (!StringUtils.hasText(dto.getFeedUrl())) {
            throw new IllegalArgumentException("RSS 피드를 사용하는 경우 피드 URL은 필수입니다.");
        }
    }

    private void validateHtmlSelectors(BlogSourceDto dto) {
        if (!StringUtils.hasText(dto.getPostSelector())) {
            throw new IllegalArgumentException("HTML 크롤링을 사용하는 경우 포스트 선택자는 필수입니다.");
        }

        if (!StringUtils.hasText(dto.getTitleSelector())) {
            throw new IllegalArgumentException("HTML 크롤링을 사용하는 경우 제목 선택자는 필수입니다.");
        }

        if (!StringUtils.hasText(dto.getLinkSelector())) {
            throw new IllegalArgumentException("HTML 크롤링을 사용하는 경우 링크 선택자는 필수입니다.");
        }
    }
}

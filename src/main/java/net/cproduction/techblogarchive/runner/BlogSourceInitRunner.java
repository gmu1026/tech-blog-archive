package net.cproduction.techblogarchive.runner;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.cproduction.techblogarchive.entity.BlogSource;
import net.cproduction.techblogarchive.repository.BlogSourceRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Component
public class BlogSourceInitRunner implements ApplicationRunner {
    private final BlogSourceRepository blogSourceRepository;

    @Transactional
    @Override
    public void run(ApplicationArguments args) throws Exception {
        BlogSource lineJp = BlogSource.builder()
                .name("LINE JP")
                .url("https://techblog.lycorp.co.jp/ko")
                .feedUrl("https://techblog.lycorp.co.jp/ko/feed/index.xml")
                .useRss(true)
                .build();

        BlogSource naverD2 = BlogSource.builder()
                .name("NAVER D2")
                .url("https://d2.naver.com")
                .feedUrl("https://d2.naver.com/d2.atom")
                .useRss(true)
                .build();

        BlogSource kakaoPay = BlogSource.builder()
                .name("KAKAO PAY")
                .url("https://tech.kakaopay.com")
                .postSelector("._postListItem_1cl5f_66")
                .contentSelector("._postInfo_1cl5f_99 p")
                .titleSelector("._postInfo_1cl5f_99 strong")
                .linkSelector("._postListItem_1cl5f_66 a")
                .paginationSelector(".pagination")
                .nextPageSelector(".pagination .icon:not(.disabled):last-child")
                .dateFormat("yyyy. M. d")
                .dateSelector("._postInfo_1cl5f_99 time")
                .useRss(false)
                .build();

        List<BlogSource> sources = List.of(lineJp, naverD2, kakaoPay);
        blogSourceRepository.saveAll(sources);

        log.info("{} 개의 블로그 소스를 저장했습니다.", sources.size());
    }
}

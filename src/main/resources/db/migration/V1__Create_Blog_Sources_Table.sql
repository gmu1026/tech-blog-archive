-- 블로그 소스 테이블 생성
CREATE TABLE blog_sources (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE,
    url VARCHAR(2000) NOT NULL UNIQUE,
    use_rss BOOLEAN NOT NULL,
    feed_url VARCHAR(2000),
    post_selector VARCHAR(255),
    title_selector VARCHAR(255),
    content_selector VARCHAR(255),
    link_selector VARCHAR(255),
    date_selector VARCHAR(255),
    date_format VARCHAR(100),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    last_crawled_at TIMESTAMP
);

-- 인덱스 생성
CREATE INDEX idx_blog_sources_active ON blog_sources(active);
CREATE INDEX idx_blog_sources_name ON blog_sources(name);
CREATE INDEX idx_blog_sources_url ON blog_sources(url);
CREATE INDEX idx_blog_sources_last_crawled_at ON blog_sources(last_crawled_at);

-- 초기 데이터 삽입
INSERT INTO blog_sources (name, url, use_rss, feed_url, active)
VALUES ('NAVER D2', 'https://d2.naver.com/home', true, 'https://d2.naver.com/d2.atom', true);

INSERT INTO blog_sources (name, url, use_rss, feed_url, active)
VALUES ('우아한형제들 기술 블로그', 'https://techblog.woowahan.com/', true, 'https://techblog.woowahan.com/feed/', true);

INSERT INTO blog_sources (name, url, use_rss, feed_url, active)
VALUES ('카카오 테크', 'https://tech.kakao.com/', true, 'https://tech.kakao.com/feed/', true);

INSERT INTO blog_sources (name, url, use_rss, feed_url, active)
VALUES ('라인 엔지니어링', 'https://engineering.linecorp.com/ko/blog/', true, 'https://engineering.linecorp.com/ko/feed/', true);

INSERT INTO blog_sources (name, url, use_rss, feed_url, active)
VALUES ('토스 테크', 'https://toss.tech/', true, 'https://toss.tech/rss.xml', true);

-- HTML 크롤링이 필요한 블로그 소스 예시
INSERT INTO blog_sources (name, url, use_rss, post_selector, title_selector, content_selector, link_selector, date_selector, date_format, active)
VALUES ('샘플 HTML 크롤링 블로그', 'https://example.com/blog', false, 
       '.post-item', '.post-title', '.post-content', '.post-title a', '.post-date', 'yyyy-MM-dd', true);

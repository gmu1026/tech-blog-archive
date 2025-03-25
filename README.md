# Tech Blog Archive

IT 기업 개발 블로그 아카이브 및 알림 시스템입니다.

## 프로젝트 개요

국내 IT 기업들의 개발 블로그에서 신간 글을 자동으로 수집하고, 기존 아카이브를 정리하여 한눈에 볼 수 있도록 제공하는 웹 애플리케이션입니다.

### 주요 기능

- 기업 개발 블로그의 RSS/사이트 크롤링을 통한 자동 수집
- 신간 글 알림 기능 (Push, Email 등)
- 키워드 검색 및 태그 분류
- 유저가 구독할 블로그 선택 가능
- 아카이브에서 과거 글 검색 가능

## 기술 스택

- **백엔드**: Spring Boot 3, Spring Batch, JPA
- **데이터베이스**: PostgreSQL, Redis, Elasticsearch
- **프론트엔드**: React, Next.js, Tailwind CSS
- **크롤링**: Jsoup, Rome (RSS)
- **배포/운영**: Docker, Kubernetes, AWS

## 개발 환경 설정

### 요구사항

- JDK 21
- Docker & Docker Compose
- PostgreSQL
- Redis
- Elasticsearch

### 시작하기

1. 저장소 클론
```bash
git clone https://github.com/gmu1026/tech-blog-archive.git
cd tech-blog-archive
```

2. .env 파일 생성 (.env.sample 참고)
```bash
cp .env.sample .env
# .env 파일을 편집하여 필요한 환경 변수 설정
```

3. Docker 컨테이너 실행
```bash
docker-compose up -d
```

4. 애플리케이션 실행
```bash
./gradlew bootRun
```

## 라이센스

MIT License

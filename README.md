# 챙김노트 (chaenggim-note)

제약 영업사원(MR) 한 명이 의료진별로 **받은 질문과 약속**을 기록하는 개인 업무 도구입니다.
질문은 미답변 → 확인 중 → 답변 준비됨 → 전달 완료로 추적하고, 답변에는 출처를 꼭 남깁니다.
다음 방문 전에 홈과 의료진 상세의 **방문 전 준비**에서 이번에 챙길 것을 한 번에 봅니다.

> 모든 의료진·병원 이름과 기록은 가상 데이터입니다. 로그인 없이 로컬에서 실행하는 1인용 도구입니다.

## 실행 방법

필요한 것: JDK 17, Node.js 20 이상(npm 포함)

### 백엔드 (저장소 루트)

```bash
./gradlew bootRun          # Windows: gradlew.bat bootRun
```

- http://localhost:8080 에서 실행됩니다. 기본 DB는 H2 파일 DB(`./data/`)입니다.
- 처음 실행할 때 의료진이 0명이면 가상 데이터(의료진 4·방문 4·질문 5·약속 4)가 자동으로 들어갑니다.
- 화면 홈의 `가상 데이터로 초기화` 버튼(또는 `POST /api/admin/reset`)으로 언제든 처음 상태로 되돌릴 수 있습니다.
- H2 콘솔: http://localhost:8080/h2-console — JDBC URL `jdbc:h2:file:./data/chaenggim;MODE=MySQL;DATABASE_TO_LOWER=TRUE;AUTO_SERVER=TRUE`, 사용자 `sa`, 비밀번호 없음

### 프론트 (`frontend/`)

```bash
cd frontend
npm install
npm run dev
```

- **http://localhost:5174** 를 브라우저로 엽니다. `/api` 요청은 Vite가 백엔드(8080)로 넘겨 줍니다.
- 백엔드를 먼저 띄워 두세요.

### MySQL로 바꾸기

1. MySQL 8에서 DB와 계정을 만듭니다(직접 실행).
   ```sql
   CREATE DATABASE chaenggim DEFAULT CHARACTER SET utf8mb4;
   CREATE USER 'chaenggim'@'localhost' IDENTIFIED BY 'chaenggim';
   GRANT ALL PRIVILEGES ON chaenggim.* TO 'chaenggim'@'localhost';
   ```
2. `mysql` 프로필로 실행합니다. 계정은 환경 변수 `DB_USERNAME`, `DB_PASSWORD`로 바꿀 수 있습니다(기본값 `chaenggim`).
   ```bash
   ./gradlew bootRun --args='--spring.profiles.active=mysql'
   ```

설정은 `src/main/resources/application-mysql.yml`, 스키마는 `src/main/resources/db/schema-mysql.sql`입니다.
MySQL 경로는 자동 테스트로 검증하지 않습니다(테스트는 H2).

### 검사

```bash
./gradlew test                              # 백엔드 테스트 (도메인·리포지토리·서비스·API)
cd frontend && npm run lint && npm run build  # 프론트 검사
```

## 기술 스택

| 구분 | 사용 |
| --- | --- |
| 백엔드 | Java 17, Spring Boot 3.3.5 (Web, Data JPA, Validation), Gradle, Lombok |
| DB | H2 (기본), MySQL 8 (프로필 전환) |
| 프론트 | React 18, TypeScript 5.6, Vite 5, React Router 6, TanStack Query 5 |
| 스타일 | CSS 파일 하나(`frontend/src/styles/app.css`), Pretendard 웹폰트. CSS 프레임워크·UI 라이브러리 없음 |
| 응답 형식 | 모든 API 응답은 `{ success, data, error }` |

## 화면

| 경로 | 화면 |
| --- | --- |
| `/` | 홈 "오늘 챙길 것": 기한 지난 약속, 오늘까지인 약속, 이번 주 방문 예정, 질문 현황 |
| `/doctors` | 의료진 목록·검색 |
| `/doctors/new`, `/doctors/:id/edit` | 의료진 등록·수정 |
| `/doctors/:id` | 의료진 상세: 방문 전 준비, 방문 기록, 전달 완료한 질문, 완료한 약속 |
| `/doctors/:id/visits/new`, `/visits/:id/edit` | 방문 기록 작성(질문·약속 각 최대 10개, 질문마다 제품 선택)·수정 |
| `/questions`, `/questions/:id` | 질문함(상태 탭 + 제품 필터), 질문 상세(확인 시작·답변·전달 완료) |
| `/commitments/:id/edit` | 약속 수정 |

휴대폰(768px 미만)에서는 하단 탭, PC에서는 왼쪽 메뉴로 바뀝니다.

## 문서

설계 문서는 `docs/`에 있고, 구현은 이 문서를 기준으로 합니다. 작업 규칙은 [`CLAUDE.md`](CLAUDE.md)에 있습니다.

| 파일 | 내용 |
| --- | --- |
| [`docs/00_개요.md`](docs/00_개요.md) | 목적, 범위, 하지 않는 것 |
| [`docs/01_결정사항.md`](docs/01_결정사항.md) | 확정된 결정 |
| [`docs/02_도메인_규칙.md`](docs/02_도메인_규칙.md) | 엔티티, 상태 전이, 검증 메시지, 예외 |
| [`docs/03_DB_스키마.md`](docs/03_DB_스키마.md) | H2·MySQL DDL |
| [`docs/04_백엔드_설정.md`](docs/04_백엔드_설정.md) | 빌드·설정·패키지 |
| [`docs/05_API_명세.md`](docs/05_API_명세.md) | 엔드포인트, 요청·응답 |
| [`docs/06_화면_명세.md`](docs/06_화면_명세.md) | 화면 구성과 문구 |
| [`docs/07_프론트_구현가이드.md`](docs/07_프론트_구현가이드.md) | 프론트 구조, 디자인 결정 기록 |
| [`docs/08_시드데이터.md`](docs/08_시드데이터.md) | 가상 데이터 |
| [`docs/09_테스트_명세.md`](docs/09_테스트_명세.md) | 백엔드 테스트, 수동 점검 목록 |
| [`docs/10_작업순서.md`](docs/10_작업순서.md) | 작업 순서와 완료 기준 |
| [`docs/11_코딩_컨벤션.md`](docs/11_코딩_컨벤션.md) | 백엔드 패키지 구조·레이어 규칙 |

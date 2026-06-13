# 그린대학교(Green University) 학사관리 시스템 - MSA
<br>

## 프로젝트 소개

그린대학교(Green University) 학사관리 시스템 2차 프로젝트는 1차에서 구축한 MyBatis 기반 단일 애플리케이션(MA)을 JPA + MSA(Microservice Architecture) 구조로 전환하는 프로젝트입니다. 회원, 인증, 강의, 학과, 수강신청, 출결, 성적, 학사일정, 공지, 알림 등 학사 운영 기능을 도메인 단위의 독립된 서비스로 재구성했습니다.

본 프로젝트는 그린컴퓨터아트학원 2차 팀 프로젝트로, 2026.04.28 ~ 2026.06.10 (약 6주, 마지막 주는 테스트 및 발표 준비) 동안 비전공자 4인이 풀스택으로 진행했습니다. 기존 모놀리식 구조를 4개의 독립 서비스(인증, 회원, 학사 핵심, 학사 행정)와 API Gateway로 분리했으며, Kafka와 Redis를 통한 서비스 간 데이터 정합성 처리를 적용했습니다.
<br>
<br>

## Repository 구성

| 구분 | 저장소 |
|---|---|
| Backend (현재 저장소, MSA 멀티모듈) | [be-msa-green-uni](https://github.com/green-uni/be-msa-green-uni) |
| Frontend | [fe-msa-green-uni](https://github.com/green-uni/fe-msa-green-uni) |
| 배포 manifest | [msa-green-uni-manifest](https://github.com/green-uni/msa-green-uni-manifest) |
<br>

### 모듈 구성

| 모듈 | 타이틀 | 도메인 / 역할 |
|---|---|---|
| gateway | - | API Gateway (포트 8000) |
| auth-service | 인증 | 인증 (포트 8080) |
| member-service | 회원 | 회원 (포트 8081) |
| academic-service | 학사 행정 | 학사일정, 공지, 알림 (포트 8082) |
| core-service | 학사 핵심 | 강의/수강신청/학과/출결/성적/등록금/장학금 (포트 8083) |
| common | - | 공통 모듈 |
| docker-kafka, docker-redis | - | 인프라 설정 |
<br>

## 기술 스택

| 구분 | 내용 |
|---|---|
| Language / Framework | Java 21, Spring Boot 4.0.4, Spring Security |
| Database | MySQL, Redis, JPA(Hibernate), MyBatis |
| 메시징 / 실시간 | Kafka, WebSocket |
| 서비스 간 통신 | Spring Cloud Gateway, OpenFeign |
| 인증 | JWT (JJWT) |
| 기타 | JavaMailSender, Apache POI |
| Frontend | Vue 3, Vite, Pinia, Vue Router, Axios, SCSS |
| 배포/운영 환경 (활용) | Docker, Jenkins, Harbor, ArgoCD, Kubernetes |

> 협업 도구: Git, GitHub, Notion, Jira, Slack, Google Sheet, ERD Cloud, draw.io, Figma
<br>

## 시스템 아키텍처

```
                [Vue Client]
                     │
                     ▼
            [Gateway :8000]
        (JWT 인증 검증 / 라우팅)
                     │
   ┌─────────┬───────┴───────┬──────────────┐
   ▼         ▼                ▼              ▼
auth-service member-service  academic-service core-service
  :8080         :8081            :8082          :8083
   │             │                 │              │
 gu_auth     gu_member         gu_academic     gu_core
(완전 분리된 개별 MySQL DB)

   └─────────────┴── Kafka (Outbox 패턴) ─────────┘

Redis: Refresh Token 저장 (auth-service, Gateway)
member-service → core-service : OpenFeign (학점/GPA 조회)
academic-service ↔ Client : WebSocket (실시간 알림)
```

서비스별로 완전히 분리된 MySQL DB를 사용하며, 도메인 간 데이터 동기화는 Kafka Outbox 패턴으로 처리합니다. (예: 회원 정보 변경 시 auth/member-service에서 이벤트 발행 → core-service의 캐시 테이블 갱신)


### CI/CD 파이프라인

배포 파이프라인은 프로젝트 진행 중에 구성되었으며, 이후 아래 흐름으로 배포가 이루어졌습니다.

```
Dev repo (push) → Deploy repo → Webhook → Jenkins (Kaniko 빌드) → Harbor (이미지 등록)
→ Image Updater → Manifest repo 업데이트 → ArgoCD → Kubernetes 배포
```

GitOps 방식으로, Harbor에 이미지가 등록되면 ArgoCD가 Manifest repo의 변경을 감지해 자동 배포합니다.
<br>
<br>

## 패키지 / 서비스별 구조

각 서비스는 `application` 패키지 아래에 도메인별로 Controller/Service/Repository/model이 구성되며, 서비스 간 이벤트 처리를 위한 `kafka` 패키지와 JPA `entity` 패키지를 공통적으로 가집니다.

**auth-service**
- `application/auth` - 로그인/로그아웃/토큰 재발급
- `application/email` - 이메일 인증, 비밀번호 재설정
- `kafka` - 회원 생성/수정/삭제 이벤트 발행·구독

**member-service**
- `application/member` - 프로필 조회/수정
- `application/admin` - 관리자 회원 관리 (엑셀 일괄등록 포함)
- `application/student`, `application/professor` - 학생/교수 정보
- `application/status` - 휴복학 신청
- `application/major` - 학과 정보, 전공/복수전공 변경 신청
- `application/schedule` - 학사일정 캐시 (전과/복수전공 신청 시기 검증용, academic-service 데이터를 Kafka로 동기화)
- `client` - OpenFeign (core-service GPA 조회)
- `kafka`, `entity` (member/student/professor/cache)

**academic-service**
- `application/schedule` - 학사일정
- `application/announcement` - 공지사항
- `application/notification` - 알림
- `websocket`, `kafka`, `entity`

**core-service**
- `application/lecture` - 강의 (하위 `evaluationController`로 강의평가 포함)
- `application/course` - 수강신청
- `application/major` - 학과
- `application/attendance` - 출결
- `application/grade` - 성적
- `application/tuition` - 등록금
- `application/scholarship` - 장학금
- `entity/cache` - 회원/학생/교수/학과 캐시 테이블
- `kafka`, `repository`, `scheduleValidator`, MyBatis `mapper`

**gateway**
- `security`, `filter` - JWT 인증 검증, URL prefix(`/admin`, `/professor`, `/student`) 기반 역할(Role) 권한 검사 후 라우팅
<br>

## 주요 기능

### auth-service
- 로그인 (학생/교수용, 관리자용 엔드포인트 분리)
- 로그아웃
- 액세스 토큰 재발급
- 비밀번호 변경
- 최초 로그인 시 비밀번호 변경
- 비밀번호 재설정 (이메일 인증 기반)
- 이메일 인증 코드 발송 및 검증

### member-service
- 내 정보 조회 및 수정 (학생/교수/관리자 공통)
- (관리자) 회원 통계 조회 (인원수)
- (관리자) 회원 목록/상세/변경 이력 조회 (학생/교수/관리자)
- (관리자) 회원 등록 (개별 등록 + 엑셀 일괄등록, 템플릿 다운로드)
- (관리자) 회원 상태 변경
- (관리자) 전공/복수전공 변경 신청, 휴복학 신청 대기 목록 조회 및 처리 (첨부파일 다운로드 포함)
- (학생) 전공/복수전공 변경 신청 / 취소, 신청 이력 조회
- (학생) 휴복학 신청 / 취소, 신청 이력 조회
- (학생) 본인 신청 현황 대시보드 조회
- 학과/단과대학 목록 조회 (캐시)

### core-service

**출결**
- (교수) 내 강의 목록/상세 조회, 출석 세션 시작/종료
- (교수) QR 코드 토큰 실시간 발급 (SSE, 자동 갱신)
- (교수) 세션 목록 및 날짜별 출석부 조회, 출결 상태 수정
- (교수) 휴강 처리/이력 조회, 보강 세션 개설
- (학생) QR 스캔 출석 체크, 내 출결 내역 조회

**강의**
- 강의 전체 목록/상세 조회 (연도별)
- (교수) 강의 개설/수정/삭제, 내 강의/시간표/오늘 강의 조회, 건물별 강의실 조회
- (관리자) 강의 승인/반려, 휴강 처리, 담당 교수 변경, 자동 휴강/반려(스케줄러)
- (학생) 내 수강 강의/시간표 조회
- 강의평가: (학생) 작성 및 조회, (교수) 본인 강의 평가 조회

**수강신청**
- (학생) 신청 상태 조회, 수강 가능 강의 조회, 내 신청 목록 조회, 신청/취소 (재학 상태 기반 제한)

**학과**
- (관리자) 학과 등록/수정, 목록/상세 조회, 단과대/교수/건물 목록 조회, 소속 학생 존재 여부 확인
- 학과 간단 목록 조회 (전체)

**성적**
- (교수) 담당 강의/성적 조회 및 입력/수정, 이의제기 처리
- (학생) 내 성적 조회, 이의제기 신청 및 조회
- (서비스 간) GPA 조회 API - member-service OpenFeign 연동

**등록금**
- (관리자) 목록 조회, 미납자 안내 메일, 납부 확인, 정책 관리, 학기별 자동 생성(스케줄러)
- (학생) 내 등록금 조회, 납부 요청

**장학금**
- (관리자) 지급 목록 조회
- (학생) 내 장학금 조회
- 성적 기준 자동 산정 (스케줄러)

### academic-service
- **알림**: 목록/안읽음 개수 조회, 읽음 처리, 삭제, 실시간 푸시 (WebSocket)
- **공지사항**: 목록/상세 조회 (로그인/비로그인 분리), (관리자) 등록/수정/삭제
- **학사일정**: 목록/활성 일정/배너 조회, (관리자) 등록/수정/삭제, 변경 시 알림 발송 + Kafka 이벤트 (member-service 캐시 동기화)

### gateway
- API 경로 기반 라우팅 (auth/member/core/academic-service, 파일 경로 분리)
- JWT 토큰 인증 필터
- URL prefix 기반 역할(Role) 권한 검사
- 인증/인가 실패(401/403) 커스텀 응답, 전역 예외 처리
<br>

## 팀원 소개 및 역할 분담

| 이름 | 담당 도메인 |
|---|---|
| [@chanmi24](https://github.com/chanmi24-cyber)(팀장) | 강의 (core-service) / 학사일정 / 알림 (academic-service) |
| [@k28y](https://github.com/k28y) | 인증 (auth-service) / 회원 (member-service) / 게이트웨이 (gateway) |
| [@JunLee Lim](https://github.com/junlee-lim) | 학과 / 수강신청 / 등록금 / 장학금 (core-service) |
| [@YouYoungGeun](https://github.com/qwazsx1346-cyber) | 출결 / 성적 (core-service) / 공지 (academic-service) |

> 전원 풀스택(Backend + Frontend), 담당 도메인 기준 분담.
<br>

## 실행 방법

### 1. 사전 요구사항
- Java 21
- MySQL, Redis, Kafka
- Node.js (프론트엔드용)

### 2. 인프라 실행 (Kafka, Redis)
```bash
docker-compose -f docker-kafka/docker-compose.yml up -d
docker-compose -f docker-redis/docker-compose.yml up -d
```

### 3. 환경변수 설정
각 서비스 루트에 환경변수 파일(`.env`)을 생성하고 DB 접속 정보, 메일 발송 계정, JWT 시크릿 키, 파일 저장 경로, 서비스 간 통신 포트 등을 설정합니다. DB는 서비스별로 분리되어 있으며, 최초 실행 시 자동 생성됩니다.

### 4. 서비스 실행
```bash
./gradlew :gateway:bootRun          # 8000
./gradlew :auth-service:bootRun     # 8080
./gradlew :member-service:bootRun   # 8081
./gradlew :academic-service:bootRun # 8082
./gradlew :core-service:bootRun     # 8083
```

### 5. 프론트엔드 실행
```bash
git clone https://github.com/green-uni/fe-msa-green-uni.git
cd fe-msa-green-uni
npm install
npm run dev
```
<br>

## 프로젝트 회고

2차 프로젝트는 1차에서 도입한 개선 사항(도메인별 브랜치 분기, 상세한 커밋 메시지)을 실제로 적용하며 진행했고, 협업 측면에서 효과를 확인할 수 있었습니다.

기술적으로는 JPA 도입과 MSA 전환 과정에서 다음과 같은 어려움을 겪었습니다.

- 서비스 간 데이터 동기화: Kafka Outbox 패턴을 도입했으나, 이벤트 필드(eventType 등) 설정을 누락하면 컨슈머가 메시지를 조용히 무시하는 문제가 있어 디버깅에 어려움을 겪었습니다. 이후 이벤트 필드를 명시적으로 설정하는 규칙을 팀 내에 공유했습니다.
- 인증/보안: JWT 만료 시 500 에러가 반환되던 문제를 401로 수정하고, Refresh Token 쿠키 경로를 로그인/로그아웃 엔드포인트 간 일치시키는 등 보안 관련 세부 사항을 다수 점검했습니다.
- 배포 환경: 배포 파이프라인이 개발 중간 시점에 구성되면서, 기존 개발 환경과 설정이 맞지 않아 일부를 재정비하는 과정이 있었습니다.

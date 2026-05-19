# 그린uni 학사관리 시스템 - CLAUDE.md

> 그린uni 2차 프로젝트의 컨텍스트와 규칙 모음. 코드 작성 전 반드시 확인.

---

## 📋 프로젝트 개요

| 항목 | 내용 |
|------|------|
| 프로젝트 | 그린uni 학사관리 시스템 2차 |
| 팀 | 4명 (Vue + Spring Boot 풀스택, 전원 입문자) |
| 기간 | 약 4.5주 (2026.04 말 ~ 2026.06 초) |
| 핵심 변경 | 1차(MyBatis + MA) → **2차(JPA + MSA) 변환 필수** |

**본인(유영근) 담당**: 출석 / 성적 / 공지사항

### 응답 스타일
- 쉬운 말로 설명, 약어는 원어+뜻 함께 표기 (예: MSA(Microservices Architecture))
- 개발 관례는 **왜 그렇게 하는지** 이유 포함

---

### DB 분리 핵심 규칙
- **캐시 테이블**: 자주 안 바뀌는 데이터(이름, 학번 등)는 각 서비스 DB에 캐시 테이블로 저장
    - 예: academic_db의 `member_cache`
    - Kafka로 동기화
    - **캐시 테이블에는 FK 걸지 말 것** (동기화 타이밍 리스크) → 논리적 참조만
- **API 호출**: 실시간 확인 필요 시(등록금 납부, 재학 상태 등)
- 서비스 간 내부 통신은 `/api/internal/`로, Gateway에서 외부 차단


## 📂 폴더 구조 (각 서버 모듈)

```
com.green.{서비스명}
├── application/              ← 비즈니스 로직 (도메인별 분리)
│   └── lecture/
│       ├── model/            ← DTO (Req/Res)
│       ├── AdminLectureController       ← admin 권한용
│       ├── ProfessorLectureController   ← professor 권한용
│       ├── StudentLectureController     ← student 권한용
│       ├── LectureController            ← 권한 인가 불필요
│       ├── LectureService               ← 모든 로직 여기서
│       └── LectureRepository
├── entity/                   ← 이 서버 DB 엔터티 전부 (캐시 테이블 포함)
├── enumcode/                 ← 모듈 내 enum
├── exception/                ← 모듈 내 ErrorCode (공용은 common에)
├── kafka/                    ← 캐시 테이블 동기화 consumer
└── repository/               ← 여러 도메인이 공유하는 Repository만
```

> ❌ **다른 팀원의 도메인 폴더는 절대 손대지 말 것.** 손대야 한다면 담당자에게 사전 공유.

---

## 🛣 API URL 규칙

### 형식
```
/api/{서버명}/{권한}/{도메인}/{리소스ID}
```

### 원칙
| 규칙 | ✅ Good | ❌ Bad |
|------|--------|--------|
| 소문자 + 케밥 케이스 | `/api/lecture-rooms` | `/api/lectureRooms` |
| 명사 복수형 | `/api/lectures` | `/api/lecture` |
| 동사 금지 (Method가 동사 역할) | `DELETE /api/lectures/1` | `POST /api/lectures/1/delete` |
| Path 변수 | `{lectureId}` | `{id}` |

### 권한별 경로 (Gateway에서 인가 처리)
- `/api/*/admin/**` → ADMIN
- `/api/*/professor/**` → PROFESSOR
- `/api/*/student/**` → STUDENT
- `/api/academic/public/**` → 비로그인 가능 (예: 공개 공지)

### HTTP Method
- 목록 조회: `GET /도메인` (필터링은 Query String)
- 상세 조회: `GET /도메인/{id}`
- 등록: `POST /도메인`
- 수정: `PATCH /도메인/{id}` (부분 수정 권장)
- 삭제: `DELETE /도메인/{id}`

---

## 🏷 네이밍 규칙

### 클래스
- Controller / Service / Repository는 접미어 필수 (예: `LectureService`)
- Entity는 이름만 (예: `Lecture`)

### 메서드 접두어
| 접두어 | 의미 |
|--------|------|
| `get` | 조회 |
| `create` | 생성 |
| `update` | 수정 |
| `delete` | 삭제 (물리/논리 모두) |
| `check` | 검증/체크 (조건 안 맞으면 예외 발생) |

> `check`는 복잡한 비즈니스 조건의 "이름표" 역할 + 실패 시 예외 발생.
> 예: `checkEnrollmentEligibility(student, lecture)` → 수강 가능 여부 검증

### DTO
- 요청: `~Req` (예: `LoginReq`)
- 응답: `~Res` (예: `StudentProfileRes`)
- 목록 응답: `~ListRes`

### 변수
- camelCase (예: `studentId`)
- Boolean은 `is` 접두어 (예: `isApproved`)

---

## 🔣 Enum 사용 규칙 (⚠️ 중요)

### 핵심 5가지
1. **모든 Enum은 `EnumMapperType` 구현**
    - `code`: DB 저장용 영문값 (예: `"STUDENT"`)
    - `value`: 프론트 응답용 한글값 (예: `"학생"`)
2. **`@JsonValue`는 `getValue()`에만** (인터페이스에 이미 설정됨, 건드리지 말 것)
3. **`@JsonCreator` 모든 Enum에 추가** (code와 value 둘 다 비교):
   ```java
   @JsonCreator
   public static EnumXxx from(String value) {
       for (EnumXxx e : EnumXxx.values()) {
           if (e.getCode().equalsIgnoreCase(value) || e.getValue().equalsIgnoreCase(value)) {
               return e;
           }
       }
       throw new IllegalArgumentException("유효하지 않은 값: " + value);
   }
   ```
4. **DB 저장은 `CodeConverter`로**:
   ```java
   @Converter(autoApply = true)
   public static class CodeConverter extends AbstractEnumCodeConverter<EnumXxx> {
       public CodeConverter() { super(EnumXxx.class, false); }
   }
   ```
5. **JWT나 서버 간 통신엔 Enum 직접 X** → `getCode()` 값(String)만 사용
    - 이유: `@JsonValue` 때문에 Enum 그대로 넣으면 한글로 저장됨

### JPA 사용 시
```java
@Enumerated(EnumType.STRING)  // DB는 VARCHAR, MySQL ENUM 타입 금지
@Column(name = "status")
private AttendStatus status;
```

### ⚠️ DB 컬럼 값은 반드시 영문 code와 일치
> 과거 한글 값이 저장되어 변환 실패 → NullPointerException 발생.
> auth_member, student, professor, lecture 등 enum 컬럼 일관성 점검 필요.

### 주요 Enum
- Role: `STUDENT`, `PROFESSOR`, `ADMIN`
- AttendStatus: `ATTEND`, `ABSENT`, `LATE`, `EARLY_LEAVE`
- AppealStatus: `PENDING`, `APPROVED`, `REJECTED`
- SessionType: `NORMAL`, `CANCEL`, `MAKEUP`
- LectureStatus: `PENDING`, `APPROVED`, `REJECTED`, `CANCELED`
- AcademicStatus(학생): `ENROLLED`, `ABSENCE`, `GRADUATED`
- 더미데이터 enum 값은 본 문서 하단 **더미 데이터 규칙** 참고

---

## 📦 공통 응답 / 에러 처리

### 응답 포맷 (성공)
```json
{ "status": "success", "message": "조회 성공", "data": { ... } }
```

### 에러 포맷
```json
{ "code": "ERR_001", "message": "에러 설명", "timestamp": "2026-04-16T09:00:00" }
```

### HTTP 상태 코드
| 상황 | Status |
|------|--------|
| 입력값 형식 오류 | 400 |
| 인증 없음 | 401 |
| 권한 없음 | 403 |
| 리소스 없음 | 404 |
| 중복 데이터 | 409 |
| 서버 오류 | 500 |

### 예외 규칙
- 목록 조회 시 데이터 없으면 **404 대신 200 + 빈 리스트**
- 모든 에러는 `ErrorCode` enum을 통해 처리 (CommonErrorCode + 도메인별 ErrorCode)

### 도메인별 에러 코드 (본인 담당)
**출석**: A001(학교 네트워크 아님 403) / A002(만료 QR 400) / A003(이미 출석 409) / A004(세션 없음 400) / A005(미수강 강의 403) / A006(당일 수업 없음 400) / A007(등록금 미납 403)

**성적**: G001(입력 기간 아님 403) / G002·G011(본인 강의 아님 403) / G003(점수 범위 초과 400) / G010(이의신청 기간 아님 403) / G012(이미 신청 409) / G013(이미 처리 409) / G020(강의평가 미완료 403)

---

## 🔒 `@Transactional` 필수 위치

1. 수강신청: `enrollments` + `grades` 동시 INSERT
2. 성적 입력/수정: `grades` UPDATE + `total_score` + `grade_letter` 재계산
3. 이의신청 승인: `grades` UPDATE + `appeal` 상태 변경
4. 출석 저장: `attendance` INSERT + `grades.attend_score` UPDATE

---

## 🎯 담당 기능 핵심

### 1️⃣ 출석 (QR + WiFi IP)

**흐름**
```
[교수] 출석 시작 → attendance_sessions 생성 (is_active=TRUE)
                → 5초마다 qr_tokens 갱신 (SSE로 프론트 푸시)
[학생] PWA 앱 + 학교 WiFi → QR 스캔
       → 검증: ①학교 IP ②QR 5초 이내 유효 ③중복 출석 ④수강 강의
       → attendance INSERT (status=ATTEND)
[교수] 종료 → is_active=FALSE → 미스캔 학생 전원 ABSENT
           → 지각/조퇴 등은 교수 수동 수정
[휴강] session_type='CANCEL'
[보강] session_type='MAKEUP' + 별도 날짜 등록(origin_date)
```

**설계 철학**: 자동 로직은 단순하게(종료 시 결석만 자동). 예외 케이스는 교수 수동 수정에 위임.

**기술 결정**
- QR 갱신: 폴링 → **SSE**(`SseEmitter`) 전환. 서버 푸시가 토큰 갱신에 적합.
- WiFi IP 검증: 백엔드에서 `X-Forwarded-For` 또는 `RemoteAddr` 확인.

### 2️⃣ 성적 (Grade)

**흐름**
```
[수강신청] enrollments INSERT + grades INSERT (점수 0, 등급 NULL) — @Transactional
[성적 입력]
(교수,학생 공통부분) Schedule DB에 type이 GRADE_APPEAL이고, is_active = true 일때만 성적이의신청 및 조치 가능. isActive = false 일 때에는 성적이의신청 및 조치 불가능.
---
(교수) Schedule DB에 type이 GRADE_INPUT 이고, is_active = true 일때만 성적입력 가능.
      isActive = false 일 때에는 성적입력 불가능.
(성적입력 흐름)
          grades UPDATE
           → attend_score: attendance 테이블에서 자동 집계
           → total = mid*0.3 + fin*0.3 + assignment*0.2 + attend*0.2
           → grade_letter 자동 부여
---
(학생) Schedule DB에 type이 GRADE_VIEW 이고, is_active = true 일때만 성적 조회 가능.
      isActive = false 일 때에는 성적조회 불가능.
      Schedule DB에 type이 GRADE_VIEW 이고, is_active = true가 되려면 성적을 조회하려는 해당학생의 정보와, lecture_evaluation테이블 row에 있는 학생정보와 동일한지 비교 후 학생이 강의 수강평가를 한 내역이 있으면 Schedule DB에 type이 GRADE_VIEW 이고, is_active = true가 되도록 구현.
```

**이의신청 흐름**
```
학생 PENDING → 교수 조회
            → 반려(REJECTED): reject_reason 필수
            → 승인(APPROVED): 성적수정창으로 이동 → 점수 수정 → grades 자동 업데이트
```

**등급 기준 (2차)**
| 점수 | 등급 |
|------|------|
| 95~100 | A+ |
| 90~94 | A |
| 85~89 | B+ |
| 80~84 | B |
| 75~79 | C+ |
| 70~74 | C |
| 65~69 | D+ |
| 60~64 | D |
| <60 | F |

**유효성 검사 4단계**: FE 입력 → FE 저장 → BE Service → DB CHECK 제약

### 3️⃣ 공지사항
- 작성자: 관리자만
- 대상: `STUDENT` / `PROFESSOR` (FK 아닌 enum)
- 조회: 학생/교수는 본인 대상 공지만, 관리자는 본인 작성 전부
- 비로그인 가능한 공지는 `/api/academic/public/` 경로

---

## 🎨 Vue 작성 규칙

### Script 작성 순서
```
1. 라이브러리 import       6. localStorage 키
2. 컴포넌트 import          7. computed
3. 서비스 import            8. watch
4. 라우터 / 스토어          9. API 호출 함수
5. 상태값 (reactive, ref)  10. 이벤트 핸들러
                            11. onMounted (마지막)
```

### 핵심 패턴
- **`isMounting` 플래그**: `watch`와 `onMounted` 충돌 방지 (마운트 중 watch 무시)
- **localStorage 임시저장**: 의존값 있는 키는 `computed`로 선언 (날짜 변경 시 자동 갱신)
- **검색 ref 변수명**: 컴포넌트명과 충돌 방지 위해 **소문자** 사용 (`searchInput` ✅, `SearchInput` ❌)
- **저장 시**: `filteredList` 말고 **`state.list` 전체** 사용

### 공통
- axios interceptor로 전역 에러 처리 (메시지 일관성)
- API 호출 중 스피너/스켈레톤 UI 표시

---

## 📌 NFR 핵심

- **JWT**: Access는 헤더, Refresh는 **HttpOnly Cookie**
- **비밀번호**: BCrypt 단방향 해시
- **API 응답**: 일반 3초 / 주요 기능 2초 / 게시글 1초 이내
- **페이지네이션**: 모든 목록 조회 API에 Spring `Pageable` 적용
- **환경변수**: DB 접속정보, JWT 시크릿은 환경변수로 분리
- **수강신청 동시성**: DB 락 또는 트랜잭션으로 정원 초과 방지
- **API 명세**: SpringDoc(Swagger) 자동 문서화

---

## 🤝 팀 협업 메모

- **수강신청 API**(타 팀원 담당): 성공 시 `grades` Row 즉시 생성 → `@Transactional`로 atomic 처리
- **Gateway**: `/api/internal/` 경로 외부 차단
- **Git**: 다른 팀원 도메인 코드 직접 수정 금지

---

## 🐛 알려진 이슈

- **log4jdbc 로그 과다**: `application.yml`에서 `jdbc: OFF` / `jdbc.sqlonly: DEBUG`로 조정
- **Enum 컬럼 한글값 NPE 위험**: 모든 enum 컬럼이 영문 code와 일치하는지 점검 필요
- **CustomException**: 팀 공용 예외 클래스명 미확정 → 확정 후 통일

---

## ✅ 코드 작성 전 체크리스트

- [ ] 어느 서비스(Auth/Academic/Community)에 속하는가?
- [ ] 권한별 Controller 분리(Admin/Professor/Student/Public)가 필요한가?
- [ ] URL이 케밥 케이스 + 복수형 + 동사 금지를 지키는가?
- [ ] DTO가 `Req`/`Res`/`ListRes` 접미어를 따르는가?
- [ ] Enum이 `EnumMapperType` + `CodeConverter` 패턴인가?
- [ ] DB 컬럼 값이 영문 enum code와 일치하는가?
- [ ] `@Transactional`이 필요한 작업인가? (출석·성적·수강신청·이의신청)
- [ ] 캐시 테이블에 FK를 걸지 않았는가?
- [ ] 공통 응답 포맷을 사용하는가?
- [ ] 목록 조회가 404 대신 200 + 빈 리스트인가?
- [ ] 다른 팀원의 도메인 코드를 건드리지 않는가?

---

- 코드의 변경이력(추가, 수정, 삭제)은 모두 주석으로 표시하여 어떤내용이 변경되는지 확인이 필요하다.

---

*Last updated: 2026.05.11*
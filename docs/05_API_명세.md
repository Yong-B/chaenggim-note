# 05. API 명세

- Base URL: `/api` · JSON(UTF-8) · 인증 없음
- 날짜 `"2026-11-09"`, 시각 `"2026-11-09T16:00:00"`(초 이하 소수점이 붙을 수 있음)
- enum은 영문 이름 문자열 (`"READY"`, `"CLINICAL"`)
- 숨김 의료진과 그 데이터는 어떤 응답에도 나오지 않는다(R7)

## 1. 공통 응답 형식 (ApiResponse)

모든 응답 본문은 `ApiResponse`로 감싼다(`11_코딩_컨벤션.md` K5). 컨트롤러 반환 타입은 `ResponseEntity<ApiResponse<T>>`.

```jsonc
// 성공
{ "success": true, "data": { ... }, "error": null }

// 성공 · 돌려줄 데이터 없음 (삭제, 초기화)
{ "success": true, "data": null, "error": null }

// 실패
{
  "success": false,
  "data": null,
  "error": {
    "code": "VALIDATION_FAILED",
    "message": "입력값을 확인해 주세요.",
    "fieldErrors": [
      { "field": "name", "message": "이름을 입력해 주세요." }
    ]
  }
}
```

```java
public record ApiResponse<T>(boolean success, T data, ErrorBody error) {
    static <T> ApiResponse<T> ok(T data);           // success=true, error=null
    static ApiResponse<Void> ok();                  // success=true, data=null
    static ApiResponse<Void> fail(ErrorBody error); // success=false, data=null
}
public record ErrorBody(String code, String message, List<FieldErrorItem> fieldErrors) { }
```

- **이 문서 3절 이후의 응답 JSON 예시와 표의 "성공" 타입은 `data` 안에 들어가는 값**이다.
- 삭제·초기화는 본문이 있어야 하므로 **204 대신 200** + `data: null`.
- 등록(201)은 `Location` 헤더 + `data`에 생성된 객체.

### 1.1 에러

| 상황 | HTTP | error.code | error.message | error.fieldErrors |
| --- | --- | --- | --- | --- |
| Bean Validation 실패 / 서비스 필드 검증(`BusinessException` + fieldErrors) / 날짜 파싱 실패 | 400 | `VALIDATION_FAILED` | `입력값을 확인해 주세요.` | 있음 |
| JSON 형식 자체가 잘못됨 | 400 | `BAD_REQUEST` | `요청 형식이 올바르지 않습니다.` | `[]` |
| `ErrorCode.NOT_FOUND` / 없는 경로 | 404 | `NOT_FOUND` | `요청한 정보를 찾을 수 없습니다.` | `[]` |
| 상태 규칙 위반 `BusinessException` | 409 | ErrorCode의 code | ErrorCode의 message | `[]` |
| 그 외 예외 | 500 | `INTERNAL_ERROR` | `일시적인 오류가 발생했습니다. 잠시 후 다시 시도해 주세요.` | `[]` |

- `fieldErrors`는 항상 배열(없으면 `[]`). 한 필드에 에러가 여럿이면 첫 번째만 넣는다.
- 날짜 파싱 실패 시 field는 해당 필드 경로, message는 `날짜 형식이 올바르지 않습니다.`
  - 구현: `HttpMessageNotReadableException`의 원인이 Jackson `InvalidFormatException`이고 대상 타입이 `LocalDate`면, 예외의 `getPath()`로 경로를 만든다(`commitments[0].dueDate` 형식: 이름 있는 항목은 `.이름`, 배열 항목은 `[인덱스]`). 그 외 `HttpMessageNotReadableException`은 `BAD_REQUEST`.
- 409 code 목록: `QUESTION_NOT_UNANSWERED`, `QUESTION_NOT_READY`, `QUESTION_LOCKED`, `VISIT_HAS_DELIVERED`, `COMMITMENT_DONE` (`02_도메인_규칙.md` 6절 `ErrorCode`)

## 2. 공통 응답 객체

```jsonc
// DoctorRef — 다른 응답 안에서 의료진을 가리킬 때
{ "id": 1, "name": "김가상", "hospital": "가상내과의원" }

// QuestionResponse
{
  "id": 1,
  "doctor": { DoctorRef },
  "visitId": 2,
  "visitDate": "2026-11-02",          // 받은 날 (= 방문 날짜)
  "content": "고령 환자 관련 로수젯 자료가 있는지 궁금하다고 하심",
  "productTag": null,                 // P1
  "status": "READY",
  "answer": "학술팀에 확인한 자료 목록을 정리함. ...",   // 없으면 null
  "sourceType": "MEDICAL_TEAM",       // 없으면 null
  "sourceDetail": "학술팀 메일 회신",  // 없으면 null
  "answeredAt": "2026-11-06T16:00:00",
  "deliveredAt": null
}

// CommitmentResponse
{
  "id": 2,
  "doctor": { DoctorRef },
  "visitId": 2,
  "content": "고령 환자 자료 요약본 전달",
  "dueDate": "2026-11-07",
  "done": false,
  "doneAt": null,
  "overdue": true,                    // 서버가 today 기준 계산
  "dueToday": false
}

// VisitResponse
{ "id": 2, "doctorId": 1, "visitDate": "2026-11-02", "memo": "...", "questionCount": 2, "commitmentCount": 1, "hasDelivered": false }

// DoctorResponse
{ "id": 1, "name": "김가상", "hospital": "가상내과의원", "department": "내과", "memo": "...", "nextVisitDate": "2026-11-10" }
```

- `VisitResponse.hasDelivered`: 그 방문에 전달 완료 질문이 있는지. 프론트는 이 값이 true면 삭제 버튼을 숨긴다(D16).

## 3. 홈

### GET `/api/home` → 200

```jsonc
{
  "today": "2026-11-09",
  "overdueCommitments": [ CommitmentResponse ],
  "dueTodayCommitments": [ CommitmentResponse ],
  "upcomingDoctors": [
    { "doctor": { DoctorRef }, "department": "순환기내과", "nextVisitDate": "2026-11-07",
      "pastDue": true, "readyAnswerCount": 0, "openCommitmentCount": 1 }
  ],
  "questionCounts": { "UNANSWERED": 1, "CHECKING": 1, "READY": 2 }
}
```

정렬·조건은 `02_도메인_규칙.md` 7.1절.

## 4. 의료진

| Method | URL | 요청 | 성공 | 실패 |
| --- | --- | --- | --- | --- |
| GET | `/api/doctors?q=` | q 선택 | 200 `[DoctorSummary]` | - |
| POST | `/api/doctors` | DoctorRequest | 201 `DoctorResponse`, `Location: /api/doctors/{id}` | 400 |
| GET | `/api/doctors/{id}` | - | 200 `DoctorDetailResponse` | 404 |
| PUT | `/api/doctors/{id}` | DoctorRequest | 200 `DoctorResponse` | 400, 404 |
| DELETE | `/api/doctors/{id}` | - | 200 `null` (숨김 처리, D14) | 404 |

```jsonc
// DoctorRequest (등록·수정 공용. 수정 시 nextVisitDate가 null이면 예정일을 지운다)
{ "name": "김가상", "hospital": "가상내과의원", "department": "내과", "memo": null, "nextVisitDate": "2026-11-10" }

// DoctorSummary
{ "id": 1, "name": "김가상", "hospital": "가상내과의원", "department": "내과", "nextVisitDate": "2026-11-10" }

// DoctorDetailResponse
{
  "doctor": { DoctorResponse },
  "today": "2026-11-09",
  "prep": {
    "readyQuestions":   [ QuestionResponse ],   // 전달할 답변
    "openQuestions":    [ QuestionResponse ],   // 미답변 + 확인 중
    "openCommitments":  [ CommitmentResponse ], // 남은 약속
    "latestVisit":      VisitResponse | null
  },
  "visits":             [ VisitResponse ],
  "deliveredQuestions": [ QuestionResponse ],
  "doneCommitments":    [ CommitmentResponse ]
}
```

## 5. 방문 기록

| Method | URL | 요청 | 성공 | 실패 |
| --- | --- | --- | --- | --- |
| POST | `/api/doctors/{doctorId}/visits` | VisitCreateRequest | 201 `VisitResponse` | 400, 404 |
| GET | `/api/visits/{id}` | - | 200 `VisitResponse` + `doctor`(DoctorRef) 필드 추가 | 404 |
| PUT | `/api/visits/{id}` | VisitUpdateRequest | 200 `VisitResponse` | 400, 404 |
| DELETE | `/api/visits/{id}` | - | 200 `null` (질문·약속 함께 삭제, D16) | 404, 409 `VISIT_HAS_DELIVERED` |

```jsonc
// VisitCreateRequest
{
  "visitDate": "2026-11-09",
  "memo": "고령 환자 처방 경험 이야기 나눔",
  "nextVisitDate": "2026-11-16",            // null이면 의료진의 기존 예정일 유지 (D13)
  "questions":   [ { "content": "...", "productTag": null } ],   // 0~10개
  "commitments": [ { "content": "...", "dueDate": "2026-11-12" } ] // 0~10개
}

// VisitUpdateRequest (D15)
{ "visitDate": "2026-11-08", "memo": "..." }
```

`VisitService.create` — 한 트랜잭션: 의료진 조회(숨김이면 404) → 방문 날짜 미래 검사 → 약속 기한 과거 검사(`commitments[i].dueDate`) → Visit 저장 → Question들 저장 → Commitment들 저장 → `doctor.changeNextVisitDate(nextVisitDate)`.
검증 에러는 **한 번에 모두** 모아서 400으로 돌려준다(Bean Validation 에러 + 서비스 검사 에러를 합쳐서). 구현: 서비스 검사 에러를 `List<FieldErrorItem>`으로 모아 `BusinessException(ErrorCode.VALIDATION_FAILED, errors)`로 한 번에 던진다. 단, 서비스 검사는 Bean Validation을 통과한 뒤에 실행되므로, Bean Validation 에러가 있으면 그것만 돌려준다.

## 6. 질문

| Method | URL | 요청 | 성공 | 실패 |
| --- | --- | --- | --- | --- |
| GET | `/api/questions?status=UNANSWERED` | status 선택(기본 UNANSWERED, 잘못된 값도 기본값) | 200 `QuestionListResponse` | - |
| GET | `/api/questions/{id}` | - | 200 `QuestionResponse` | 404 |
| PATCH | `/api/questions/{id}` | `{ "content": "..." }` | 200 `QuestionResponse` | 400, 404, 409 `QUESTION_LOCKED` |
| POST | `/api/questions/{id}/start-checking` | - | 200 `QuestionResponse` | 404, 409 `QUESTION_NOT_UNANSWERED` |
| POST | `/api/questions/{id}/answer` | AnswerRequest | 200 `QuestionResponse` | 400, 404, 409 `QUESTION_LOCKED` |
| POST | `/api/questions/{id}/deliver` | - | 200 `QuestionResponse` | 404, 409 `QUESTION_NOT_READY` |
| DELETE | `/api/questions/{id}` | - | 200 `null` | 404, 409 `QUESTION_LOCKED` |

```jsonc
// AnswerRequest
{ "answer": "...", "sourceType": "CLINICAL", "sourceDetail": "RACING 연구 요약 자료" }

// QuestionListResponse
{
  "status": "UNANSWERED",
  "counts": { "UNANSWERED": 1, "CHECKING": 1, "READY": 2, "DELIVERED": 1 },
  "items": [ QuestionResponse ]
}
```

## 7. 약속

| Method | URL | 요청 | 성공 | 실패 |
| --- | --- | --- | --- | --- |
| GET | `/api/commitments/{id}` | - | 200 `CommitmentResponse` | 404 |
| PUT | `/api/commitments/{id}` | `{ "content": "...", "dueDate": "2026-11-12" }` | 200 `CommitmentResponse` | 400, 404, 409 `COMMITMENT_DONE` |
| POST | `/api/commitments/{id}/complete` | - | 200 `CommitmentResponse` | 404 |
| POST | `/api/commitments/{id}/reopen` | - | 200 `CommitmentResponse` | 404 |
| DELETE | `/api/commitments/{id}` | - | 200 `null` | 404 |

## 8. 관리

| Method | URL | 성공 |
| --- | --- | --- |
| POST | `/api/admin/reset` | 200 `null` — 전체 삭제 후 시드 입력(한 트랜잭션) |

## 9. 프론트 쿼리 갱신 규칙

변경 요청이 성공하면 프론트는 아래 쿼리 키를 무효화한다(`07_프론트_구현가이드.md` 3절 키 정의).

| 변경 | 무효화할 키 |
| --- | --- |
| 의료진 등록·수정 | `['doctors']`, `['doctor', id]`, `['home']` |
| 의료진 삭제 | `['doctors']`, `['home']`, `['questions']` (그리고 `['doctor', id]`는 제거) |
| 방문 작성·수정·삭제 | `['visit', id]`, `['doctor', doctorId]`, `['home']`, `['questions']`, `['doctors']` |
| 질문 수정·상태 변경·삭제 | `['question', id]`, `['doctor', doctorId]`, `['home']`, `['questions']` |
| 약속 수정·완료·취소·삭제 | `['commitment', id]`, `['doctor', doctorId]`, `['home']` |
| 초기화 | 전체 (`queryClient.invalidateQueries()`) |

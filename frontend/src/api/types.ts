// 05 API 명세의 요청·응답 타입. 날짜·시각은 string 그대로 둔다(Date로 바꾸지 않음).

export type QuestionStatus = 'UNANSWERED' | 'CHECKING' | 'READY' | 'DELIVERED'
export type SourceType = 'LABEL' | 'CLINICAL' | 'MEDICAL_TEAM'
export type ProductTag = 'ROSUZET' | 'AMOSARTAN' | 'AMOPREL' | 'EFE' | 'ROLONTIS'

/** "2026-11-09" */
export type DateString = string
/** "2026-11-09T16:00:00" (초 이하 소수점이 붙을 수 있음) */
export type DateTimeString = string

// ---- 공통 응답 객체 ----

export type DoctorRef = { id: number; name: string; hospital: string }

export type QuestionResponse = {
  id: number
  doctor: DoctorRef
  visitId: number
  visitDate: DateString
  content: string
  productTag: ProductTag | null
  status: QuestionStatus
  answer: string | null
  sourceType: SourceType | null
  sourceDetail: string | null
  answeredAt: DateTimeString | null
  deliveredAt: DateTimeString | null
}

export type CommitmentResponse = {
  id: number
  doctor: DoctorRef
  visitId: number
  content: string
  dueDate: DateString
  done: boolean
  doneAt: DateTimeString | null
  overdue: boolean
  dueToday: boolean
}

export type VisitResponse = {
  id: number
  doctorId: number
  visitDate: DateString
  memo: string | null
  questionCount: number
  commitmentCount: number
  hasDelivered: boolean
}

/** GET /api/visits/{id} */
export type VisitDetailResponse = VisitResponse & { doctor: DoctorRef }

export type DoctorResponse = {
  id: number
  name: string
  hospital: string
  department: string
  memo: string | null
  nextVisitDate: DateString | null
}

export type DoctorSummary = {
  id: number
  name: string
  hospital: string
  department: string
  nextVisitDate: DateString | null
}

export type DoctorDetailResponse = {
  doctor: DoctorResponse
  today: DateString
  prep: {
    readyQuestions: QuestionResponse[]
    openQuestions: QuestionResponse[]
    openCommitments: CommitmentResponse[]
    latestVisit: VisitResponse | null
  }
  visits: VisitResponse[]
  deliveredQuestions: QuestionResponse[]
  doneCommitments: CommitmentResponse[]
}

// ---- 홈 ----

export type UpcomingDoctor = {
  doctor: DoctorRef
  department: string
  nextVisitDate: DateString
  pastDue: boolean
  readyAnswerCount: number
  openCommitmentCount: number
}

export type HomeResponse = {
  today: DateString
  overdueCommitments: CommitmentResponse[]
  dueTodayCommitments: CommitmentResponse[]
  upcomingDoctors: UpcomingDoctor[]
  questionCounts: Record<'UNANSWERED' | 'CHECKING' | 'READY', number>
}

// ---- 질문함 ----

export type QuestionListResponse = {
  status: QuestionStatus
  /** 적용한 제품 필터 (P1). 없으면 null */
  product: ProductTag | null
  counts: Record<QuestionStatus, number>
  items: QuestionResponse[]
}

// ---- 요청 ----

/** 등록·수정 공용. 수정 시 nextVisitDate가 null이면 예정일을 지운다. */
export type DoctorRequest = {
  name: string
  hospital: string
  department: string
  memo: string | null
  nextVisitDate: DateString | null
}

export type VisitCreateRequest = {
  visitDate: DateString
  memo: string | null
  /** null이면 의료진의 기존 예정일 유지 (D13) */
  nextVisitDate: DateString | null
  questions: { content: string; productTag: ProductTag | null }[]
  commitments: { content: string; dueDate: DateString | null }[]
}

export type VisitUpdateRequest = { visitDate: DateString; memo: string | null }

export type QuestionUpdateRequest = { content: string }

export type AnswerRequest = {
  answer: string
  sourceType: SourceType | null
  sourceDetail: string | null
}

export type CommitmentUpdateRequest = { content: string; dueDate: DateString | null }

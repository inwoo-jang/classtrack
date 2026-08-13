/**
 * Spring 백엔드의 DTO와 1:1로 맞춘 타입.
 * 서버 record 를 고치면 여기도 같이 고쳐야 한다 — 어긋나면 컴파일 단계에서 잡힌다.
 */

/** com.inwoo.classtrack.dto.course.AssignmentSummary */
export interface AssignmentSummary {
  total: number
  todo: number
  inProgress: number
  completed: number
  /** 아직 끝내지 않았는데 마감이 지난 과제 수 */
  overdue: number
}

/** com.inwoo.classtrack.domain.CourseStatus — 시작일/기간으로 서버가 계산한다 */
export type CourseStatus = 'UPCOMING' | 'ONGOING' | 'FINISHED'

/** com.inwoo.classtrack.dto.course.CourseResponse */
export interface Course {
  id: number
  title: string
  subject: string
  instructor: string
  /** ISO date, 예: "2026-03-02" */
  startDate: string
  /** 수강 마지막 날 */
  endDate: string
  durationDays: number
  location: string
  /** true = 대면 실강, false = 비대면 */
  liveLecture: boolean
  practiceProfessor: string | null
  status: CourseStatus
  assignments: AssignmentSummary
}

/** com.inwoo.classtrack.dto.course.CourseCreateRequest */
export interface CourseCreateRequest {
  title: string
  subject: string
  instructor: string
  startDate: string
  durationDays: number
  location: string
  liveLecture: boolean
  practiceProfessor: string | null
}

/** com.inwoo.classtrack.dto.course.CourseUpdateRequest — 생성과 필드가 같다 */
export type CourseUpdateRequest = CourseCreateRequest

/** com.inwoo.classtrack.domain.AssignmentStatus */
/** com.inwoo.classtrack.domain.LinkStatus — 결과물 링크 확인 결과 (비동기 갱신) */
export type LinkStatus = 'NONE' | 'PENDING' | 'OK' | 'BROKEN'

export type AssignmentStatus = 'TODO' | 'IN_PROGRESS' | 'COMPLETED'

export const ASSIGNMENT_STATUSES: readonly AssignmentStatus[] = [
  'TODO',
  'IN_PROGRESS',
  'COMPLETED',
]

/** com.inwoo.classtrack.dto.assignment.AssignmentResponse */
export interface Assignment {
  id: number
  courseId: number
  courseTitle: string
  title: string
  description: string | null
  /** ISO date-time, 예: "2026-03-20T23:59:00" */
  dueDate: string
  status: AssignmentStatus
  /** 완료로 바꾼 시각 */
  submittedAt: string | null
  /** Google Drive / GitHub 등 결과물 링크 */
  submissionUrl: string | null
  linkStatus: LinkStatus
  linkCheckedAt: string | null
}

/** com.inwoo.classtrack.dto.assignment.AssignmentCreateRequest */
export interface AssignmentCreateRequest {
  title: string
  description: string | null
  dueDate: string
  submissionUrl: string | null
}

/** com.inwoo.classtrack.dto.assignment.AssignmentUpdateRequest */
export interface AssignmentUpdateRequest {
  status: AssignmentStatus
  submissionUrl: string | null
}

/** com.inwoo.classtrack.logging.LogEntry */
export interface LogEntry {
  sequence: number
  timestamp: string
  level: string
  logger: string
  thread: string
  /** 콘솔에 찍히는 것과 동일한 원문. 스택트레이스 포함. 가공하지 않고 그대로 출력한다 */
  raw: string
}

// ── 캘린더 (com.inwoo.classtrack.calendar.CalendarResponse) ──

export interface CalendarSession {
  courseId: number
  courseTitle: string
  subject: string
  /** ISO date */
  date: string
  dayIndex: number
  totalDays: number
  liveLecture: boolean
}

export interface CalendarData {
  from: string
  to: string
  /** 이 기간의 공휴일 */
  holidays: string[]
  sessions: CalendarSession[]
}

// ── 구현 현황 (com.inwoo.classtrack.dev.DevOverview) ──
// 실행 중인 앱에서 뽑아낸 메타데이터. 손으로 관리하는 표가 아니다.

export interface EndpointInfo {
  /** 리소스 묶음 (courses / assignments / …) */
  group: string
  httpMethod: string
  path: string
  controller: string
  handler: string
  /** @ApiDescription 값. 안 붙인 엔드포인트는 null */
  description: string | null
  annotations: string[]
}

export interface ServiceMethodInfo {
  serviceClass: string
  method: string
  transactional: boolean
  readOnly: boolean
  /** LoggingAspect 포인트컷에 걸리는지 */
  aopLogged: boolean
}

export interface EntityAttribute {
  name: string
  type: string
  /** BASIC / MANY_TO_ONE / ONE_TO_MANY … */
  kind: string
  id: boolean
  optional: boolean
  targetEntity: string | null
}

export interface EntityInfo {
  name: string
  tableName: string
  attributes: EntityAttribute[]
}

export interface DevOverview {
  endpoints: EndpointInfo[]
  serviceMethods: ServiceMethodInfo[]
  entities: EntityInfo[]
}

/** com.inwoo.classtrack.dto.dashboard.CourseStats */
export interface CourseStats {
  total: number
  upcoming: number
  ongoing: number
  finished: number
}

/** com.inwoo.classtrack.dto.dashboard.DashboardResponse */
export interface Dashboard {
  courses: CourseStats
  assignments: AssignmentSummary
  ongoingCourses: Course[]
  inProgressAssignments: Assignment[]
}

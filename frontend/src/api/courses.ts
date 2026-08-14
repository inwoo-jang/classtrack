import { api } from './client'
import type {
  Assignment,
  AssignmentCreateRequest,
  AssignmentUpdateRequest,
  Course,
  CourseCreateRequest,
  CourseUpdateRequest,
  CalendarData,
  Dashboard,
  DevOverview,
  LogEntry,
} from '@/types/api'

export const dashboardApi = {
  load: () => api.get<Dashboard>('/api/dashboard'),
}

export const portfolioApi = {
  markdown: (featuredOnly: boolean) =>
    api.getText(`/api/portfolio/markdown?featuredOnly=${featuredOnly}`),
}

export const technologyApi = {
  /** 설정 기본값 + 이미 쓰인 기술 */
  list: () => api.get<string[]>('/api/technologies'),
}

export const subjectApi = {
  /** 설정에 정의된 기본 목록 + DB 에 이미 쓰인 과목명 */
  list: () => api.get<string[]>('/api/subjects'),
}

export const calendarApi = {
  range: (from: string, to: string) =>
    api.get<CalendarData>(`/api/calendar?from=${from}&to=${to}`),
}

export const devApi = {
  overview: () => api.get<DevOverview>('/api/dev/overview'),
}

export const logApi = {
  /** after 를 주면 그 sequence 보다 새로 생긴 로그만 받는다 (폴링용) */
  list: (params: { after?: number; level?: string; q?: string; limit?: number } = {}) => {
    const search = new URLSearchParams()
    if (params.after !== undefined) search.set('after', String(params.after))
    if (params.level) search.set('level', params.level)
    if (params.q) search.set('q', params.q)
    if (params.limit) search.set('limit', String(params.limit))

    const query = search.toString()
    return api.get<LogEntry[]>(`/api/logs${query ? `?${query}` : ''}`)
  },

  clear: () => api.delete<void>('/api/logs'),
}

export const courseApi = {
  list: () => api.get<Course[]>('/api/courses'),

  detail: (courseId: number) => api.get<Course>(`/api/courses/${courseId}`),

  create: (body: CourseCreateRequest) => api.post<Course>('/api/courses', body),

  update: (courseId: number, body: CourseUpdateRequest) =>
    api.put<Course>(`/api/courses/${courseId}`, body),

  remove: (courseId: number) => api.delete<void>(`/api/courses/${courseId}`),
}

export const assignmentApi = {
  /** 강의 구분 없이 전체 과제 (마감일 순) */
  listAll: () => api.get<Assignment[]>('/api/assignments'),

  list: (courseId: number) =>
    api.get<Assignment[]>(`/api/courses/${courseId}/assignments`),

  create: (courseId: number, body: AssignmentCreateRequest) =>
    api.post<Assignment>(`/api/courses/${courseId}/assignments`, body),

  update: (courseId: number, assignmentId: number, body: AssignmentUpdateRequest) =>
    api.patch<Assignment>(`/api/courses/${courseId}/assignments/${assignmentId}`, body),

  remove: (courseId: number, assignmentId: number) =>
    api.delete<void>(`/api/courses/${courseId}/assignments/${assignmentId}`),
}

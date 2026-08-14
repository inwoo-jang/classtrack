import type { Assignment, AssignmentStatus, Course, CourseStatus } from '@/types/api'

/** "2026-03-02" -> "2026. 3. 2." */
export function formatDate(iso: string): string {
  return new Date(iso).toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'long',
    day: 'numeric',
  })
}

/** "2026-03-20T23:59:00" -> "3월 20일 오후 11:59" */
export function formatDateTime(iso: string): string {
  return new Date(iso).toLocaleString('ko-KR', {
    month: 'long',
    day: 'numeric',
    hour: 'numeric',
    minute: '2-digit',
  })
}

/** 마감까지 남은 일수. 음수면 이미 지난 것. */
export function daysUntil(iso: string): number {
  const diff = new Date(iso).getTime() - Date.now()
  return Math.ceil(diff / (1000 * 60 * 60 * 24))
}

export const STATUS_LABEL: Record<AssignmentStatus, string> = {
  TODO: '진행 전',
  IN_PROGRESS: '진행 중',
  COMPLETED: '완료',
}

export const COURSE_STATUS_LABEL: Record<CourseStatus, string> = {
  UPCOMING: '예정',
  ONGOING: '진행 중',
  FINISHED: '종료',
}

/** 대면 실강인지 비대면인지. */
export function deliveryLabel(course: Course): string {
  return course.liveLecture ? '대면' : '비대면'
}

/**
 * 장소에서 호수만 뽑는다. "판교 캠퍼스 5층 501호" -> "501호"
 * 캠퍼스·층은 매번 같아서 카드에서는 정보가 되지 못한다.
 */
export function roomOf(location: string): string {
  const room = location.match(/(\S*\d+호)\s*$/)
  if (room) return room[1]!

  // "호"로 끝나지 않으면 마지막 낱말이라도 보여준다
  const parts = location.trim().split(/\s+/)
  return parts[parts.length - 1] ?? location
}

/** 진행 중인 강의가 며칠째인지 (1일차부터). */
export function courseDayIndex(course: Course): number {
  const start = new Date(course.startDate).getTime()
  const elapsed = Math.floor((Date.now() - start) / (1000 * 60 * 60 * 24))
  return Math.min(Math.max(elapsed + 1, 1), course.durationDays)
}

export function isDone(status: AssignmentStatus): boolean {
  return status === 'COMPLETED'
}

/** 마감 임박/초과 배지. 이미 끝난 과제에는 표시하지 않는다. */
export function deadlineBadge(
  assignment: Assignment,
): { text: string; urgent: boolean } | null {
  if (isDone(assignment.status)) return null

  const days = daysUntil(assignment.dueDate)
  if (days < 0) return { text: `${Math.abs(days)}일 지남`, urgent: true }
  if (days === 0) return { text: '오늘 마감', urgent: true }
  if (days <= 3) return { text: `D-${days}`, urgent: true }
  return { text: `D-${days}`, urgent: false }
}

/** 결과물 링크가 어디를 가리키는지 짧게 알려준다. */
export function linkKind(url: string): string {
  try {
    const host = new URL(url).hostname.replace(/^www\./, '')
    if (host.includes('github')) return 'GitHub'
    if (host.includes('gitlab')) return 'GitLab'
    if (host.includes('google')) return 'Drive'
    if (host.includes('notion')) return 'Notion'
    return host
  } catch {
    return '링크'
  }
}

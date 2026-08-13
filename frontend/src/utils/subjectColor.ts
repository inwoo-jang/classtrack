/**
 * 과목별 색.
 *
 * DB에 저장하지 않는다. 과목명은 이미 Course에 문자열로 있고, 같은 이름이면 항상 같은
 * 색이 나오도록 규칙으로 정하면 충분하다. 저장하면 과목을 추가할 때마다 색도 함께
 * 관리해야 한다.
 *
 * 기본 8개 과목은 순서를 고정하고(서로 이웃한 색이 겹치지 않게), 그 밖의 과목은
 * 이름 해시로 배정한다.
 *
 * 흑백 기조를 해치지 않도록 채도를 낮춘 톤만 골랐고, 화면에서는 배경이 아니라
 * 작은 점·왼쪽 선처럼 최소 면적으로만 쓴다.
 */

const PALETTE = [
  '#7b7fd4', // indigo — 악센트와 동일
  '#5b8fa8', // steel blue
  '#6fa292', // teal
  '#a88b6b', // tan
  '#a87b8f', // mauve
  '#93a06f', // olive
  '#8c8fa3', // slate
  '#7e8ca0', // blue grey
] as const

/** 설정에 있는 기본 과목은 팔레트 순서를 고정한다. */
const FIXED: Record<string, number> = {
  'Software Engineering': 0,
  'Data Analytics': 1,
  'Machine Learning': 2,
  'LLM Engineering': 3,
  'Generative AI': 4,
  'AI Agent': 5,
  'Cloud DevOps': 6,
  'Project & Career': 7,
}

/** 문자열 → 안정적인 정수. 같은 이름이면 새로고침해도 같은 색이 나온다. */
function hash(value: string): number {
  let h = 0
  for (let i = 0; i < value.length; i++) {
    h = (h * 31 + value.charCodeAt(i)) | 0
  }
  return Math.abs(h)
}

export function subjectColor(subject: string): string {
  const fixed = FIXED[subject]
  const index = fixed !== undefined ? fixed : hash(subject) % PALETTE.length
  return PALETTE[index]!
}

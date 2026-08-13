/**
 * 얇은 fetch 래퍼.
 *
 * 개발 중에는 BASE_URL 이 비어 있고, vite.config.ts 의 proxy 가 /api 요청을
 * localhost:8085 로 넘긴다. 배포 시에는 .env 에 VITE_API_BASE_URL 을 넣으면 된다.
 */

const BASE_URL = import.meta.env.VITE_API_BASE_URL ?? ''

/** 서버가 4xx/5xx 를 돌려줬을 때 던지는 에러. */
export class ApiError extends Error {
  constructor(
    readonly status: number,
    message: string,
  ) {
    super(message)
    this.name = 'ApiError'
  }
}

/**
 * Spring 의 기본 에러 응답에서 사람이 읽을 만한 메시지를 뽑아낸다.
 *
 * Bean Validation 이 실패하면 최상위 message 는 "Validation failed for object=…" 같은
 * 내부 문구라 쓸모가 없다. 우리가 DTO 에 적어둔 한국어 메시지는 errors[].defaultMessage
 * 에 들어오므로 그쪽을 먼저 본다.
 */
async function readErrorMessage(res: Response): Promise<string> {
  try {
    const body = await res.json()

    const fieldMessages: string[] = Array.isArray(body?.errors)
      ? body.errors
          .map((e: { defaultMessage?: unknown }) => e?.defaultMessage)
          .filter((m: unknown): m is string => typeof m === 'string' && m.length > 0)
      : []

    if (fieldMessages.length > 0) return fieldMessages.join('\n')

    if (typeof body?.message === 'string' && body.message) return body.message
    if (typeof body?.error === 'string' && body.error) return body.error
  } catch {
    // JSON 이 아니면 무시하고 아래 기본 문구를 쓴다.
  }
  return `요청이 실패했습니다 (HTTP ${res.status})`
}

async function request<T>(path: string, init?: RequestInit): Promise<T> {
  let res: Response
  try {
    res = await fetch(`${BASE_URL}${path}`, {
      headers: { 'Content-Type': 'application/json' },
      ...init,
    })
  } catch {
    throw new ApiError(0, '서버에 연결할 수 없습니다. Spring 애플리케이션이 실행 중인지 확인하세요.')
  }

  if (!res.ok) {
    throw new ApiError(res.status, await readErrorMessage(res))
  }

  // 204 No Content 처럼 본문이 없는 응답 대응
  if (res.status === 204) return undefined as T

  return (await res.json()) as T
}

export const api = {
  get: <T>(path: string) => request<T>(path),
  post: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'POST', body: JSON.stringify(body) }),
  put: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PUT', body: JSON.stringify(body) }),
  patch: <T>(path: string, body: unknown) =>
    request<T>(path, { method: 'PATCH', body: JSON.stringify(body) }),
  delete: <T>(path: string) => request<T>(path, { method: 'DELETE' }),
}

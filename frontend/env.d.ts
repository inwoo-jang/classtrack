/// <reference types="vite/client" />

interface ImportMetaEnv {
  /** 배포 시 백엔드가 다른 도메인일 때만 지정한다. 비어 있으면 Vite 프록시를 탄다. */
  readonly VITE_API_BASE_URL?: string
}

interface ImportMeta {
  readonly env: ImportMetaEnv
}

declare global {
  interface Window {
    __TRAVEL_BACKEND_ORIGIN__?: string
    __TRAVEL_PUBLIC_BACKEND_ORIGIN__?: string
    __TRAVEL_INITIAL_BACKEND_HEALTH__?: {
      status: string
      service: string
      backendPort: number
    } | null
  }
}

function normalizeConfiguredBackendOrigin(configuredOrigin: string | undefined): string | undefined {
  if (!configuredOrigin) {
    return undefined
  }

  const normalizedOrigin = configuredOrigin.trim()
  if (normalizedOrigin.length === 0) {
    return undefined
  }

  if (normalizedOrigin === 'same-origin') {
    return window.location.origin
  }

  return normalizedOrigin.replace(/\/+$/, '')
}

export function getTravelBackendOrigin(): string {
  if (window.location && (window.location.hostname === 'localhost' || window.location.hostname === '127.0.0.1')) {
    // 快捷方式默认打开 localhost 前端。
    // 这种情况下必须始终回到同 host 的 backend，否则 cookie 会落在另一套 host 上，
    // 周期性 me/session 恢复时就会把已登录用户误判成未登录。
    return `${window.location.protocol}//${window.location.hostname}:19095`
  }

  const configuredBackendOrigin = normalizeConfiguredBackendOrigin(window.__TRAVEL_BACKEND_ORIGIN__)
  if (configuredBackendOrigin) {
    return configuredBackendOrigin
  }

  const configuredPublicBackendOrigin = normalizeConfiguredBackendOrigin(window.__TRAVEL_PUBLIC_BACKEND_ORIGIN__)
  if (configuredPublicBackendOrigin) {
    return configuredPublicBackendOrigin
  }

  if (import.meta.env.VITE_TRAVEL_BACKEND_ORIGIN) {
    return import.meta.env.VITE_TRAVEL_BACKEND_ORIGIN
  }

  if (window.location && window.location.hostname) {
    return `${window.location.protocol}//${window.location.hostname}:19095`
  }

  return 'http://localhost:19095'
}

export function getTravelFrontendOrigin(): string {
  if (typeof window !== 'undefined' && window.location) {
    return window.location.origin
  }

  return 'http://localhost:5173'
}

export function getInitialBackendHealth() {
  return window.__TRAVEL_INITIAL_BACKEND_HEALTH__ ?? null
}

export {}

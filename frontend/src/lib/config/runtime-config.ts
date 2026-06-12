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
    // 本地开发默认走 localhost 前端时，后端也必须回到同一个 host。
    // 这样 cookie 才会落在同一站点上，后面的 session/me 恢复才不会误判登录状态。
    // 所以这里直接固定到本机的 19095 端口。
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

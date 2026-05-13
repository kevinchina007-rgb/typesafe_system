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
    // 蹇嵎鏂瑰紡榛樿鎵撳紑 localhost 鍓嶇銆?
    // 杩欑鎯呭喌涓嬪繀椤诲缁堝洖鍒板悓 host 鐨?backend锛屽惁鍒?cookie 浼氳惤鍦ㄥ彟涓€濂?host 涓婏紝
    // 鍛ㄦ湡鎬?me/session 鎭㈠鏃跺氨浼氭妸宸茬櫥褰曠敤鎴疯鍒ゆ垚鏈櫥褰曘€?
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

declare global {
  interface Window {
    __TRAVEL_BACKEND_ORIGIN__?: string
    __TRAVEL_INITIAL_BACKEND_HEALTH__?: {
      status: string
      service: string
      backendPort: number
    } | null
  }
}

export function getTravelBackendOrigin(): string {
  return window.__TRAVEL_BACKEND_ORIGIN__ ?? import.meta.env.VITE_TRAVEL_BACKEND_ORIGIN ?? 'http://localhost:19095'
}

export function getInitialBackendHealth() {
  return window.__TRAVEL_INITIAL_BACKEND_HEALTH__ ?? null
}

export {}

declare global {
  interface Window {
    __TRAVEL_BACKEND_ORIGIN__?: string
  }
}

export function getTravelBackendOrigin(): string {
  return window.__TRAVEL_BACKEND_ORIGIN__ ?? import.meta.env.VITE_TRAVEL_BACKEND_ORIGIN ?? 'http://localhost:19095'
}

export {}

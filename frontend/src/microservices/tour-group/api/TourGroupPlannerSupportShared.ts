import { createQueryString } from '@/shared-kernel/api/ApiTransport'

const userSessionStorageKey = 'flypig.userSessionId'

export function readUserSessionId(): string | null {
  return window.localStorage.getItem(userSessionStorageKey)
}

export function createChatQueryString(queryEntries: Record<string, string | number | boolean | null | undefined> = {}): string {
  return createQueryString({ sessionId: readUserSessionId(), ...queryEntries })
}

export function readFileAsBase64(file: File): Promise<string> {
  return new Promise((resolve, reject) => {
    const reader = new FileReader()
    reader.onload = () => {
      const result = typeof reader.result === 'string' ? reader.result : ''
      const base64 = result.includes(',') ? result.split(',').slice(1).join(',') : result
      resolve(base64)
    }
    reader.onerror = () => reject(reader.error ?? new Error('Failed to read file'))
    reader.readAsDataURL(file)
  })
}

export function normalizeTourGroupDetails<T extends { planItems?: unknown[]; planOptions?: unknown[]; selections?: unknown[]; selectionOrderLinks?: unknown[]; selectionOrderProjections?: unknown[]; blacklists?: unknown[]; bookings?: unknown[] }>(response: T): T {
  return {
    ...response,
    planItems: response.planItems ?? [],
    planOptions: response.planOptions ?? [],
    selections: response.selections ?? [],
    selectionOrderLinks: response.selectionOrderLinks ?? [],
    selectionOrderProjections: response.selectionOrderProjections ?? [],
    blacklists: response.blacklists ?? [],
    bookings: response.bookings ?? [],
  }
}


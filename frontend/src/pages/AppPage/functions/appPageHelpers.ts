import type { AppViewKey } from '@/lib/mvp-types/index'

export function normalizeViewKey(viewKey: AppViewKey): AppViewKey {
  if (viewKey === 'bookings' || viewKey === 'orders') {
    return 'flightOrders'
  }

  if (viewKey === 'explore') {
    return 'smartPlanner'
  }

  if (viewKey === 'trainAdmin' || viewKey === 'attractionAdmin') {
    return 'manager'
  }

  return viewKey
}

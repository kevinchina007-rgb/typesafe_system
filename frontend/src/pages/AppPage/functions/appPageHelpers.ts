import type { AppViewKey } from '@/lib/mvp-types/index'

// 把一些别名路由统一归一到实际页面 key。
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

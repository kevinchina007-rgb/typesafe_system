import type { DisplayFlight } from '../../objects'
import { formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'

export type RoundTripLeg = 'outbound' | 'return'

export const legTheme = {
  outbound: {
    shell: 'bg-[#fff4cf]',
    activeTab: 'bg-[#f2c94c] border-[#c99a15] text-slate-950',
    inactiveTab: 'bg-[#fff9df] border-[#e7d99a] text-slate-700',
  },
  return: {
    shell: 'bg-[#eef7dc]',
    activeTab: 'bg-[#b8d96f] border-[#7fa83a] text-slate-950',
    inactiveTab: 'bg-[#f6fbec] border-[#d7e8b2] text-slate-700',
  },
  single: {
    shell: 'bg-slate-100',
    activeTab: 'bg-sky-500 border-sky-600 text-white',
    inactiveTab: 'bg-white border-slate-300 text-slate-700',
  },
} as const

export { formatFlightRouteCity }

export function formatFlightClock(isoDateTime: string | null): string {
  if (!isoDateTime) {
    return '--:--'
  }

  return new Date(isoDateTime).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

export function formatDateLabel(date: string): string {
  return new Date(`${date}T00:00:00`).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  })
}

export function formatWeekday(date: string): string {
  return new Date(`${date}T00:00:00`).toLocaleDateString('zh-CN', {
    weekday: 'short',
  })
}

export function formatPrice(value: string | number): string {
  const numeric = Number(value)
  if (!Number.isFinite(numeric)) {
    return '--'
  }

  return numeric % 1 === 0 ? numeric.toFixed(0) : numeric.toFixed(2)
}

export function getPriceToneClass(kind: DisplayFlight['priceTone']) {
  if (kind === 'lowest') {
    return 'text-orange-500'
  }

  if (kind === 'discount') {
    return 'text-sky-600'
  }

  return 'text-slate-950'
}

export function getPriceToneLabel(kind: DisplayFlight['priceTone']) {
  if (kind === 'lowest') {
    return '最低价'
  }

  if (kind === 'discount') {
    return '折扣价'
  }

  return '标准价'
}

import type { ChangeEventHandler } from 'react'

import { flightCityOptions, getFlightDetailsPlannerCityAirportCodes } from '@/app/stores/models/flights/flightConstants'
import { unique } from './manager-panel-workspace-profile'

export type ManagerFlightSearchDraft = {
  departureCity: string
  arrivalCity: string
  departureDate: string
  timeRange: string
}

export const timeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']

export const defaultManagerFlightSearchDraft: ManagerFlightSearchDraft = {
  departureCity: '',
  arrivalCity: '',
  departureDate: '',
  timeRange: 'all',
}

export const createFlightTimeWindows = [
  { value: '00:00-03:59', label: '凌晨 00:00-03:59', start: '00:00', end: '03:59' },
  { value: '04:00-07:59', label: '清晨 04:00-07:59', start: '04:00', end: '07:59' },
  { value: '08:00-11:59', label: '上午 08:00-11:59', start: '08:00', end: '11:59' },
  { value: '12:00-15:59', label: '下午 12:00-15:59', start: '12:00', end: '15:59' },
  { value: '16:00-19:59', label: '傍晚 16:00-19:59', start: '16:00', end: '19:59' },
  { value: '20:00-23:59', label: '夜间 20:00-23:59', start: '20:00', end: '23:59' },
]

export type CreateFlightDraft = {
  flightNumber: string
  departureCity: string
  departureAirport: string
  arrivalCity: string
  arrivalAirport: string
  departureDate: string
  timeRange: string
  departureClock: string
  arrivalClock: string
  economySeatCount: number
  economyPrice: string
  economyDiscounted: boolean
  economyDiscountRate: string
  premiumEconomySeatCount: number
  premiumEconomyPrice: string
  premiumEconomyDiscounted: boolean
  premiumEconomyDiscountRate: string
  businessSeatCount: number
  businessPrice: string
  businessDiscounted: boolean
  businessDiscountRate: string
  firstSeatCount: number
  firstPrice: string
  firstDiscounted: boolean
  firstDiscountRate: string
}

export const defaultCreateFlightDraft: CreateFlightDraft = {
  flightNumber: '',
  departureCity: '',
  departureAirport: '',
  arrivalCity: '',
  arrivalAirport: '',
  departureDate: '',
  timeRange: createFlightTimeWindows[2].value,
  departureClock: createFlightTimeWindows[2].start,
  arrivalClock: '10:00',
  economySeatCount: 120,
  economyPrice: '680',
  economyDiscounted: false,
  economyDiscountRate: '10',
  premiumEconomySeatCount: 36,
  premiumEconomyPrice: '1180',
  premiumEconomyDiscounted: false,
  premiumEconomyDiscountRate: '10',
  businessSeatCount: 24,
  businessPrice: '2880',
  businessDiscounted: false,
  businessDiscountRate: '10',
  firstSeatCount: 8,
  firstPrice: '4880',
  firstDiscounted: false,
  firstDiscountRate: '10',
}

export function CitySelect({
  value,
  placeholder,
  onChange,
}: {
  value: string
  placeholder: string
  onChange: ChangeEventHandler<HTMLSelectElement>
}) {
  return (
    <select
      value={value}
      onChange={onChange}
      className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
    >
      <option value="">{placeholder}</option>
      {flightCityOptions.map(city => (
        <option key={city} value={city}>
          {city}
        </option>
      ))}
    </select>
  )
}

// 航班管理区块，负责展示航班查询结果和排序筛选。

export function clockToMinutes(clock: string): number {
  const [hourText, minuteText] = clock.split(':')
  return Number(hourText) * 60 + Number(minuteText)
}

// 把分钟数格式化回 HH:mm。
export function formatClockFromMinutes(totalMinutes: number): string {
  const hour = Math.floor(totalMinutes / 60).toString().padStart(2, '0')
  const minute = (totalMinutes % 60).toString().padStart(2, '0')
  return `${hour}:${minute}`
}

// 判断一个钟点是否落在指定时间段内。
export function isClockInsideWindow(clock: string, windowStart: string, windowEnd: string): boolean {
  const value = clockToMinutes(clock)
  return value >= clockToMinutes(windowStart) && value <= clockToMinutes(windowEnd)
}

// 在时间段内找一个默认钟点，避免表单初始值越界。
export function bumpClockInsideWindow(windowStart: string, windowEnd: string): string {
  const preferred = clockToMinutes(windowStart) + 90
  return formatClockFromMinutes(Math.min(preferred, clockToMinutes(windowEnd)))
}

// 把日期和钟点拼成后端需要的本地时间字符串。
export function buildLocalDateTime(date: string, clock: string): string {
  return `${date}T${clock}`
}

// 根据原价和折扣率计算舱位实际价格。
export function calculateCabinActualPrice(price: string, discounted: boolean, discountRate: string): string {
  const originalPrice = Number(price)
  const rate = Number(discountRate)
  if (!Number.isFinite(originalPrice) || originalPrice <= 0 || !Number.isFinite(rate) || rate <= 0) {
    return '--'
  }
  const actualPrice = discounted ? originalPrice * rate / 10 : originalPrice
  return actualPrice.toFixed(2)
}

// 展示出发或到达时间块。
export function TimeBlock({ time, airport }: { time: string; airport: string }) {
  return (
    <div className="grid gap-1">
      <strong className="text-4xl font-bold leading-none text-slate-950">{time}</strong>
      <span className="text-base text-slate-600">{airport}</span>
    </div>
  )
}

// 统一的筛选下拉框组件。
export function FilterSelect({
  label,
  value,
  options,
  onChange,
  renderOption = option => option,
}: {
  label: string
  value: string
  options: string[]
  onChange: (value: string) => void
  renderOption?: (value: string) => string
}) {
  return (
    <select
      className="h-14 w-full min-w-0 border-2 border-slate-400 bg-white px-4 text-lg text-slate-950 outline-none focus:border-sky-500"
      value={value}
      onChange={event => onChange(event.target.value)}
    >
      <option value="all">{label}</option>
      {options.map(option => (
        <option key={option} value={option}>
          {renderOption(option)}
        </option>
      ))}
    </select>
  )
}

// 校验航班查询条件是否合理。
export function validateManagerFlightSearchDraft(draft: ManagerFlightSearchDraft): string | null {
  const departureCity = draft.departureCity.trim()
  const arrivalCity = draft.arrivalCity.trim()
  if (departureCity && arrivalCity && departureCity === arrivalCity) {
    return '出发地和目的地不能选择同一个地方。'
  }

  return null
}

// 把查询草稿整理成后端搜索参数。
export function buildManagerFlightSearchPayload(draft: ManagerFlightSearchDraft, sortDirection: 'asc' | 'desc') {
  return {
    departureAirports: draft.departureCity ? getFlightDetailsPlannerCityAirportCodes(draft.departureCity) : undefined,
    arrivalAirports: draft.arrivalCity ? getFlightDetailsPlannerCityAirportCodes(draft.arrivalCity) : undefined,
    departureDate: draft.departureDate || undefined,
    timeRange: draft.timeRange === 'all' ? undefined : draft.timeRange,
    sortDirection,
  }
}

// 根据城市和现有航班数据生成机场选项。
export function buildManagerAirportOptions(cityName: string, fallbackAirportCodes: string[]): string[] {
  const cityAirportCodes = cityName ? getFlightDetailsPlannerCityAirportCodes(cityName) : []
  if (cityAirportCodes.length > 0) {
    return cityAirportCodes
  }

  return unique(fallbackAirportCodes)
}

// 航司资料草稿，保存当前页面正在编辑的基础资料。

export { CreateFlightSection } from './manager-panel-workspace-flight-create'
export { FlightManagementSection } from './manager-panel-workspace-flight-search'
export { ManagerFlightOrdersSection } from './manager-panel-workspace-flight-orders'

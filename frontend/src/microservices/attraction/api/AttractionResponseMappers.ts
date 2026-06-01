import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { AttractionTicketSessionResponse } from '@/microservices/attraction/objects/AttractionTicketSessionResponse'
import type { AttractionTicketTypeResponse } from '@/microservices/attraction/objects/AttractionTicketTypeResponse'
import type { AttractionTicketTypeRuleResponse } from '@/microservices/attraction/objects/AttractionTicketTypeRuleResponse'

type BackendMoney = {
  amount: number | string
  currency: string
}

type BackendAttractionTicketRule = {
  ruleId: string
  ticketTypeId: string
  ruleType: string
  ruleConfigJson: string
  createdAt: string
}

type BackendAttractionTicketSession = {
  sessionId: string
  ticketTypeId: string
  sessionName: string
  useDate: string
  startsAt: string
  endsAt: string
  capacity: number
  status: string
  createdAt: string
}

type BackendAttractionTicketType = {
  ticketTypeId: string
  attractionId: string
  ticketTypeName: string
  description: string
  unitPrice: BackendMoney
  availableFromDate: string
  availableToDate: string
  totalQuantity: number
  validWeekdays: string[]
  ticketTypeStatus: string
  sessions: BackendAttractionTicketSession[]
  eligibilityRules: BackendAttractionTicketRule[]
  createdAt: string
}

export type BackendAttractionResponse = {
  attractionId: string
  managerId: string
  attractionName: string
  city: string
  location: string
  description: string
  attractionStatus: string
  ticketTypes: BackendAttractionTicketType[]
  createdAt: string
}

export type BackendAttractionListResponse = {
  attractions: BackendAttractionResponse[]
}

const WEEKDAY_NAMES = ['SUNDAY', 'MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY'] as const

function formatMoneyAmount(amount: number | string): string {
  if (typeof amount === 'number' && Number.isFinite(amount)) {
    return amount.toFixed(2)
  }
  return String(amount)
}

function parseDateOnly(value: string): Date | null {
  const parsedDate = new Date(`${value}T00:00:00Z`)
  return Number.isNaN(parsedDate.getTime()) ? null : parsedDate
}

function getWeekdayName(value: string): string | null {
  const parsedDate = parseDateOnly(value)
  if (!parsedDate) {
    return null
  }
  return WEEKDAY_NAMES[parsedDate.getUTCDay()]
}

function parseRuleConfig(ruleConfigJson: string): Record<string, unknown> | null {
  try {
    const parsed = JSON.parse(ruleConfigJson) as Record<string, unknown>
    return parsed && typeof parsed === 'object' ? parsed : null
  } catch {
    return null
  }
}

function mapAttractionTicketTypeRule(rule: BackendAttractionTicketRule): AttractionTicketTypeRuleResponse {
  const config = parseRuleConfig(rule.ruleConfigJson)
  const ruleType = rule.ruleType.trim()
  const ageValue = typeof config?.ageValue === 'number' ? config.ageValue : null
  const minAge = typeof config?.minAge === 'number' ? config.minAge : null
  const maxAge = typeof config?.maxAge === 'number' ? config.maxAge : null
  const documentType = typeof config?.documentType === 'string' ? config.documentType : null
  const documentNumberPrefix = typeof config?.documentNumberPrefix === 'string' ? config.documentNumberPrefix : null

  const summary = (() => {
    if (ruleType === 'AgeLessThan' && ageValue !== null) {
      return `游玩当日年龄必须小于 ${ageValue} 岁`
    }
    if (ruleType === 'AgeBetween' && minAge !== null && maxAge !== null) {
      return `游玩当日年龄必须在 ${minAge}-${maxAge} 岁之间`
    }
    if (ruleType === 'AgeAtLeast' && (minAge !== null || ageValue !== null)) {
      return `游玩当日年龄必须至少 ${minAge ?? ageValue} 岁`
    }
    if (ruleType === 'DocumentTypeEquals' && documentType) {
      return `证件类型必须是 ${documentType}`
    }
    if (ruleType === 'DocumentNumberPrefix' && documentNumberPrefix) {
      return `证件号必须以 ${documentNumberPrefix} 开头`
    }
    return ruleType
  })()

  return {
    ruleId: rule.ruleId,
    ruleType,
    ageValue,
    minAge,
    maxAge,
    documentType,
    documentNumberPrefix,
    summary,
  }
}

function mapAttractionTicketSession(session: BackendAttractionTicketSession): AttractionTicketSessionResponse {
  return {
    sessionId: session.sessionId,
    sessionName: session.sessionName,
    useDate: session.useDate,
    startsAt: session.startsAt,
    endsAt: session.endsAt,
    capacity: session.capacity,
    availableQuantity: session.capacity,
    status: session.status,
  }
}

function mapAttractionTicketType(ticketType: BackendAttractionTicketType, useDate?: string): AttractionTicketTypeResponse {
  const requestedUseDate = useDate?.trim() ?? ''
  const ticketTypeStatus = ticketType.ticketTypeStatus.trim()
  const normalizedWeekdays = ticketType.validWeekdays.map(weekday => weekday.trim().toUpperCase()).filter(Boolean)
  const parsedUseDate = requestedUseDate ? parseDateOnly(requestedUseDate) : null
  const weekdayName = requestedUseDate ? getWeekdayName(requestedUseDate) : null
  const isWithinDateRange =
    requestedUseDate.length > 0 &&
    requestedUseDate >= ticketType.availableFromDate &&
    requestedUseDate <= ticketType.availableToDate
  const isWeekdaySupported = weekdayName ? normalizedWeekdays.includes(weekdayName) : false
  const isActive = ticketTypeStatus.toLowerCase() === 'active'
  const sessionsOnRequestedDate = ticketType.sessions.filter(session => session.useDate === requestedUseDate && session.status.trim().toLowerCase() === 'active')
  const availabilityCount =
    ticketType.sessions.length > 0
      ? sessionsOnRequestedDate.reduce((sum, session) => sum + Math.max(session.capacity, 0), 0)
      : ticketType.totalQuantity
  const availableQuantityForRequestedDate = parsedUseDate && isWithinDateRange && isWeekdaySupported && isActive ? availabilityCount : parsedUseDate ? 0 : null

  return {
    ticketTypeId: ticketType.ticketTypeId,
    ticketTypeName: ticketType.ticketTypeName,
    description: ticketType.description,
    priceAmount: formatMoneyAmount(ticketType.unitPrice.amount),
    priceCurrency: ticketType.unitPrice.currency,
    status: ticketTypeStatus,
    availableFromDate: ticketType.availableFromDate,
    availableToDate: ticketType.availableToDate,
    totalQuantity: ticketType.totalQuantity,
    validWeekdays: ticketType.validWeekdays,
    availableQuantityForRequestedDate,
    isAvailableForRequestedDate: Boolean(parsedUseDate && isWithinDateRange && isWeekdaySupported && isActive && availabilityCount > 0),
    rules: ticketType.eligibilityRules.map(mapAttractionTicketTypeRule),
    sessions: ticketType.sessions.map(mapAttractionTicketSession),
  }
}

function mapAttractionResponse(attraction: BackendAttractionResponse, useDate?: string): AttractionResponse {
  return {
    attractionId: attraction.attractionId,
    attractionName: attraction.attractionName,
    city: attraction.city,
    location: attraction.location,
    description: attraction.description,
    status: attraction.attractionStatus,
    ticketTypes: attraction.ticketTypes.map(ticketType => mapAttractionTicketType(ticketType, useDate)),
  }
}

export function mapAttractionListResponseFromBackend(response: BackendAttractionListResponse, useDate?: string): AttractionListResponse {
  return {
    attractions: response.attractions.map(attraction => mapAttractionResponse(attraction, useDate)),
  }
}

export function mapAttractionResponseFromBackend(response: BackendAttractionResponse, useDate?: string): AttractionResponse {
  return mapAttractionResponse(response, useDate)
}

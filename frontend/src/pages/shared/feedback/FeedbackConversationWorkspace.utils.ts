import type { FeedbackAudience } from '@/microservices/feedback/objects/FeedbackAudience'
import type { FeedbackManagerType } from '@/microservices/feedback/objects/FeedbackManagerType'
import type { FeedbackMessageResponse } from '@/microservices/feedback/objects/FeedbackMessageResponse'
import type { FeedbackThread } from '@/microservices/feedback/objects/FeedbackThread'
import type { OrderCancellationRequestStatus } from '@/microservices/feedback/objects/OrderCancellationRequestPayload'
import type { OrderCategory } from '@/pages/BookingsPage/objects'
import type { ChatIdentity, SupportIdentityOverride } from './FeedbackConversationWorkspace.types'

export const siteAdminIdentity: ChatIdentity = {
  name: '网站管理员',
  logoPath: null,
  fallback: '管',
}

const airlineIdentityCatalog: Array<{ name: string; logoPath: string }> = [
  { name: '奈龙航空', logoPath: '/images/airlines/NL.svg' },
  { name: '科比航空', logoPath: '/images/airlines/LD.svg' },
  { name: '双子塔航空', logoPath: '/images/airlines/TF.svg' },
  { name: '雪豹航空', logoPath: '/images/airlines/YS.svg' },
  { name: '星际穿越航空', logoPath: '/images/airlines/WX.svg' },
  { name: '祖国人航空', logoPath: '/images/airlines/ZX.svg' },
  { name: 'SpaceX航空', logoPath: '/images/airlines/JN.svg' },
  { name: '无人驾驶航空', logoPath: '/images/airlines/PM.svg' },
  { name: '卡皮巴拉航空', logoPath: '/images/airlines/NM.svg' },
  { name: '万户航空', logoPath: '/images/airlines/MH.svg' },
]

export function localizeManagerType(managerType: FeedbackManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'Airline') return translate('manager.type.airline')
  if (managerType === 'Hotel') return translate('manager.type.hotel')
  if (managerType === 'Train') return translate('manager.type.train')
  if (managerType === 'Attraction') return translate('manager.type.attraction')
  return translate('manager.type.siteAdmin')
}

export function localizeCancellationStatus(status: OrderCancellationRequestStatus) {
  if (status === 'approved') return '已同意取消'
  if (status === 'rejected') return '已拒绝取消'
  if (status === 'needMoreInfo') return '需要补充信息'
  return '等待客服处理'
}

export function isOwnMessage(senderRole: string, audience: FeedbackAudience) {
  if (audience === 'User') return senderRole === 'User'
  if (audience === 'Manager') return senderRole === 'Manager'
  return senderRole === 'SiteAdmin'
}

export function getLastMessage(thread: FeedbackThread) {
  return thread.messages[thread.messages.length - 1] ?? null
}

export function getThreadPreview(thread: FeedbackThread) {
  const lastMessage = getLastMessage(thread)
  if (!lastMessage) {
    if (thread.managerType === 'Hotel') {
      return normalizeHotelFeedbackText(thread.subtitle || thread.resourceSummaryTitle || '暂无消息')
    }
    return thread.subtitle || thread.resourceSummaryTitle || '暂无消息'
  }
  if (lastMessage.messageType === 'orderCancellationRequest') {
    return `取消订单请求：${lastMessage.payload?.reason ?? '等待查看'}`
  }
  if (lastMessage.messageType === 'complaintCard') {
    return `投诉：${lastMessage.complaintPayload?.summary ?? lastMessage.content}`
  }
  return lastMessage.content
}

export function formatListTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

export function formatCenterTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

export function shouldShowTimeMarker(messages: FeedbackMessageResponse[], index: number) {
  if (index === 0) return true
  const previous = new Date(messages[index - 1]?.createdAt ?? '').getTime()
  const current = new Date(messages[index]?.createdAt ?? '').getTime()
  if (Number.isNaN(previous) || Number.isNaN(current)) return true
  return current - previous > 10 * 60 * 1000
}

function findAirlineIdentity(text: string): ChatIdentity | null {
  const matchedAirline = airlineIdentityCatalog.find(airline => text.includes(airline.name))
  if (!matchedAirline) return null
  return {
    name: `${matchedAirline.name}客服`,
    logoPath: matchedAirline.logoPath,
    fallback: matchedAirline.name.slice(0, 1),
  }
}

export function managerTypeToOrderCategory(managerType: FeedbackManagerType): OrderCategory | null {
  if (managerType === 'Hotel') return 'hotelOrders'
  if (managerType === 'Airline') return 'flightOrders'
  if (managerType === 'Train') return 'trainOrders'
  if (managerType === 'Attraction') return 'attractionOrders'
  return null
}

function extractHotelIdentityName(text: string) {
  const trimmed = text.trim()
  if (trimmed.length === 0) return null
  const firstSegment = trimmed.split(/[|?|→]/)[0]?.trim() ?? trimmed
  const candidate = firstSegment.endsWith('客服') ? firstSegment.slice(0, -2).trim() : firstSegment
  if (candidate.length === 0) return null
  const normalized = candidate.toLowerCase().replace(/\s+/g, '')
  if (normalized === 'hotel' || normalized === '酒店' || normalized === '酒店管理员' || normalized === '酒店客服' || normalized === 'hotel客服') return null
  return candidate
}

function normalizeHotelFeedbackText(text: string) {
  return text
    .replace(/\s+路\s+/g, ' · ')
    .replace(/\s*·\s*/g, ' · ')
    .replace(/\s{2,}/g, ' ')
    .trim()
}

export function getThreadIdentity(
  thread: FeedbackThread,
  audience: FeedbackAudience,
  translate: (translationKey: string) => string,
  supportIdentityOverrides: Record<string, SupportIdentityOverride>,
  cancellationOrderTitleById: Map<string, string>,
): ChatIdentity {
  if (thread.kind === 'ManagerEscalation' && audience === 'Manager') {
    return {
      ...siteAdminIdentity,
      logoPath: thread.siteAdminActorLogoAssetPath ?? null,
    }
  }

  if (thread.managerType === 'SiteAdmin') {
    return {
      ...siteAdminIdentity,
      logoPath: thread.siteAdminActorLogoAssetPath ?? null,
    }
  }

  const override = supportIdentityOverrides[thread.threadId]
  if (override) {
    return {
      name: override.name,
      logoPath: override.logoPath,
      fallback: override.name.slice(0, 1) || '客',
    }
  }

  const searchableText = [
    thread.title,
    thread.subtitle,
    thread.resourceSummaryTitle,
    ...thread.messages.map(message => `${message.content} ${message.payload?.orderTitle ?? ''}`),
  ].join(' ')
  const airlineIdentity = findAirlineIdentity(searchableText)
  if (airlineIdentity) return airlineIdentity

  if (audience === 'Manager') {
    const userIdentityName = thread.ownerUserDisplayName.trim() || thread.title.trim() || thread.resourceSummaryTitle.trim() || '用户'
    return {
      name: userIdentityName,
      logoPath: null,
      fallback: userIdentityName.slice(0, 1) || '客',
    }
  }

  const managerName = localizeManagerType(thread.managerType, translate)
  if (thread.managerType === 'Hotel') {
    const hotelIdentityName = resolveHotelIdentityName(thread, managerName, cancellationOrderTitleById)
    return {
      name: hotelIdentityName,
      logoPath: null,
      fallback: hotelIdentityName.slice(0, 1) || '酒',
    }
  }

  return {
    name: thread.managerType === 'Airline' ? '航空公司客服' : `${managerName}客服`,
    logoPath: thread.managerType === 'Airline' ? '/images/airlines/MU.svg' : null,
    fallback: managerName.slice(0, 1) || '客',
  }
}

export function resolveHotelIdentityName(thread: FeedbackThread, fallbackName: string, cancellationOrderTitleById: Map<string, string>) {
  const orderTitle = thread.orderId ? cancellationOrderTitleById.get(thread.orderId) ?? '' : ''
  const resourceTitle = thread.resourceSummaryTitle.trim()
  const messageHotelName = thread.messages
    .map(message => message.payload?.orderTitle?.trim() ?? '')
    .find(value => value.length > 0)

  const candidates = [extractHotelIdentityName(orderTitle), extractHotelIdentityName(resourceTitle), extractHotelIdentityName(messageHotelName ?? '')].filter(
    (value): value is string => Boolean(value),
  )

  const hotelName = candidates[0]
  if (hotelName) {
    return hotelName.endsWith('客服') ? hotelName : `${hotelName}客服`
  }

  return fallbackName.endsWith('客服') ? fallbackName : `${fallbackName}客服`
}

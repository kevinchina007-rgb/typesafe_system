import { useEffect, useMemo, useRef, useState } from 'react'

import type { FeedbackAudience } from '@/microservices/content/objects/FeedbackAudience'
import type { FeedbackManagerType } from '@/microservices/content/objects/FeedbackManagerType'
import type { FeedbackMessageResponse } from '@/microservices/content/objects/FeedbackMessageResponse'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'
import type { OrderCancellationRequestStatus } from '@/microservices/content/objects/OrderCancellationRequestPayload'
import type { OrderCategory } from '@/pages/BookingsPage/objects'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

type CancellationOrderOption = {
  orderId: string
  title: string
  category: OrderCategory
}

type SupportIdentityOverride = {
  name: string
  logoPath: string | null
}

type FeedbackConversationWorkspaceProps = {
  audience: FeedbackAudience
  audienceDisplayName: string
  audienceAvatarUrl?: string | null
  emptyTitle: string
  emptyDescription: string
  threads: FeedbackThread[]
  fullScreen?: boolean
  preferredThreadId?: string | null
  cancellationOrders?: CancellationOrderOption[]
  supportIdentityOverrides?: Record<string, SupportIdentityOverride>
  translate: (translationKey: string) => string
  unreadCountSelector: (thread: FeedbackThread) => number
  onThreadChange?: (thread: FeedbackThread | null) => void
  onMarkRead: (threadId: string, audience: FeedbackAudience) => Promise<unknown> | void
  onSendMessage: (threadId: string, body: string) => Promise<unknown> | void
  onCreateCancellationRequest?: (threadId: string, orderId: string, reason: string) => Promise<unknown> | void
  onHandleCancellationRequest?: (
    threadId: string,
    messageId: string,
    status: Exclude<OrderCancellationRequestStatus, 'pending'>,
    managerNote: string,
  ) => Promise<unknown> | void
  onEscalate?: (thread: FeedbackThread) => Promise<unknown> | void
  onCreateComplaint?: (params: {
    sourceThreadId: string
    selectedMessageIds: string[]
    userExplanation: string
    userDisplayName: string
  }) => Promise<FeedbackThread | void> | FeedbackThread | void
  onOpenComplaintManagerThread?: (complaintMessageId: string) => Promise<FeedbackThread | void> | FeedbackThread | void
}

type ChatIdentity = {
  name: string
  logoPath: string | null
  fallback: string
}

const siteAdminIdentity: ChatIdentity = {
  name: '网站管理者',
  logoPath: null,
  fallback: '网',
}

const airlineIdentityCatalog: Array<{ name: string; logoPath: string }> = [
  { name: '奶龙航空', logoPath: '/images/airlines/NL.svg' },
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

function localizeManagerType(managerType: FeedbackManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'Airline') return translate('manager.type.airline')
  if (managerType === 'Hotel') return translate('manager.type.hotel')
  if (managerType === 'Train') return translate('manager.type.train')
  if (managerType === 'Attraction') return translate('manager.type.attraction')
  return translate('manager.type.siteAdmin')
}

function localizeCancellationStatus(status: OrderCancellationRequestStatus) {
  if (status === 'approved') return '已同意取消'
  if (status === 'rejected') return '已拒绝取消'
  if (status === 'needMoreInfo') return '需要补充信息'
  return '等待客服处理'
}

function isOwnMessage(senderRole: string, audience: FeedbackAudience) {
  if (audience === 'User') return senderRole === 'User'
  if (audience === 'Manager') return senderRole === 'Manager'
  return senderRole === 'SiteAdmin'
}

function getLastMessage(thread: FeedbackThread) {
  return thread.messages[thread.messages.length - 1] ?? null
}

function getThreadPreview(thread: FeedbackThread) {
  const lastMessage = getLastMessage(thread)
  if (!lastMessage) return thread.subtitle || thread.resourceSummaryTitle || '暂无消息'
  if (lastMessage.messageType === 'orderCancellationRequest') {
    return `取消订单请求：${lastMessage.payload?.reason ?? '等待查看'}`
  }
  if (lastMessage.messageType === 'complaintCard') {
    return `投诉：${lastMessage.complaintPayload?.summary ?? lastMessage.content}`
  }
  return lastMessage.content
}

function formatListTime(value: string) {
  const date = new Date(value)
  if (Number.isNaN(date.getTime())) return ''
  const now = new Date()
  if (date.toDateString() === now.toDateString()) {
    return date.toLocaleTimeString('zh-CN', { hour: '2-digit', minute: '2-digit', hour12: false })
  }
  return date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit' })
}

function formatCenterTime(value: string) {
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

function shouldShowTimeMarker(messages: FeedbackMessageResponse[], index: number) {
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

function managerTypeToOrderCategory(managerType: FeedbackManagerType): OrderCategory | null {
  if (managerType === 'Hotel') return 'hotelOrders'
  if (managerType === 'Airline') return 'flightOrders'
  if (managerType === 'Train') return 'trainOrders'
  if (managerType === 'Attraction') return 'attractionOrders'
  return null
}

function extractHotelIdentityName(text: string) {
  const trimmed = text.trim()
  if (trimmed.length === 0) return null
  const firstSegment = trimmed.split(/[·?|｜\/]/)[0]?.trim() ?? trimmed
  const candidate = firstSegment.endsWith('客服') ? firstSegment.slice(0, -2).trim() : firstSegment
  if (candidate.length === 0) return null
  const normalized = candidate.toLowerCase().replace(/\s+/g, '')
  if (normalized === 'hotel' || normalized === '酒店' || normalized === '酒店管理者' || normalized === '酒店客服' || normalized === 'hotel客服') return null
  return candidate
}

function getThreadIdentity(
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

function resolveHotelIdentityName(thread: FeedbackThread, fallbackName: string, cancellationOrderTitleById: Map<string, string>) {
  const orderTitle = thread.orderId ? cancellationOrderTitleById.get(thread.orderId) ?? '' : ''
  const resourceTitle = thread.resourceSummaryTitle.trim()
  const messageHotelName = thread.messages
    .map(message => message.payload?.orderTitle?.trim() ?? '')
    .find(value => value.length > 0)

  const candidates = [
    extractHotelIdentityName(orderTitle),
    extractHotelIdentityName(resourceTitle),
    extractHotelIdentityName(messageHotelName ?? ''),
  ].filter((value): value is string => Boolean(value))

  const hotelName = candidates[0]
  if (hotelName) {
    return hotelName.endsWith('客服') ? hotelName : `${hotelName}客服`
  }

  return `${fallbackName}客服`
}

function Avatar({
  imageUrl,
  fallback,
  alt,
  useBackendAsset = false,
}: {
  imageUrl: string | null | undefined
  fallback: string
  alt: string
  useBackendAsset?: boolean
}) {
  const className = 'flex h-11 w-11 shrink-0 items-center justify-center overflow-hidden border border-slate-200 bg-white object-contain text-sm font-bold text-slate-700'
  if (useBackendAsset) {
    return <BackendAssetImage assetUrl={imageUrl} alt={alt} className={className} fallbackContent={fallback} />
  }
  if (imageUrl) {
    return <img src={imageUrl} alt={alt} className={className} />
  }
  return <span className={className}>{fallback}</span>
}

export function FeedbackConversationWorkspace({
  audience,
  audienceDisplayName,
  audienceAvatarUrl,
  emptyTitle,
  emptyDescription,
  threads,
  fullScreen = false,
  preferredThreadId,
  cancellationOrders = [],
  supportIdentityOverrides = {},
  translate,
  unreadCountSelector,
  onThreadChange,
  onMarkRead,
  onSendMessage,
  onCreateCancellationRequest,
  onHandleCancellationRequest,
  onEscalate,
  onCreateComplaint,
  onOpenComplaintManagerThread,
}: FeedbackConversationWorkspaceProps) {
  const [activeThreadId, setActiveThreadId] = useState<string | null>(threads[0]?.threadId ?? null)
  const [draftMessage, setDraftMessage] = useState('')
  const [complaintMode, setComplaintMode] = useState(false)
  const [selectedComplaintMessageIds, setSelectedComplaintMessageIds] = useState<string[]>([])
  const [complaintExplanation, setComplaintExplanation] = useState('')
  const [complaintPreviewMessage, setComplaintPreviewMessage] = useState<FeedbackMessageResponse | null>(null)
  const [showCancellationForm, setShowCancellationForm] = useState(false)
  const [cancellationOrderId, setCancellationOrderId] = useState('')
  const [managerNotes, setManagerNotes] = useState<Record<string, string>>({})
  void fullScreen

  useEffect(() => {
    if (!threads.some(thread => thread.threadId === activeThreadId)) {
      setActiveThreadId(threads[0]?.threadId ?? null)
    }
  }, [activeThreadId, threads])

  useEffect(() => {
    if (preferredThreadId && threads.some(thread => thread.threadId === preferredThreadId)) {
      setActiveThreadId(preferredThreadId)
    }
  }, [preferredThreadId, threads])

  const activeThread = useMemo(
    () => threads.find(thread => thread.threadId === activeThreadId) ?? null,
    [activeThreadId, threads],
  )
  const cancellationOrderTitleById = useMemo(
    () => new Map(cancellationOrders.map(order => [order.orderId, order.title || order.orderId])),
    [cancellationOrders],
  )
  const activeIdentity = activeThread
    ? getThreadIdentity(activeThread, audience, translate, supportIdentityOverrides, cancellationOrderTitleById)
    : null
  const lastNotifiedThreadIdRef = useRef<string | null>(null)
  const visibleCancellationOrders = useMemo(() => {
    if (!activeThread) return cancellationOrders
    const threadCategory = managerTypeToOrderCategory(activeThread.managerType)
    const categoryOrders = threadCategory ? cancellationOrders.filter(order => order.category === threadCategory) : cancellationOrders
    if (activeThread.orderId) {
      const matchedOrders = categoryOrders.filter(order => order.orderId === activeThread.orderId)
      if (matchedOrders.length > 0) return matchedOrders
    }
    if (threadCategory) return categoryOrders
    const resourceTitle = activeThread.resourceSummaryTitle.trim()
    if (resourceTitle.length === 0) return categoryOrders
    return categoryOrders.filter(order => order.title.includes(resourceTitle) || resourceTitle.includes(order.title))
  }, [activeThread, cancellationOrders])
  useEffect(() => {
    const nextThreadId = activeThread?.threadId ?? null
    if (lastNotifiedThreadIdRef.current === nextThreadId) {
      return
    }
    lastNotifiedThreadIdRef.current = nextThreadId
    onThreadChange?.(activeThread)
  }, [activeThread, onThreadChange])

  useEffect(() => {
    if (!activeThread) return
    void onMarkRead(activeThread.threadId, audience)
  }, [activeThread?.threadId, audience, onMarkRead])

  useEffect(() => {
    if (cancellationOrderId && !visibleCancellationOrders.some(order => order.orderId === cancellationOrderId)) {
      setCancellationOrderId('')
    }
  }, [cancellationOrderId, visibleCancellationOrders])

  if (threads.length === 0) {
    return (
      <section className="mx-auto grid min-h-[calc(100vh-10rem)] w-full max-w-4xl content-start gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{emptyTitle}</h2>
        </div>
        <p className="text-sm leading-6 text-slate-500">{emptyDescription}</p>
      </section>
    )
  }

  return (
    <section className="mx-auto grid min-h-[calc(100vh-8rem)] w-full max-w-6xl overflow-hidden border border-slate-200 bg-white text-slate-950 shadow-sm shadow-slate-200/70">
      <div className="grid min-h-0 grid-cols-[19rem_minmax(0,1fr)]">
        <aside className="min-h-0 border-r border-slate-300 bg-slate-50">
          <div className="border-b border-slate-200 p-4">
            <h2 className="m-0 text-2xl font-bold text-slate-950">客服反馈</h2>
          </div>

          <div className="grid">
            {threads.map(thread => {
              const unreadCount = unreadCountSelector(thread)
              const identity = getThreadIdentity(thread, audience, translate, supportIdentityOverrides, cancellationOrderTitleById)
              const lastMessage = getLastMessage(thread)
              const isActive = thread.threadId === activeThreadId
              return (
                <button
                  key={thread.threadId}
                  type="button"
                  className={`grid grid-cols-[2.75rem_minmax(0,1fr)_3.5rem] items-center gap-3 border-b border-slate-200 p-4 text-left transition ${
                    isActive ? 'bg-white' : 'bg-slate-50 hover:bg-white'
                  }`}
                  onClick={() => {
                    setActiveThreadId(thread.threadId)
                    setDraftMessage('')
                    setShowCancellationForm(false)
                    setComplaintMode(false)
                    setSelectedComplaintMessageIds([])
                    setComplaintExplanation('')
                  }}
                >
                  <Avatar imageUrl={identity.logoPath} fallback={identity.fallback} alt={identity.name} />
                  <span className="grid min-w-0 gap-1">
                    <span className="truncate text-base font-bold text-slate-950">{identity.name}</span>
                    <span className="truncate text-sm text-slate-500">{getThreadPreview(thread)}</span>
                  </span>
                  <span className="grid justify-items-end gap-2">
                    <span className="text-xs text-slate-400">{formatListTime(lastMessage?.createdAt ?? thread.updatedAt)}</span>
                    {unreadCount > 0 ? <span className="min-w-5 bg-pink-500 px-1.5 py-0.5 text-center text-xs font-bold text-white">{unreadCount}</span> : null}
                  </span>
                </button>
              )
            })}
          </div>
        </aside>

        {activeThread && activeIdentity ? (
          <div className="grid min-h-0 grid-rows-[4.5rem_minmax(0,1fr)_auto] bg-white">
            <header className="flex items-center justify-between border-b border-slate-200 px-6">
              <div className="flex min-w-0 items-center gap-3">
                <Avatar imageUrl={activeIdentity.logoPath} fallback={activeIdentity.fallback} alt={activeIdentity.name} />
                <div className="min-w-0">
                  <h3 className="m-0 truncate text-xl font-bold text-slate-950">{activeIdentity.name}</h3>
                  <p className="m-0 truncate text-sm text-slate-500">{activeThread.resourceSummaryTitle || activeThread.subtitle}</p>
                </div>
              </div>
              {onEscalate && activeThread.kind === 'ServiceReview' ? (
                <button type="button" className="min-h-10 border border-slate-300 bg-white px-4 text-sm font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white" onClick={() => onEscalate(activeThread)}>
                  {translate('feedback.escalate')}
                </button>
              ) : null}
              {audience === 'User' && onCreateComplaint && activeThread.kind === 'ServiceReview' && activeThread.managerType !== 'SiteAdmin' ? (
                <button
                  type="button"
                  className={complaintMode ? 'min-h-10 border border-black bg-black px-4 text-sm font-bold text-white' : 'min-h-10 border border-slate-300 bg-white px-4 text-sm font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white'}
                  onClick={() => {
                    setComplaintMode(value => !value)
                    setSelectedComplaintMessageIds([])
                    setComplaintExplanation('')
                    setShowCancellationForm(false)
                  }}
                >
                  {complaintMode ? '退出投诉' : '投诉'}
                </button>
              ) : null}
            </header>

            <div className="min-h-0 overflow-y-auto bg-slate-50 px-6 py-5">
              <div className="grid gap-4">
                {activeThread.messages.map((message, index) => {
                  const ownMessage = isOwnMessage(message.senderRole, audience)
                  const senderName = ownMessage && audience === 'User' ? audienceDisplayName : message.senderDisplayName
                  const counterpartyAvatar =
                    activeThread.kind === 'ManagerEscalation' && message.senderRole === 'SiteAdmin'
                      ? { imageUrl: activeThread.siteAdminActorLogoAssetPath ?? null, fallback: siteAdminIdentity.fallback, useBackendAsset: true }
                      : activeThread.kind === 'ManagerEscalation' && message.senderRole === 'Manager'
                        ? { imageUrl: activeThread.managerActorLogoAssetPath ?? null, fallback: senderName.slice(0, 1) || '管', useBackendAsset: true }
                      : audience === 'Manager'
                        ? { imageUrl: null, fallback: senderName.slice(0, 1) || '客', useBackendAsset: false }
                        : { imageUrl: activeIdentity.logoPath, fallback: activeIdentity.fallback, useBackendAsset: false }
                  const avatar = ownMessage
                    ? { imageUrl: audienceAvatarUrl, fallback: audienceDisplayName.slice(0, 1) || '我', useBackendAsset: true }
                    : counterpartyAvatar
                  const canSelectForComplaint = complaintMode && message.messageType !== 'system'
                  const isSelectedForComplaint = selectedComplaintMessageIds.includes(message.messageId)

                  return (
                    <div key={message.messageId} className={canSelectForComplaint ? 'grid grid-cols-[2rem_minmax(0,1fr)] items-start gap-3' : 'grid gap-3'}>
                      {canSelectForComplaint ? (
                        <button
                          type="button"
                          aria-label={isSelectedForComplaint ? '取消选择这条消息' : '选择这条消息'}
                          className={isSelectedForComplaint ? 'mt-8 flex h-6 w-6 items-center justify-center rounded-full bg-sky-600 text-sm font-bold text-white' : 'mt-8 h-6 w-6 rounded-full border-2 border-slate-300 bg-white'}
                          onClick={() => {
                            setSelectedComplaintMessageIds(previous =>
                              previous.includes(message.messageId)
                                ? previous.filter(messageId => messageId !== message.messageId)
                                : [...previous, message.messageId],
                            )
                          }}
                        >
                          {isSelectedForComplaint ? '✓' : ''}
                        </button>
                      ) : null}
                      <div className="grid gap-3">
                      {shouldShowTimeMarker(activeThread.messages, index) ? (
                        <div className="justify-self-center bg-slate-200 px-3 py-1 text-xs font-semibold text-slate-500">{formatCenterTime(message.createdAt)}</div>
                      ) : null}

                      {message.messageType === 'system' ? (
                        <div className="justify-self-center bg-white px-4 py-2 text-sm font-semibold text-slate-500 shadow-sm shadow-slate-200/50">
                          {message.content}
                        </div>
                      ) : null}

                      {message.messageType === 'orderCancellationRequest' && message.payload ? (
                        <div className={ownMessage ? 'grid grid-cols-[minmax(0,1fr)_2.75rem] gap-3 justify-self-end' : 'grid grid-cols-[2.75rem_minmax(0,1fr)] gap-3 justify-self-start'}>
                          {!ownMessage ? <Avatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
                          <article className="grid max-w-xl gap-3 border border-pink-200 bg-white p-4 shadow-sm shadow-pink-100">
                            <div className="flex flex-wrap items-start justify-between gap-3">
                              <div>
                                <p className="m-0 text-xs font-bold text-pink-600">{senderName}</p>
                                <h4 className="m-0 text-lg font-bold text-slate-950">取消订单请求</h4>
                              </div>
                              <span className="border border-pink-200 bg-pink-50 px-3 py-1 text-sm font-bold text-pink-700">{localizeCancellationStatus(message.payload.status)}</span>
                            </div>
                            <div className="grid gap-1 text-sm leading-6 text-slate-600">
                              <span>订单编号：{message.payload.orderId}</span>
                              <span>订单名称：{message.payload.orderTitle ?? '订单'}</span>
                              <span>取消原因：{message.payload.reason}</span>
                              {message.payload.requestedRefundAmount !== null ? <span>预计可退：￥{message.payload.requestedRefundAmount}</span> : null}
                              {message.payload.managerNote ? <span>客服备注：{message.payload.managerNote}</span> : null}
                              {message.payload.status === 'pending' && audience === 'User' ? <strong className="text-pink-700">等待客服处理</strong> : null}
                            </div>
                            {audience !== 'User' && message.payload.status === 'pending' && onHandleCancellationRequest ? (
                              <div className="grid gap-3">
                                <textarea
                                  rows={3}
                                  value={managerNotes[message.messageId] ?? ''}
                                  onChange={event => setManagerNotes(previous => ({ ...previous, [message.messageId]: event.target.value }))}
                                  className="min-h-20 border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
                                />
                                <div className="flex flex-wrap gap-3">
                                  {(['approved', 'rejected', 'needMoreInfo'] as const).map(nextStatus => (
                                    <button
                                      key={nextStatus}
                                      type="button"
                                      className={nextStatus === 'approved' ? 'min-h-10 border border-pink-500 bg-pink-500 px-4 py-2 font-bold text-white' : 'min-h-10 border border-slate-300 bg-white px-4 py-2 font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white'}
                                      onClick={() => {
                                        void onHandleCancellationRequest(activeThread.threadId, message.messageId, nextStatus, managerNotes[message.messageId] ?? '')
                                      }}
                                    >
                                      {nextStatus === 'approved' ? '同意取消' : nextStatus === 'rejected' ? '拒绝取消' : '需要补充信息'}
                                    </button>
                                  ))}
                                </div>
                              </div>
                            ) : null}
                          </article>
                          {ownMessage ? <Avatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
                        </div>
                      ) : null}

                      {message.messageType === 'complaintCard' && message.complaintPayload ? (
                        <div className={ownMessage ? 'grid grid-cols-[minmax(0,1fr)_2.75rem] gap-3 justify-self-end' : 'grid grid-cols-[2.75rem_minmax(0,1fr)] gap-3 justify-self-start'}>
                          {!ownMessage ? <Avatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
                          <article className="grid w-[26rem] max-w-full gap-3 border border-slate-200 bg-white p-4 text-left shadow-sm shadow-slate-200/70">
                            <button type="button" className="grid gap-2 text-left" onClick={() => setComplaintPreviewMessage(message)}>
                              <span className="text-xs font-bold text-slate-500">投诉记录</span>
                              <strong className="text-lg leading-tight text-slate-950">投诉对象：{message.complaintPayload.targetDisplayName}</strong>
                              <span className="line-clamp-2 text-sm leading-6 text-slate-600">用户说明：{message.complaintPayload.userExplanation}</span>
                              <span className="line-clamp-2 text-sm leading-6 text-slate-500">摘要：{message.complaintPayload.summary}</span>
                              <span className="text-sm font-bold text-pink-600">查看{message.complaintPayload.selectedMessages.length}条投诉消息</span>
                            </button>
                            {audience === 'SiteAdmin' && onOpenComplaintManagerThread ? (
                              <button
                                type="button"
                                className="min-h-10 border border-black bg-black px-4 py-2 text-sm font-bold text-white hover:bg-white hover:text-black"
                                onClick={() => void onOpenComplaintManagerThread(message.messageId)}
                              >
                                与被投诉者进行对话
                              </button>
                            ) : null}
                          </article>
                          {ownMessage ? <Avatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
                        </div>
                      ) : null}

                      {message.messageType === 'text' ? (
                        <div className={ownMessage ? 'grid grid-cols-[minmax(0,1fr)_2.75rem] gap-3 justify-self-end' : 'grid grid-cols-[2.75rem_minmax(0,1fr)] gap-3 justify-self-start'}>
                          {!ownMessage ? <Avatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
                          <div className={ownMessage ? 'grid justify-items-end gap-1' : 'grid justify-items-start gap-1'}>
                            <span className="text-xs font-bold text-slate-500">{senderName}</span>
                            <article className={ownMessage ? 'max-w-xl bg-sky-100 p-3 text-slate-950' : 'max-w-xl bg-white p-3 text-slate-950 shadow-sm shadow-slate-200/60'}>
                              <p className="m-0 whitespace-pre-wrap text-base leading-7">{message.content}</p>
                            </article>
                          </div>
                          {ownMessage ? <Avatar imageUrl={avatar.imageUrl} fallback={avatar.fallback} alt={senderName} useBackendAsset={avatar.useBackendAsset} /> : null}
                        </div>
                      ) : null}
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>

            <footer className="border-t border-slate-200 bg-white p-4">
              {complaintMode ? (
                <form
                  className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
                  onSubmit={async event => {
                    event.preventDefault()
                    if (!activeThread || !onCreateComplaint) return
                    if (selectedComplaintMessageIds.length === 0 || complaintExplanation.trim().length === 0) return
                    const nextThread = await onCreateComplaint({
                      sourceThreadId: activeThread.threadId,
                      selectedMessageIds: selectedComplaintMessageIds,
                      userExplanation: complaintExplanation,
                      userDisplayName: audienceDisplayName,
                    })
                    if (nextThread && typeof nextThread === 'object' && 'threadId' in nextThread) {
                      setActiveThreadId(nextThread.threadId)
                    }
                    setComplaintMode(false)
                    setSelectedComplaintMessageIds([])
                    setComplaintExplanation('')
                  }}
                >
                  <div className="flex flex-wrap items-center justify-between gap-3">
                    <strong className="text-base text-slate-950">已选择 {selectedComplaintMessageIds.length} 条消息</strong>
                    <button
                      type="button"
                      className="min-h-9 border border-slate-300 bg-white px-3 text-sm font-bold text-slate-950 hover:border-black hover:bg-black hover:text-white"
                      onClick={() => {
                        setComplaintMode(false)
                        setSelectedComplaintMessageIds([])
                        setComplaintExplanation('')
                      }}
                    >
                      退出
                    </button>
                  </div>
                  <textarea
                    className="min-h-24 resize-none border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
                    rows={3}
                    value={complaintExplanation}
                    onChange={event => setComplaintExplanation(event.target.value)}
                    placeholder="请说明你要投诉的问题。"
                    required
                  />
                  <button
                    className="justify-self-end min-h-10 border border-pink-500 bg-pink-500 px-6 py-2 text-sm font-bold text-white hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-50"
                    type="submit"
                    disabled={selectedComplaintMessageIds.length === 0 || complaintExplanation.trim().length === 0}
                  >
                    提交给网站管理者
                  </button>
                </form>
              ) : null}

              {!complaintMode && audience === 'User' && onCreateCancellationRequest ? (
                <div className="grid gap-3">
                  <button
                    type="button"
                    className="w-fit min-h-10 border border-pink-500 bg-white px-4 py-2 text-sm font-bold text-pink-600 hover:bg-pink-500 hover:text-white"
                    onClick={() => setShowCancellationForm(value => !value)}
                  >
                    申请取消订单
                  </button>
                  {showCancellationForm ? (
                    <form
                      className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
                      onSubmit={event => {
                        event.preventDefault()
                        const trimmedMessage = draftMessage.trim()
                        if (trimmedMessage.length === 0) return
                        if (cancellationOrderId) {
                          void onCreateCancellationRequest(activeThread.threadId, cancellationOrderId, trimmedMessage)
                        } else {
                          void onSendMessage(activeThread.threadId, trimmedMessage)
                        }
                        setDraftMessage('')
                        setCancellationOrderId('')
                        setShowCancellationForm(false)
                      }}
                    >
                      <select className="min-h-12 border border-slate-300 bg-white px-3 text-base" value={cancellationOrderId} onChange={event => setCancellationOrderId(event.target.value)}>
                        <option value="">请选择订单</option>
                        {visibleCancellationOrders.map(order => (
                          <option key={order.orderId} value={order.orderId}>
                            {order.title || order.orderId}
                          </option>
                        ))}
                      </select>
                      <textarea
                        className="min-h-28 border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
                        rows={4}
                        value={draftMessage}
                        onChange={event => setDraftMessage(event.target.value)}
                        placeholder={cancellationOrderId ? '请写明你想取消订单的原因，例如时间不合适、价格变化、行程有变等。' : '不选择订单时，这里会作为普通消息直接发送。'}
                      />
                      <button type="submit" className="w-fit min-h-11 border border-pink-500 bg-pink-500 px-5 py-2 font-bold text-white">
                        {cancellationOrderId ? '提交取消请求' : '发送'}
                      </button>
                    </form>
                  ) : null}
                </div>
              ) : null}

              {!showCancellationForm && !complaintMode ? (
                <form
                  className="mt-3 grid gap-3"
                  onSubmit={event => {
                    event.preventDefault()
                    if (draftMessage.trim().length === 0) return
                    void onSendMessage(activeThread.threadId, draftMessage)
                    setDraftMessage('')
                  }}
                >
                  <textarea
                    className="min-h-24 resize-none border border-slate-300 bg-white p-3 text-base outline-none focus:border-black"
                    rows={3}
                    value={draftMessage}
                    onChange={event => setDraftMessage(event.target.value)}
                  />
                  <button className="justify-self-end min-h-10 border border-pink-500 bg-pink-500 px-6 py-2 text-sm font-bold text-white hover:bg-pink-600" type="submit">
                    {translate('feedback.send')}
                  </button>
                </form>
              ) : null}
            </footer>
          </div>
        ) : null}
      </div>
      {complaintPreviewMessage?.complaintPayload ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-black/45 p-6" role="dialog" aria-modal="true">
          <section className="grid max-h-[80vh] w-full max-w-2xl grid-rows-[auto_minmax(0,1fr)_auto] overflow-hidden bg-white text-slate-950 shadow-2xl">
            <header className="flex items-center justify-between border-b border-slate-200 p-4">
              <div>
                <p className="m-0 text-sm font-bold text-slate-500">投诉消息</p>
                <h3 className="m-0 text-xl font-bold">{complaintPreviewMessage.complaintPayload.targetDisplayName}</h3>
              </div>
              <button type="button" className="h-10 w-10 bg-black text-lg font-bold text-white" onClick={() => setComplaintPreviewMessage(null)}>
                ×
              </button>
            </header>
            <div className="min-h-0 overflow-y-auto bg-slate-50 p-5">
              <div className="mb-4 grid gap-2 border border-slate-200 bg-white p-4">
                <strong>用户说明</strong>
                <p className="m-0 whitespace-pre-wrap text-sm leading-6 text-slate-600">{complaintPreviewMessage.complaintPayload.userExplanation}</p>
              </div>
              <div className="grid gap-4">
                {complaintPreviewMessage.complaintPayload.selectedMessages.map((snapshot, index, snapshots) => {
                  const ownSnapshot = snapshot.senderRole === 'User'
                  return (
                    <div key={`${snapshot.messageId}-${index}`} className="grid gap-3">
                      {shouldShowTimeMarker(snapshots.map(item => ({ createdAt: item.createdAt }) as FeedbackMessageResponse), index) ? (
                        <div className="justify-self-center bg-slate-200 px-3 py-1 text-xs font-semibold text-slate-500">{formatCenterTime(snapshot.createdAt)}</div>
                      ) : null}
                      <div className={ownSnapshot ? 'grid justify-items-end gap-1' : 'grid justify-items-start gap-1'}>
                        <span className="text-xs font-bold text-slate-500">{snapshot.senderDisplayName}</span>
                        <article className={ownSnapshot ? 'max-w-xl bg-sky-100 p-3 text-slate-950' : 'max-w-xl bg-white p-3 text-slate-950 shadow-sm shadow-slate-200/60'}>
                          <p className="m-0 whitespace-pre-wrap text-base leading-7">{snapshot.content}</p>
                        </article>
                      </div>
                    </div>
                  )
                })}
              </div>
            </div>
            <footer className="border-t border-slate-200 p-4 text-sm text-slate-500">以上为用户选择提交给网站管理者的聊天记录。</footer>
          </section>
        </div>
      ) : null}
    </section>
  )
}













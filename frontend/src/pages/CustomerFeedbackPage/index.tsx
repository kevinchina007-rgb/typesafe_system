import { useEffect, useState } from 'react'

import { createOrderCancellationMessage, markFeedbackThreadRead, sendFeedbackMessage, setActiveFeedbackMiniThread, useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import { getFlightAirlineDisplayNameByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import type { AppLanguage, UserResponse } from '@/lib/mvp-types/index'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { OrderLineItemResponse } from '@/microservices/order/objects/OrderLineItemResponse'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { FeedbackConversationWorkspace } from '@/pages/shared/feedback/FeedbackConversationWorkspace'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

type CustomerFeedbackPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export function CustomerFeedbackPage({ currentLanguage, signedInUser, translate, onShowNotice }: CustomerFeedbackPageProps) {
  const threads = useFeedbackChatStore(state => state.userThreads)
  const activeThread = useFeedbackChatStore(state => state.activeMiniThread)
  const loadUserThreads = useFeedbackChatStore(state => state.loadUserThreads)
  const [cancellationOrders, setCancellationOrders] = useState<{ orderId: string; title: string }[]>([])
  void currentLanguage
  void onShowNotice

  useEffect(() => {
    if (!signedInUser) return

    void loadUserThreads()
    void travelMvpApiClient.listOrders(signedInUser.userId).then(response => {
      setCancellationOrders(response.orders.map(order => ({
        orderId: order.orderId,
        title: buildCancellationOrderTitle(order),
      })))
    })
  }, [loadUserThreads, signedInUser])

  if (!signedInUser) {
    return (
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <p className="text-sm leading-6 text-slate-500">{translate('feedback.userGuest')}</p>
      </section>
    )
  }

  return (
    <>
      <FeedbackConversationWorkspace
        audience="User"
        audienceDisplayName={signedInUser.nickname}
        audienceAvatarUrl={signedInUser.avatarUrl}
        emptyTitle={translate('feedback.userTitle')}
        emptyDescription={translate('feedback.userEmpty')}
        fullScreen
        preferredThreadId={activeThread?.threadId ?? null}
        threads={threads}
        cancellationOrders={cancellationOrders}
        translate={translate}
        unreadCountSelector={thread => thread.unreadByUser}
        onMarkRead={markFeedbackThreadRead}
        onSendMessage={(threadId, body) =>
          sendFeedbackMessage({
            threadId,
            senderRole: 'User',
            senderDisplayName: signedInUser.nickname,
            body,
          })
        }
        onCreateCancellationRequest={(threadId, orderId, reason) =>
          createOrderCancellationMessage({
            threadId,
            orderId,
            reason,
          })
        }
        onThreadChange={thread => setActiveFeedbackMiniThread(thread)}
      />
    </>
  )
}

function buildCancellationOrderTitle(order: OrderResponse) {
  const flightItem = order.orderLineItems.find(item => item.flightDetails) ?? order.orderLineItems[0]
  if (!flightItem) {
    return `${order.orderType} · ${order.totalPrice} ${order.orderCurrency}`
  }

  const flight = buildFlightOrderSummary(flightItem)
  if (!flight) {
    return `${flightItem.summaryLabel || order.orderType} · ${order.totalPrice} ${order.orderCurrency}`
  }

  return `${flight.airlineName} ${flight.flightNumber} · ${formatFlightRouteCity(flight.departureAirport)} → ${formatFlightRouteCity(flight.arrivalAirport)} · ${formatFlightDate(flight.departureTime)} · ${formatCabinClass(flight.cabinClass)} · ${order.totalPrice} ${order.orderCurrency}`
}

function buildFlightOrderSummary(orderLineItem: OrderLineItemResponse) {
  const snapshot = parseFlightSnapshot(orderLineItem.summaryLabel)
  const details = orderLineItem.flightDetails
  if (!details && !snapshot) return null

  const airlineCode = details?.airlineCode ?? snapshot?.airlineCode ?? ''
  return {
    airlineName: getFlightAirlineDisplayNameByCode(airlineCode, details?.airlineName ?? snapshot?.airlineName ?? '航空公司'),
    flightNumber: details?.flightNumber ?? snapshot?.flightNumber ?? snapshot?.flightId ?? '',
    departureAirport: details?.departureAirport ?? snapshot?.departureAirport ?? '',
    arrivalAirport: details?.arrivalAirport ?? snapshot?.arrivalAirport ?? '',
    departureTime: details?.departureTime ?? snapshot?.departureTime ?? '',
    arrivalTime: details?.arrivalTime ?? snapshot?.arrivalTime ?? '',
    cabinClass: details?.cabinClass ?? snapshot?.cabinClass ?? '',
  }
}

type FlightSnapshotSummary = {
  airlineName?: string
  airlineCode?: string
  flightNumber?: string
  flightId?: string
  departureAirport?: string
  arrivalAirport?: string
  departureTime?: string
  arrivalTime?: string
  cabinClass?: string
}

function parseFlightSnapshot(summaryLabel: string): FlightSnapshotSummary | null {
  if (!summaryLabel.trim().startsWith('{')) return null

  try {
    const parsed = JSON.parse(summaryLabel) as Record<string, unknown>
    return {
      airlineName: getStringField(parsed, 'airlineName'),
      airlineCode: getStringField(parsed, 'airlineCode'),
      flightNumber: getStringField(parsed, 'flightNumber'),
      flightId: getStringField(parsed, 'flightId'),
      departureAirport: getStringField(parsed, 'departureAirport') ?? getStringField(parsed, 'departureAirportCode'),
      arrivalAirport: getStringField(parsed, 'arrivalAirport') ?? getStringField(parsed, 'arrivalAirportCode'),
      departureTime: getStringField(parsed, 'departureTime') ?? getStringField(parsed, 'departureAt'),
      arrivalTime: getStringField(parsed, 'arrivalTime') ?? getStringField(parsed, 'arrivalAt'),
      cabinClass: getStringField(parsed, 'cabinClass'),
    }
  } catch {
    return null
  }
}

function getStringField(record: Record<string, unknown>, key: string) {
  const value = record[key]
  return typeof value === 'string' && value.trim() ? value : undefined
}

function formatFlightDate(departureTime: string) {
  const date = new Date(departureTime)
  return Number.isNaN(date.getTime())
    ? departureTime.slice(0, 10).replaceAll('-', '/')
    : date.toLocaleDateString('zh-CN', { month: '2-digit', day: '2-digit', weekday: 'short' })
}

function formatCabinClass(cabinClass: string) {
  const normalizedCabinClass = cabinClass.trim().toUpperCase()
  const cabinClassLabelMap: Record<string, string> = {
    ECONOMY: '经济舱',
    PREMIUM_ECONOMY: '超级经济舱',
    BUSINESS: '商务舱',
    FIRST: '头等舱',
  }
  return cabinClassLabelMap[normalizedCabinClass] ?? (cabinClass || '舱位未注明')
}

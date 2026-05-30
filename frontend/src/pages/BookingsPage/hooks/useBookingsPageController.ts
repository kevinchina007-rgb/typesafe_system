import { useEffect, useState } from 'react'

import { ensureOrderCancellationThread, setActiveFeedbackMiniThread } from '@/app/stores/feedback-chat-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { OrderResponse, ReviewResponse, TravelerResponse, PaymentLinkResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'
import type { BookingsPageController, BookingsPageProps, PaymentMethodValue } from '../objects'

export function useBookingsPageController({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: BookingsPageProps): BookingsPageController {
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])
  const [pendingPaymentOrder, setPendingPaymentOrder] = useState<OrderResponse | null>(null)
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  useEffect(() => {
    void reloadOrders()
    void reloadReviews()
    void reloadTravelers()
  }, [signedInUser?.userId])

  async function reloadOrders() {
    if (!signedInUser) {
      setOrders([])
      return
    }
    const orderListResponse = await travelMvpApiClient.listOrders(signedInUser.userId)
    setOrders(orderListResponse.orders)
  }

  async function reloadReviews() {
    if (!signedInUser) {
      setReviews([])
      return
    }
    const reviewListResponse = await travelMvpApiClient.listMyReviews(signedInUser.userId)
    setReviews(reviewListResponse.reviews)
  }

  async function reloadTravelers() {
    if (!signedInUser) {
      setTravelers([])
      return
    }
    const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUser.userId)
    setTravelers(travelerListResponse.travelers)
  }

  function requireSignedInUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

  return {
    orders,
    reviews,
    travelers,
    pendingPaymentOrder,
    isAuthDialogOpen,
    isBusy,
    isGuestMode: signedInUser === null,
    setPendingPaymentOrder,
    setIsAuthDialogOpen,
    onRequireLogin: () => setIsAuthDialogOpen(true),
    onAuthDialogClose: () => setIsAuthDialogOpen(false),
    onAuthDialogConfirm: () => {
      setIsAuthDialogOpen(false)
      setPendingPaymentOrder(null)
      onNavigate('account')
    },
    onOpenPayment: order => {
      if (!signedInUser) {
        setIsAuthDialogOpen(true)
        return
      }
      setPendingPaymentOrder(order)
    },
    onReloadOrders: async () => {
      await runPageAction(async () => {
        await reloadOrders()
      }, translate('bookings.refresh'), translate('notice.actionSuccess'))
    },
    onCancelOrder: async orderId => {
      await runPageAction(async () => {
        await travelMvpApiClient.cancelOrder(orderId)
        await reloadOrders()
      }, translate('bookings.cancel'), translate('notice.actionSuccess'))
    },
    onRequestRefund: async (orderId, refundReason) => {
      await runPageAction(async () => {
        await travelMvpApiClient.requestRefund(orderId, { refundReason })
        await reloadOrders()
      }, translate('bookings.requestRefund'), translate('notice.actionSuccess'))
    },
    onDeleteReview: async reviewId => {
      const nextSignedInUser = requireSignedInUser()
      await runPageAction(async () => {
        await travelMvpApiClient.deleteReview(reviewId, { userId: nextSignedInUser.userId })
        await reloadReviews()
      }, translate('reviews.delete'), translate('notice.actionSuccess'))
    },
    onOpenOrderCancellationFeedback: async orderId => {
      const nextSignedInUser = requireSignedInUser()
      const thread = await ensureOrderCancellationThread({ userId: nextSignedInUser.userId, orderId })
      onNavigate('customerFeedback')
      setActiveFeedbackMiniThread(thread)
    },
    onCreatePaymentLink: payload =>
      travelMvpApiClient.createPaymentLink(payload.orderId, requireSignedInUser().userId, payload.paymentMethod as PaymentMethodValue, currentLanguage) as Promise<PaymentLinkResponse>,
    onConfirmPayment: async payload => {
      await runPageAction(async () => {
        await travelMvpApiClient.payOrder(payload.orderId, {
          paymentMethod: payload.paymentMethod,
          paymentSucceeded: true,
          travelerIds: payload.travelerIds,
        })
        await reloadOrders()
        setPendingPaymentOrder(null)
      }, translate('bookings.pay'), translate('notice.paymentSuccess'))
    },
  }
}

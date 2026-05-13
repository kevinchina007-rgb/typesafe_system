import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
﻿import { useEffect, useState } from 'react'

import { createReviewFeedbackThread, setActiveFeedbackMiniThread, setPendingFeedbackReviewDraft } from '@/app/stores/feedback-chat-store'
import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { OrderPanel } from '@/pages/BookingsPage/components/OrderPanel'
import { PaymentModal } from '@/pages/BookingsPage/components/PaymentModal'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, OrderResponse, ReviewResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'

type BookingsPageProps = {
  currentLanguage: AppLanguage
  isSessionReady: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function BookingsPage({
  currentLanguage,
  isSessionReady,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: BookingsPageProps) {
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])
  const [pendingPaymentOrder, setPendingPaymentOrder] = useState<OrderResponse | null>(null)
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  function requireSignedInUser() {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUser
  }

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

  useEffect(() => {
    void reloadOrders()
    void reloadReviews()
    void reloadTravelers()
  }, [signedInUser?.userId])

  return (
    <>
      <OrderPanel
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isGuestMode={isSessionReady && signedInUser === null}
        orders={orders}
        reviews={reviews}
        travelers={travelers}
        translate={translate}
        onRequireLogin={() => setIsAuthDialogOpen(true)}
        onReloadOrders={async () => {
          await runPageAction(async () => {
            await reloadOrders()
          }, translate('bookings.refresh'), translate('notice.actionSuccess'))
        }}
        onOpenPayment={order => {
          if (!signedInUser) {
            setIsAuthDialogOpen(true)
            return
          }
          setPendingPaymentOrder(order)
        }}
        onCancelOrder={async orderId => {
          await runPageAction(async () => {
            await travelMvpApiClient.cancelOrder(orderId)
            await reloadOrders()
          }, translate('bookings.cancel'), translate('notice.actionSuccess'))
        }}
        onRequestRefund={async (orderId, refundReason) => {
          await runPageAction(async () => {
            await travelMvpApiClient.requestRefund(orderId, { refundReason })
            await reloadOrders()
          }, translate('bookings.requestRefund'), translate('notice.actionSuccess'))
        }}
        onDeleteReview={async reviewId => {
          const nextSignedInUser = requireSignedInUser()
          await runPageAction(async () => {
            await travelMvpApiClient.deleteReview(reviewId, { userId: nextSignedInUser.userId })
            await reloadReviews()
          }, translate('reviews.delete'), translate('notice.actionSuccess'))
        }}
        onOpenFeedbackForReview={async reviewId => {
          const thread = await createReviewFeedbackThread(reviewId)
          setActiveFeedbackMiniThread(thread)
          onNavigate('customerFeedback')
        }}
        onStartReviewInFeedback={async payload => {
          const nextSignedInUser = requireSignedInUser()
          const eligibility = await travelMvpApiClient.getReviewEligibility({
            userId: nextSignedInUser.userId,
            orderItemId: payload.orderItemId,
          })

          if (!eligibility.canReview) {
            if (eligibility.alreadyReviewed) {
              const reviewListResponse = await travelMvpApiClient.listMyReviews(nextSignedInUser.userId)
              const existingReview = reviewListResponse.reviews.find(review => review.orderItemId === payload.orderItemId)
              if (existingReview) {
                const thread = await createReviewFeedbackThread(existingReview.reviewId)
                setActiveFeedbackMiniThread(thread)
                setPendingFeedbackReviewDraft(null)
                await reloadReviews()
                onNavigate('customerFeedback')
                return
              }
            }

            throw new Error(eligibility.reason ?? translate('reviews.notEligible'))
          }

          setPendingFeedbackReviewDraft({
            orderId: payload.orderId,
            orderItemId: payload.orderItemId,
            title: payload.title,
            eligibility,
          })
          onNavigate('customerFeedback')
        }}
      />

      <PaymentModal
        isOpen={pendingPaymentOrder !== null}
        order={pendingPaymentOrder}
        isBusy={isBusy}
        translate={translate}
        onClose={() => setPendingPaymentOrder(null)}
        onCreatePaymentLink={payload =>
          travelMvpApiClient.createPaymentLink(payload.orderId, payload.paymentMethod, currentLanguage)
        }
        onConfirmPayment={async payload => {
          await runPageAction(async () => {
            await travelMvpApiClient.payOrder(payload.orderId, {
              paymentMethod: payload.paymentMethod,
              paymentSucceeded: true,
            })
            await reloadOrders()
            setPendingPaymentOrder(null)
          }, translate('bookings.pay'), translate('notice.paymentSuccess'))
        }}
      />

      <AuthRequiredDialog
        isOpen={isAuthDialogOpen}
        title={translate('authRequired.paymentTitle')}
        description={translate('authRequired.paymentDescription')}
        translate={translate}
        onClose={() => setIsAuthDialogOpen(false)}
        onConfirm={() => {
          setIsAuthDialogOpen(false)
          setPendingPaymentOrder(null)
          onNavigate('account')
        }}
      />
    </>
  )
}

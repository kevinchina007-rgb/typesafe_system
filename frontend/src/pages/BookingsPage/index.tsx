import { useEffect, useState } from 'react'

import { OrderPanel } from '../../components/OrderPanel'
import { PaymentModal } from '../../components/PaymentModal'
import { travelMvpApiClient } from '../../lib/api-client'
import type { AppLanguage, OrderResponse, ReviewResponse, TravelerResponse, UserResponse } from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'

type BookingsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export function BookingsPage({
  currentLanguage,
  signedInUser,
  translate,
  onShowNotice,
}: BookingsPageProps) {
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])
  const [pendingPaymentOrder, setPendingPaymentOrder] = useState<OrderResponse | null>(null)
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

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
        isGuestMode={signedInUser === null}
        orders={orders}
        reviews={reviews}
        travelers={travelers}
        translate={translate}
        onReloadOrders={async () => {
          await runPageAction(async () => {
            await reloadOrders()
          }, translate('bookings.refresh'), translate('notice.actionSuccess'))
        }}
        onOpenPayment={order => {
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
        onLoadReviewEligibility={async orderItemId => {
          const nextSignedInUser = requireSignedInUser()
          return travelMvpApiClient.getReviewEligibility({
            userId: nextSignedInUser.userId,
            orderItemId,
          })
        }}
        onUploadReviewImage={async imageFile => {
          const nextSignedInUser = requireSignedInUser()
          return runPageActionWithResult(
            () => travelMvpApiClient.uploadReviewImage(nextSignedInUser.userId, imageFile),
            translate('content.imagesUpload'),
            translate('notice.actionSuccess'),
          )
        }}
        onCreateReview={async payload => {
          const nextSignedInUser = requireSignedInUser()
          await runPageAction(async () => {
            await travelMvpApiClient.createReview({
              userId: nextSignedInUser.userId,
              orderId: payload.orderId,
              orderItemId: payload.orderItemId,
              rating: payload.rating,
              title: payload.title,
              content: payload.content,
              images: payload.images,
            })
            await reloadReviews()
          }, translate('reviews.submit'), translate('notice.actionSuccess'))
        }}
        onUpdateReview={async (reviewId, payload) => {
          const nextSignedInUser = requireSignedInUser()
          await runPageAction(async () => {
            await travelMvpApiClient.updateReview(reviewId, {
              userId: nextSignedInUser.userId,
              rating: payload.rating,
              title: payload.title,
              content: payload.content,
              images: payload.images,
            })
            await reloadReviews()
          }, translate('reviews.save'), translate('notice.actionSuccess'))
        }}
        onDeleteReview={async reviewId => {
          const nextSignedInUser = requireSignedInUser()
          await runPageAction(async () => {
            await travelMvpApiClient.deleteReview(reviewId, { userId: nextSignedInUser.userId })
            await reloadReviews()
          }, translate('reviews.delete'), translate('notice.actionSuccess'))
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
      />
    </>
  )
}

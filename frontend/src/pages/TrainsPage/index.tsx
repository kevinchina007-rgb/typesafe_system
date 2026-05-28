import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import { useState } from 'react'

import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { TrainsPanel } from '@/pages/TrainsPage/components/TrainsPanel'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, ResourceReviewSummaryResponse, ReviewResponse, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'

type TrainsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function TrainsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: TrainsPageProps) {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)

  async function loadReviewSummary(payload: {
    resourceType: string
    resourceId: string
  }): Promise<ResourceReviewSummaryResponse> {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    return travelMvpApiClient.getReviewResourceSummary({
      userId: signedInUser.userId,
      resourceType: payload.resourceType,
      resourceId: payload.resourceId,
    })
  }

  async function loadReviewsByResource(payload: {
    resourceType: string
    resourceId: string
  }): Promise<ReviewResponse[]> {
    if (!signedInUser) {
      throw new Error(translate('error.loginRequired'))
    }
    const response = await travelMvpApiClient.listReviewsByResource({
      userId: signedInUser.userId,
      resourceType: payload.resourceType,
      resourceId: payload.resourceId,
    })
    return response.reviews
  }

  return (
    <>
      <TrainsPanel
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isGuestMode={signedInUser === null}
        travelers={travelers}
        translate={translate}
        onRequireLogin={() => setIsAuthDialogOpen(true)}
        onSearchTrains={async payload => {
          const trainListResponse = await travelMvpApiClient.listTrains(payload)
          return trainListResponse.trains
        }}
        onBookTrain={async payload => {
          if (!signedInUser) {
            setIsAuthDialogOpen(true)
            return
          }
          await runPageAction(async () => {
            const createdOrder = await travelMvpApiClient.createOrder({
              ownerUserId: signedInUser.userId,
              orderCurrency: payload.orderCurrency,
            })
            await travelMvpApiClient.addTrainItemToOrder(createdOrder.orderId, {
              trainId: payload.trainId,
              travelerIds: payload.travelerIds,
              fromStationCode: payload.fromStationCode,
              toStationCode: payload.toStationCode,
              seatClass: payload.seatClass,
              seatPreference: payload.seatPreference,
            })
            onNavigate('bookings')
          }, translate('trains.bookNow'), translate('notice.bookingCreated'))
        }}
        onLoadReviewSummary={loadReviewSummary}
        onLoadReviews={loadReviewsByResource}
      />

      <AuthRequiredDialog
        isOpen={isAuthDialogOpen}
        title={translate('authRequired.bookingTitle')}
        description={translate('authRequired.bookingDescription')}
        translate={translate}
        onClose={() => setIsAuthDialogOpen(false)}
        onConfirm={() => {
          setIsAuthDialogOpen(false)
          onNavigate('account')
        }}
      />
    </>
  )
}

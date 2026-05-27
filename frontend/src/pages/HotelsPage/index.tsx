import { useState } from 'react'

import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { HotelsPanel } from '@/pages/HotelsPage/components/HotelsPanel'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, ResourceReviewSummaryResponse, ReviewResponse, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'

type HotelsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function HotelsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: HotelsPageProps) {
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
      <HotelsPanel
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isGuestMode={signedInUser === null}
        travelers={travelers}
        translate={translate}
        onRequireLogin={() => setIsAuthDialogOpen(true)}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onSearchHotels={async payload => {
          const hotelListResponse = await travelMvpApiClient.searchHotelsPlanner(payload)
          return hotelListResponse.hotels
        }}
        onBookHotel={async payload => {
          if (!signedInUser) {
            setIsAuthDialogOpen(true)
            return
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createHotelOrder({
              userId: signedInUser.userId,
              roomTypeId: payload.roomTypeId,
              guestTravelerIds: payload.guestTravelerIds,
              checkInDate: payload.checkInDate,
              checkOutDate: payload.checkOutDate,
              roomCount: payload.roomCount,
            })
            onNavigate('hotelOrders')
          }, translate('hotels.bookNow'), translate('notice.bookingCreated'))
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

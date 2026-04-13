import { useState } from 'react'

import { AuthRequiredDialog } from '../../components/AuthRequiredDialog'
import { AttractionsPanel } from '../../components/AttractionsPanel'
import { travelMvpApiClient } from '../../lib/api-client'
import type {
  AppLanguage,
  AppViewKey,
  ResourceReviewSummaryResponse,
  ReviewResponse,
  UserResponse,
} from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'
import { useSignedInTravelers } from '../shared/useSignedInTravelers'

type AttractionsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function AttractionsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: AttractionsPageProps) {
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
      <AttractionsPanel
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isGuestMode={signedInUser === null}
        travelers={travelers}
        translate={translate}
        onRequireLogin={() => setIsAuthDialogOpen(true)}
        onSearchAttractions={async payload => {
          const attractionListResponse = await travelMvpApiClient.listAttractions(payload)
          const detailedAttractions = await Promise.all(
            attractionListResponse.attractions.map(async attractionSummary => {
              try {
                return await travelMvpApiClient.getAttraction(attractionSummary.attractionId, { useDate: payload.useDate })
              } catch {
                return attractionSummary
              }
            }),
          )
          return detailedAttractions
        }}
        onBookAttraction={async payload => {
          if (!signedInUser) {
            setIsAuthDialogOpen(true)
            return
          }
          await runPageAction(async () => {
            const createdOrder = await travelMvpApiClient.createOrder({
              ownerUserId: signedInUser.userId,
              orderCurrency: payload.orderCurrency,
            })
            await travelMvpApiClient.addAttractionItemToOrder(createdOrder.orderId, {
              buyerUserId: signedInUser.userId,
              orderId: createdOrder.orderId,
              attractionId: payload.attractionId,
              ticketTypeId: payload.ticketTypeId,
              sessionId: payload.sessionId,
              travelerIds: payload.travelerIds,
              useDate: payload.useDate,
            })
            onNavigate('bookings')
          }, translate('attractions.bookNow'), translate('notice.bookingCreated'))
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

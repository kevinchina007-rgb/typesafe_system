import { TrainsPanel } from '../../components/TrainsPanel'
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
    <TrainsPanel
      currentLanguage={currentLanguage}
      isBusy={isBusy}
      isGuestMode={signedInUser === null}
      travelers={travelers}
      translate={translate}
      onSearchTrains={async payload => {
        const trainListResponse = await travelMvpApiClient.listTrains(payload)
        return trainListResponse.trains
      }}
      onBookTrain={async payload => {
        if (!signedInUser) {
          throw new Error(translate('error.loginRequired'))
        }
        await runPageAction(async () => {
          const createdOrder = await travelMvpApiClient.createOrder({
            ownerUserId: signedInUser.userId,
            orderCurrency: payload.orderCurrency,
          })
          await travelMvpApiClient.addTrainItemToOrder(createdOrder.orderId, {
            buyerUserId: signedInUser.userId,
            orderId: createdOrder.orderId,
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
  )
}

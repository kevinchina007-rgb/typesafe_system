import { useCallback, useState } from 'react'

import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { FlightsPanel } from '@/pages/FlightsPage/components/FlightsPanel'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import { usePageActions } from '@/pages/shared/usePageActions'
import { useSignedInTravelers } from '@/pages/shared/useSignedInTravelers'

type FlightsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function FlightsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: FlightsPageProps) {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)
  const [isAuthDialogOpen, setIsAuthDialogOpen] = useState(false)
  const loadDailyLowestPrices = useCallback(
    (payload: Parameters<typeof travelMvpApiClient.listFlightDailyLowestPrices>[0]) =>
      travelMvpApiClient.listFlightDailyLowestPrices(payload),
    [],
  )

  return (
    <>
      <FlightsPanel
        isBusy={isBusy}
        isGuestMode={signedInUser === null}
        signedInUserId={signedInUser?.userId ?? null}
        travelers={travelers}
        translate={translate}
        onRequireLogin={() => setIsAuthDialogOpen(true)}
        onSearchFlights={async payload => {
          const flightListResponse = await travelMvpApiClient.listFlights(payload)
          return flightListResponse.flights
        }}
        onLoadDailyLowestPrices={loadDailyLowestPrices}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onBookFlight={async payload => {
          if (!signedInUser) {
            setIsAuthDialogOpen(true)
            return
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createFlightOrder({
              userId: payload.userId,
              flightId: payload.flightId,
              travelerIds: payload.travelerIds,
              cabinClass: payload.cabinClass,
            })
            onNavigate('bookings')
          }, translate('flights.bookNow'), translate('notice.bookingCreated'))
        }}
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

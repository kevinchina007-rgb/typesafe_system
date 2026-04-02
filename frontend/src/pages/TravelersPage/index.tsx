import { useEffect, useState } from 'react'

import { TravelerPanel } from '../../components/TravelerPanel'
import { travelMvpApiClient } from '../../lib/api-client'
import type { AppLanguage, TravelerResponse, UserResponse } from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'

type TravelersPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSignedInUserChange: (user: UserResponse | null) => void
  onShowNotice: PageNoticeHandler
}

export function TravelersPage({
  currentLanguage,
  signedInUser,
  translate,
  onSignedInUserChange,
  onShowNotice,
}: TravelersPageProps) {
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  async function reloadTravelers() {
    if (!signedInUser) {
      setTravelers([])
      return
    }

    const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUser.userId)
    setTravelers(travelerListResponse.travelers)
  }

  async function reloadCurrentUser() {
    if (!signedInUser) {
      return
    }
    const userResponse = await travelMvpApiClient.getUser(signedInUser.userId)
    onSignedInUserChange(userResponse)
  }

  useEffect(() => {
    void reloadTravelers()
  }, [signedInUser?.userId])

  return (
    <TravelerPanel
      currentLanguage={currentLanguage}
      isBusy={isBusy}
      isGuestMode={signedInUser === null}
      travelers={travelers}
      translate={translate}
      onCreateTraveler={async payload => {
        if (!signedInUser) {
          throw new Error(translate('error.loginRequired'))
        }
        await runPageAction(async () => {
          await travelMvpApiClient.createTraveler(signedInUser.userId, payload)
          await Promise.all([reloadCurrentUser(), reloadTravelers()])
        }, translate('travelers.add'), translate('notice.travelerSaved'))
      }}
      onUpdateTraveler={async payload => {
        if (!signedInUser) {
          throw new Error(translate('error.loginRequired'))
        }
        if (!payload.travelerId) {
          throw new Error('Missing traveler id')
        }
        const travelerId = payload.travelerId
        await runPageAction(async () => {
          await travelMvpApiClient.updateTraveler(signedInUser.userId, travelerId, {
            fullName: payload.fullName,
            documentType: payload.documentType,
            documentNumber: payload.documentNumber,
            phone: payload.phone,
            birthDate: payload.birthDate,
            seatPreference: payload.seatPreference,
            mealPreference: payload.mealPreference,
            accessibilityRequestNotes: payload.accessibilityRequestNotes.trim() || null,
            emergencyContactName: payload.emergencyContactName.trim() || null,
            emergencyContactPhoneNumber: payload.emergencyContactPhoneNumber.trim() || null,
            isDefaultTraveler: payload.isDefaultTraveler,
          })
          await Promise.all([reloadCurrentUser(), reloadTravelers()])
        }, translate('travelers.saveEdit'), translate('notice.travelerSaved'))
      }}
      onDeleteTraveler={async travelerId => {
        if (!signedInUser) {
          throw new Error(translate('error.loginRequired'))
        }
        await runPageAction(async () => {
          await travelMvpApiClient.deleteTraveler(signedInUser.userId, travelerId)
          await Promise.all([reloadCurrentUser(), reloadTravelers()])
        }, translate('travelers.delete'), translate('notice.travelerDeleted'))
      }}
      onReloadTravelers={async () => {
        await runPageAction(async () => {
          await reloadTravelers()
        }, translate('travelers.refresh'), translate('notice.actionSuccess'))
      }}
    />
  )
}

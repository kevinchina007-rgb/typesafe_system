import { useEffect, useState } from 'react'

import type { AppLanguage, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { TravelerProfileInput } from '@/microservices/traveler/objects/TravelerProfileInput'
import { TravelerPanel } from '@/pages/TravelersPage/components/TravelerPanel'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import { usePageActions } from '@/pages/shared/usePageActions'

type TravelersPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSignedInUserChange: (user: UserResponse | null) => void
  onShowNotice: PageNoticeHandler
}

type TravelerDraftForUpdate = {
  fullName: string
  gender: string
  nationality: string
  documentType: string
  documentNumber: string
  documentExpiryDate: string
  phone: string
  email: string
  birthDate: string
  seatPreference: string
  mealPreference: string
  quietSeatPreferred: boolean
  assistanceType: string
  requirementNote: string
  hasLargeLuggage: boolean
  luggageNote: string
  emergencyContactName: string
  emergencyContactPhoneNumber: string
  isDefaultTraveler: boolean
}

function buildTravelerProfileInput(payload: TravelerDraftForUpdate): TravelerProfileInput {
  const requirementNote = payload.requirementNote.trim() || null
  return {
    fullName: payload.fullName.trim(),
    documentType: payload.documentType,
    documentNumber: payload.documentNumber.trim(),
    phone: payload.phone.trim(),
    birthDate: payload.birthDate,
    seatPreference: payload.seatPreference,
    mealPreference: payload.mealPreference,
    accessibilityRequestNotes: requirementNote,
    emergencyContactName: payload.emergencyContactName.trim() || null,
    emergencyContactPhoneNumber: payload.emergencyContactPhoneNumber.trim() || null,
    isDefaultTraveler: payload.isDefaultTraveler,
    basicInfo: {
      fullName: payload.fullName.trim(),
      gender: payload.gender,
      birthDate: payload.birthDate,
      nationality: payload.nationality.trim() || '中国',
    },
    documentInfo: {
      documentType: payload.documentType,
      documentNumber: payload.documentNumber.trim(),
      documentExpiryDate: payload.documentExpiryDate || null,
    },
    contactInfo: {
      phone: payload.phone.trim(),
      email: payload.email.trim() || null,
    },
    preferenceInfo: {
      seatPreference: payload.seatPreference,
      mealPreference: payload.mealPreference,
      quietSeatPreferred: payload.quietSeatPreferred,
    },
    specialRequirementInfo: {
      assistanceType: payload.assistanceType,
      requirementNote,
      hasLargeLuggage: payload.hasLargeLuggage,
      luggageNote: payload.luggageNote.trim() || null,
    },
  }
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
          await travelMvpApiClient.updateTraveler(signedInUser.userId, travelerId, buildTravelerProfileInput(payload))
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

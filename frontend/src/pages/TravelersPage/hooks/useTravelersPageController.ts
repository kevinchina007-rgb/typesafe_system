// 本文件定义 TravelersPage 页面的状态控制逻辑，负责条件维护、请求触发和动作调度。

import { useEffect, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { usePageActions } from '@/pages/shared/usePageActions'
import type { TravelerResponse } from '@/lib/mvp-types/index'
import type { TravelersPageController, TravelersPageProps } from '../objects'
import { buildTravelerPayload, createTravelerFormDraft } from '../functions'

export function useTravelersPageController({
  currentLanguage,
  signedInUser,
  translate,
  onSignedInUserChange,
  onShowNotice,
}: TravelersPageProps): TravelersPageController {
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

  return {
    currentLanguage,
    isBusy,
    isGuestMode: signedInUser === null,
    travelers,
    translate,
    onCreateTraveler: async payload => {
      if (!signedInUser) {
        throw new Error(translate('error.loginRequired'))
      }
      await runPageAction(async () => {
        await travelMvpApiClient.createTraveler(signedInUser.userId, payload)
        await Promise.all([reloadCurrentUser(), reloadTravelers()])
      }, translate('travelers.add'), translate('notice.travelerSaved'))
    },
    onUpdateTraveler: async payload => {
      if (!signedInUser) {
        throw new Error(translate('error.loginRequired'))
      }
      if (!payload.travelerId) {
        throw new Error('Missing traveler id')
      }
      const travelerId = payload.travelerId
      await runPageAction(async () => {
        await travelMvpApiClient.updateTraveler(signedInUser.userId, travelerId, buildTravelerPayload(payload))
        await Promise.all([reloadCurrentUser(), reloadTravelers()])
      }, translate('travelers.saveEdit'), translate('notice.travelerSaved'))
    },
    onSetDefaultTraveler: async traveler => {
      if (!signedInUser) {
        throw new Error(translate('error.loginRequired'))
      }
      await runPageAction(async () => {
        const payload = buildTravelerPayload({
          ...createTravelerFormDraft(traveler),
          isDefaultTraveler: true,
        })
        await travelMvpApiClient.updateTraveler(signedInUser.userId, traveler.travelerId, payload)
        await Promise.all([reloadCurrentUser(), reloadTravelers()])
      }, translate('travelers.primaryToggle'), translate('notice.travelerSaved'))
    },
    onDeleteTraveler: async travelerId => {
      if (!signedInUser) {
        throw new Error(translate('error.loginRequired'))
      }
      await runPageAction(async () => {
        await travelMvpApiClient.deleteTraveler(signedInUser.userId, travelerId)
        await Promise.all([reloadCurrentUser(), reloadTravelers()])
      }, translate('travelers.delete'), translate('notice.travelerDeleted'))
    },
    onReloadTravelers: async () => {
      await runPageAction(async () => {
        await reloadTravelers()
      }, translate('travelers.refresh'), translate('notice.actionSuccess'))
    },
  }
}

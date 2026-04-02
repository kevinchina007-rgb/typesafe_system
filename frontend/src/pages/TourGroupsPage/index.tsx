import { TourGroupsPanel } from '../../components/TourGroupsPanel'
import { travelMvpApiClient } from '../../lib/api-client'
import type { AppLanguage, AppViewKey, UserResponse } from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'
import { useSignedInTravelers } from '../shared/useSignedInTravelers'

type TourGroupsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function TourGroupsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: TourGroupsPageProps) {
  const { travelers } = useSignedInTravelers(signedInUser)
  const { isBusy, runPageAction, runPageActionWithResult } = usePageActions(currentLanguage, translate, onShowNotice)

  return (
    <TourGroupsPanel
      currentLanguage={currentLanguage}
      isBusy={isBusy}
      signedInUser={signedInUser}
      travelers={travelers}
      translate={translate}
      onListGroups={async () => {
        const response = await travelMvpApiClient.listTourGroups()
        return response.groups
      }}
      onLoadGroupDetails={groupId => travelMvpApiClient.getTourGroup(groupId)}
      onCreateGroup={payload =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroup(payload),
          translate('tourGroups.createGroup'),
          translate('notice.actionSuccess'),
        )
      }
      onJoinGroup={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.joinTourGroup(groupId, payload),
          translate('tourGroups.joinGroup'),
          translate('notice.actionSuccess'),
        )
      }
      onAddMembershipTraveler={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.addTourGroupMembershipTraveler(groupId, payload),
          translate('tourGroups.addMembershipTraveler'),
          translate('notice.actionSuccess'),
        )
      }
      onCreatePlanItem={(groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroupPlanItem(groupId, payload),
          translate('tourGroups.createPlanItem'),
          translate('notice.actionSuccess'),
        )
      }
      onCreatePlanOption={(planItemId, groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroupPlanOption(planItemId, groupId, payload),
          translate('tourGroups.createOption'),
          translate('notice.actionSuccess'),
        )
      }
      onCreateSelection={(planItemId, groupId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.createTourGroupSelection(planItemId, groupId, payload),
          translate('tourGroups.createSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onSubmitSelection={(selectionId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.submitTourGroupSelection(selectionId, payload),
          translate('tourGroups.submitSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onConfirmSelection={(selectionId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.confirmTourGroupSelection(selectionId, payload),
          translate('tourGroups.confirmSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onRejectSelection={(selectionId, payload) =>
        runPageActionWithResult(
          () => travelMvpApiClient.rejectTourGroupSelection(selectionId, payload),
          translate('tourGroups.rejectSelection'),
          translate('notice.actionSuccess'),
        )
      }
      onSearchFlights={async payload => {
        const flightListResponse = await travelMvpApiClient.listFlights(payload)
        return flightListResponse.flights
      }}
      onSearchHotels={async payload => {
        const hotelListResponse = await travelMvpApiClient.listHotels(payload)
        return hotelListResponse.hotels
      }}
      onSearchTrains={async payload => {
        const trainListResponse = await travelMvpApiClient.listTrains(payload)
        return trainListResponse.trains
      }}
      onSearchAttractions={async payload => {
        const attractionListResponse = await travelMvpApiClient.listAttractions(payload)
        const detailedAttractions = await Promise.all(
          attractionListResponse.attractions.map(async attractionSummary => {
            try {
              return await travelMvpApiClient.getAttraction(attractionSummary.attractionId)
            } catch {
              return attractionSummary
            }
          }),
        )
        return detailedAttractions
      }}
      onOpenBookings={async () => {
        await runPageAction(async () => {
          onNavigate('bookings')
        }, translate('nav.bookings'), translate('notice.actionSuccess'))
      }}
    />
  )
}

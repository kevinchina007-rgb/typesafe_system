import { useEffect } from 'react'

import type { AppLanguage, AttractionResponse, FlightPlannerResponse, GroupPlanItemResponse, GroupPlanOptionResponse, GroupSelectionOrderProjectionResponse, GroupPlanSelectionResponse, HotelPlannerResponse, TourGroupMembershipResponse, TrainResponse, UserResponse } from '@/lib/mvp-types/index'
import { TourGroupPlanComposer } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer'
import { TourGroupMemberManagementSection } from '@/pages/TourGroupsPage/components/TourGroupMemberManagementSection'
import { TourGroupPlanSection } from '@/pages/TourGroupsPage/components/TourGroupPlanSection'
import { TourGroupSelectionList } from '@/pages/TourGroupsPage/components/TourGroupSelectionList'

type TourGroupOrganizerWorkspaceProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  activePlanItem: GroupPlanItemResponse | null
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  selectionOrderProjections: GroupSelectionOrderProjectionResponse[]
  pendingApprovals: GroupPlanSelectionResponse[]
  activeMembership: TourGroupMembershipResponse | null
  memberships: TourGroupMembershipResponse[]
  blacklists: import('@/lib/mvp-types/index').TourGroupBlacklistResponse[]
  organizerUserId: string
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onCreatePlanItem: (payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<GroupPlanItemResponse | null>
  onSelectPlanItem: (planItem: GroupPlanItemResponse) => void
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightPlannerResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelPlannerResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onCreateOption: (
    planItemId: string,
    payload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) => Promise<void>
  onConfirmSelection: (selectionId: string, note: string) => Promise<void>
  onRejectSelection: (selectionId: string, note: string) => Promise<void>
  onBatchConfirmSelections: (selectionIds: string[]) => Promise<void>
  onBatchRejectSelections: (selectionIds: string[], note: string) => Promise<void>
  onKickMember: (targetUserId: string) => Promise<void>
  onBlacklistMember: (targetUserId: string) => Promise<void>
  onTransferOrganizer: (targetUserId: string) => Promise<void>
}

export function TourGroupOrganizerWorkspace({
  currentLanguage,
  isBusy,
  activePlanItem,
  planItems,
  planOptions,
  selectionOrderProjections,
  pendingApprovals,
  activeMembership,
  memberships,
  blacklists,
  organizerUserId,
  signedInUser,
  translate,
  onCreatePlanItem,
  onSelectPlanItem,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
  onCreateOption,
  onConfirmSelection,
  onRejectSelection,
  onBatchConfirmSelections,
  onBatchRejectSelections,
  onKickMember,
  onBlacklistMember,
  onTransferOrganizer,
}: TourGroupOrganizerWorkspaceProps) {
  useEffect(() => {
    if (!activePlanItem && planItems.length > 0) {
      onSelectPlanItem(
        [...planItems].sort((left, right) => left.sequenceNo - right.sequenceNo)[0],
      )
    }
  }, [activePlanItem, onSelectPlanItem, planItems])

  return (
    <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_20rem] xl:items-start">
      <div className="grid gap-4">
        <TourGroupPlanSection
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          isOrganizer
          planItems={planItems}
          planOptions={planOptions}
          activePlanItemId={activePlanItem?.planItemId ?? null}
          translate={translate}
          onSelectPlanItem={onSelectPlanItem}
        />
        <TourGroupSelectionList
          currentLanguage={currentLanguage}
          title={translate('tourGroups.pendingApprovals')}
          eyebrow={translate('tourGroups.pendingApprovalsEyebrow')}
          isBusy={isBusy}
          selections={pendingApprovals}
          planItems={planItems}
          planOptions={planOptions}
          selectionOrderProjections={selectionOrderProjections}
          activeMembership={activeMembership}
          translate={translate}
          onConfirmSelection={onConfirmSelection}
          onRejectSelection={onRejectSelection}
          onBatchConfirmSelections={onBatchConfirmSelections}
          onBatchRejectSelections={onBatchRejectSelections}
        />
      </div>

      <aside className="grid gap-4">
        <TourGroupPlanComposer
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          existingPlanItems={planItems}
          translate={translate}
          onSearchFlights={onSearchFlights}
          onSearchHotels={onSearchHotels}
          onSearchTrains={onSearchTrains}
          onSearchAttractions={onSearchAttractions}
          onCreatePlanItem={onCreatePlanItem}
          onCreateOptionForPlanItem={async (planItemId, payload) => {
            await onCreateOption(planItemId, payload)
          }}
        />
        <TourGroupMemberManagementSection
          memberships={memberships}
          blacklists={blacklists}
          organizerUserId={organizerUserId}
          signedInUser={signedInUser}
          isBusy={isBusy}
          translate={translate}
          onKickMember={onKickMember}
          onBlacklistMember={onBlacklistMember}
          onTransferOrganizer={onTransferOrganizer}
        />
      </aside>
    </div>
  )
}

import { useEffect } from 'react'

import type { AppLanguage, AttractionResponse, FlightResponse, GroupPlanItemResponse, GroupPlanOptionResponse, GroupSelectionOrderProjectionResponse, GroupPlanSelectionResponse, HotelResponse, TourGroupMembershipResponse, TrainResponse } from '@/lib/mvp-types/index'
import { TourGroupPlanComposer } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer'
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
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelResponse[]>
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
}: TourGroupOrganizerWorkspaceProps) {
  useEffect(() => {
    if (!activePlanItem && planItems.length > 0) {
      onSelectPlanItem(
        [...planItems].sort((left, right) => left.sequenceNo - right.sequenceNo)[0],
      )
    }
  }, [activePlanItem, onSelectPlanItem, planItems])

  return (
    <div className="grid gap-5 xl:grid-cols-[1fr_20rem]">
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
      </aside>
    </div>
  )
}

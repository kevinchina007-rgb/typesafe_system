import { useEffect, useState } from 'react'

import type {
  AppLanguage,
  AttractionResponse,
  FlightResponse,
  GroupPlanItemResponse,
  HotelResponse,
  TourGroupDetailsResponse,
  TrainResponse,
  TravelerResponse,
  UserResponse,
} from '../lib/mvp-types'
import { localizeTourGroupStatus } from '../lib/view-models'
import { TourGroupMemberWorkspace } from './TourGroupMemberWorkspace'
import { TourGroupOrganizerWorkspace } from './TourGroupOrganizerWorkspace'

type TourGroupDetailProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onJoinGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onCreatePlanItem: (payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<GroupPlanItemResponse | null>
  activePlanItem: GroupPlanItemResponse | null
  onSelectPlanItem: (planItem: GroupPlanItemResponse) => void
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
  onOpenChoose: (planItem: GroupPlanItemResponse) => void
  onSubmitSelection: (selectionId: string) => Promise<void>
  onConfirmSelection: (selectionId: string, note: string) => Promise<void>
  onRejectSelection: (selectionId: string, note: string) => Promise<void>
  onOpenBookings: () => void
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
}

export function TourGroupDetail({
  currentLanguage,
  details,
  signedInUser,
  travelers,
  isBusy,
  translate,
  activePlanItem,
  onJoinGroup,
  onAddMembershipTraveler,
  onCreatePlanItem,
  onSelectPlanItem,
  onCreateOption,
  onOpenChoose,
  onSubmitSelection,
  onConfirmSelection,
  onRejectSelection,
  onOpenBookings,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
}: TourGroupDetailProps) {
  const isOrganizer = signedInUser?.userId === details.group.organizerUserId
  const [workspaceMode, setWorkspaceMode] = useState<'manage' | 'member'>('manage')
  const activeMembership =
    details.memberships.find(
      membership => membership.userId === signedInUser?.userId && membership.status === 'Active',
    ) ?? null

  const mySelections = activeMembership
    ? details.selections.filter(selection => selection.membershipId === activeMembership.membershipId)
    : []
  const linkedSelectionIds = details.selectionOrderLinks.map(link => link.selectionId)
  const pendingApprovals = details.selections.filter(selection => selection.status === 'Submitted')

  useEffect(() => {
    if (!isOrganizer) {
      setWorkspaceMode('member')
    }
  }, [isOrganizer])

  return (
    <section className="tour-group-detail-shell">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('tourGroups.detailEyebrow')}</p>
          <h2>{details.group.title}</h2>
          <p>{details.group.description}</p>
        </div>
        <span className="tag-chip">{localizeTourGroupStatus(details.group.status, currentLanguage)}</span>
      </div>

      <div className="detail-grid">
        <div>
          <span className="detail-label">{translate('tourGroups.destination')}</span>
          <strong>{details.group.destination}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.dateRange')}</span>
          <strong>{`${details.group.startDate} - ${details.group.endDate}`}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.capacity')}</span>
          <strong>{`${details.group.usedCapacity} / ${details.group.capacity}`}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.organizer')}</span>
          <strong>{details.group.organizerUserId}</strong>
        </div>
      </div>

      {isOrganizer ? (
        <div className="tour-group-mode-switch">
          <button
            type="button"
            className={workspaceMode === 'manage' ? 'secondary-button' : undefined}
            disabled={isBusy}
            onClick={() => setWorkspaceMode('manage')}
          >
            {translate('tourGroups.manageGroup')}
          </button>
          <button
            type="button"
            className={workspaceMode === 'member' ? 'secondary-button' : undefined}
            disabled={isBusy}
            onClick={() => setWorkspaceMode('member')}
          >
            {translate('tourGroups.enterGroup')}
          </button>
        </div>
      ) : null}

      {isOrganizer && workspaceMode === 'manage' ? (
        <TourGroupOrganizerWorkspace
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          activePlanItem={activePlanItem}
          planItems={details.planItems}
          planOptions={details.planOptions}
          pendingApprovals={pendingApprovals}
          activeMembership={activeMembership}
          translate={translate}
          onCreatePlanItem={onCreatePlanItem}
          onSelectPlanItem={onSelectPlanItem}
          onSearchFlights={onSearchFlights}
          onSearchHotels={onSearchHotels}
          onSearchTrains={onSearchTrains}
          onSearchAttractions={onSearchAttractions}
          onCreateOption={onCreateOption}
          onConfirmSelection={onConfirmSelection}
          onRejectSelection={onRejectSelection}
        />
      ) : (
        <TourGroupMemberWorkspace
          currentLanguage={currentLanguage}
          details={details}
          signedInUser={signedInUser}
          travelers={travelers}
          activeMembership={activeMembership}
          mySelections={mySelections}
          linkedSelectionIds={linkedSelectionIds}
          isBusy={isBusy}
          translate={translate}
          onJoinGroup={onJoinGroup}
          onAddMembershipTraveler={onAddMembershipTraveler}
          onOpenChoose={onOpenChoose}
          onSubmitSelection={onSubmitSelection}
          onOpenBookings={onOpenBookings}
        />
      )}
    </section>
  )
}

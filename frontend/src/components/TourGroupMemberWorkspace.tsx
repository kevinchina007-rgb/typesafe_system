import type {
  AppLanguage,
  GroupPlanItemResponse,
  GroupPlanSelectionResponse,
  GroupSelectionOrderProjectionResponse,
  TourGroupDetailsResponse,
  TourGroupMembershipResponse,
  TravelerResponse,
  UserResponse,
} from '../lib/mvp-types'
import { TourGroupBookingSummary } from './TourGroupBookingSummary'
import { TourGroupMemberSection } from './TourGroupMemberSection'
import { TourGroupPlanSection } from './TourGroupPlanSection'
import { TourGroupSelectionList } from './TourGroupSelectionList'

type TourGroupMemberWorkspaceProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  activeMembership: TourGroupMembershipResponse | null
  mySelections: GroupPlanSelectionResponse[]
  linkedSelectionIds: string[]
  selectionOrderProjections: GroupSelectionOrderProjectionResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onJoinGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onOpenChoose: (planItem: GroupPlanItemResponse) => void
  onSubmitSelection: (selectionId: string) => Promise<void>
  onBatchPaySelections: (selectionIds: string[]) => Promise<void>
  onOpenBookings: () => void
}

export function TourGroupMemberWorkspace({
  currentLanguage,
  details,
  signedInUser,
  travelers,
  activeMembership,
  mySelections,
  linkedSelectionIds,
  selectionOrderProjections,
  isBusy,
  translate,
  onJoinGroup,
  onAddMembershipTraveler,
  onOpenChoose,
  onSubmitSelection,
  onBatchPaySelections,
  onOpenBookings,
}: TourGroupMemberWorkspaceProps) {
  return (
    <div className="tour-group-member-stack">
      <TourGroupMemberSection
        currentLanguage={currentLanguage}
        details={details}
        signedInUser={signedInUser}
        activeMembership={activeMembership}
        travelers={travelers}
        isBusy={isBusy}
        translate={translate}
        onJoinGroup={onJoinGroup}
        onAddMembershipTraveler={onAddMembershipTraveler}
      />
      <TourGroupPlanSection
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isOrganizer={false}
        planItems={details.planItems}
        planOptions={details.planOptions}
        translate={translate}
        onOpenChoose={onOpenChoose}
      />
      <TourGroupSelectionList
        currentLanguage={currentLanguage}
        title={translate('tourGroups.myChoices')}
        eyebrow={translate('tourGroups.myChoicesEyebrow')}
        isBusy={isBusy}
        selections={mySelections}
        planItems={details.planItems}
        planOptions={details.planOptions}
        activeMembership={activeMembership}
        linkedSelectionIds={linkedSelectionIds}
        selectionOrderProjections={selectionOrderProjections}
        translate={translate}
        onSubmitSelection={onSubmitSelection}
        onBatchPaySelections={onBatchPaySelections}
        onOpenBookings={onOpenBookings}
      />
      <TourGroupBookingSummary
        currentLanguage={currentLanguage}
        bookings={details.bookings}
        translate={translate}
      />
    </div>
  )
}

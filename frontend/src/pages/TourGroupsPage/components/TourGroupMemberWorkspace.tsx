import type { AppLanguage, GroupPlanItemResponse, TourGroupDetailsResponse, TourGroupMembershipResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { TourGroupMemberSection } from '@/pages/TourGroupsPage/components/TourGroupMemberSection'
import { TourGroupPlanSection } from '@/pages/TourGroupsPage/components/TourGroupPlanSection'

type TourGroupMemberWorkspaceProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  activeMembership: TourGroupMembershipResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onJoinGroup: () => Promise<void>
  onLeaveGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onRemoveMembershipTraveler: (travelerId: string) => Promise<void>
  onOpenChoose: (planItem: GroupPlanItemResponse) => void
}

export function TourGroupMemberWorkspace({
  currentLanguage,
  details,
  signedInUser,
  travelers,
  activeMembership,
  isBusy,
  translate,
  onJoinGroup,
  onLeaveGroup,
  onAddMembershipTraveler,
  onRemoveMembershipTraveler,
  onOpenChoose,
}: TourGroupMemberWorkspaceProps) {
  return (
    <div className="grid gap-4">
      <TourGroupMemberSection
        currentLanguage={currentLanguage}
        details={details}
        signedInUser={signedInUser}
        activeMembership={activeMembership}
        travelers={travelers}
        isBusy={isBusy}
        translate={translate}
        onJoinGroup={onJoinGroup}
        onLeaveGroup={onLeaveGroup}
        onAddMembershipTraveler={onAddMembershipTraveler}
        onRemoveMembershipTraveler={onRemoveMembershipTraveler}
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
    </div>
  )
}

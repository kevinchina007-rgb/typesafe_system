import type { GroupPlanItemResponse, TourGroupDetailsResponse, TourGroupSummaryResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'

export function syncGroupSummary(groups: TourGroupSummaryResponse[], details: TourGroupDetailsResponse): TourGroupSummaryResponse[] {
  const nextGroups = groups.filter(group => group.groupId !== details.group.groupId)
  return [details.group, ...nextGroups].sort((left, right) => right.createdAt.localeCompare(left.createdAt))
}

export function getActiveMembership(selectedGroupDetails: TourGroupDetailsResponse | null, signedInUser: UserResponse | null) {
  if (!selectedGroupDetails || !signedInUser) {
    return null
  }
  return (
    selectedGroupDetails.memberships.find(
      membership => membership.userId === signedInUser.userId && membership.status === 'Active',
    ) ?? null
  )
}

export function getMembershipTravelerIds(selectedGroupDetails: TourGroupDetailsResponse | null, activeMembershipId: string | null | undefined) {
  if (!selectedGroupDetails || !activeMembershipId) {
    return new Set<string>()
  }
  return new Set(
    selectedGroupDetails.membershipTravelers
      .filter(row => row.membershipId === activeMembershipId && row.status === 'Active')
      .map(row => row.travelerId),
  )
}

export function getSelectionDialogOptions(
  selectedGroupDetails: TourGroupDetailsResponse | null,
  selectionPlanItem: GroupPlanItemResponse | null,
) {
  if (!selectionPlanItem || !selectedGroupDetails) {
    return []
  }
  return selectedGroupDetails.planOptions.filter(option => option.planItemId === selectionPlanItem.planItemId)
}

export function getSelectionDialogTravelers(travelers: TravelerResponse[], travelerIds: Set<string>) {
  return travelers.filter(traveler => travelerIds.has(traveler.travelerId))
}

export function pickInitialActivePlanItem(details: TourGroupDetailsResponse, currentPlanItem: GroupPlanItemResponse | null) {
  if (currentPlanItem) {
    return details.planItems.find(planItem => planItem.planItemId === currentPlanItem.planItemId) ?? currentPlanItem
  }
  return [...details.planItems].sort((left, right) => left.sequenceNo - right.sequenceNo)[0] ?? null
}

export function pickCreatedPlanItem(
  details: TourGroupDetailsResponse,
  previousPlanItemIds: Set<string>,
) {
  return (
    details.planItems
      .filter(planItem => !previousPlanItemIds.has(planItem.planItemId))
      .sort((left, right) => left.sequenceNo - right.sequenceNo)
      .at(-1) ?? null
  )
}

export function findNewestSelection(
  createdDetails: TourGroupDetailsResponse,
  planItemId: string,
  optionId: string,
  membershipId: string | null | undefined,
) {
  return [...createdDetails.selections]
    .filter(selection => selection.planItemId === planItemId && selection.optionId === optionId && selection.membershipId === membershipId)
    .sort((left, right) => right.createdAt.localeCompare(left.createdAt))[0]
    ?? null
}

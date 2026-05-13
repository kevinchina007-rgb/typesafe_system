export type GroupPlanSelectionResponse = {
  selectionId: string
  groupId: string
  planItemId: string
  optionId: string
  membershipId: string
  quantity: number
  travelerIds: string[]
  status: string
  createdAt: string
  confirmedAt: string | null
  reviewedByOrganizerUserId: string | null
  reviewNote: string | null
}

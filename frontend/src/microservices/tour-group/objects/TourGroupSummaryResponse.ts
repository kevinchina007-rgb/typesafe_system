export type TourGroupSummaryResponse = {
  groupId: string
  organizerUserId: string
  title: string
  description: string
  destination: string
  startDate: string
  endDate: string
  capacity: number
  usedCapacity: number
  memberCount: number
  pendingSelectionCount: number
  confirmedSelectionCount: number
  convertedOrderCount: number
  status: string
  createdAt: string
}

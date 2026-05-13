export type GroupPlanItemResponse = {
  planItemId: string
  itemType: string
  title: string
  description: string
  scheduledAt: string
  endsAt: string | null
  sequenceNo: number
  status: string
}

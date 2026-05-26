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
export const groupPlanItemResponseFromJson = (json: string): GroupPlanItemResponse =>
  JSON.parse(json) as GroupPlanItemResponse

export const groupPlanItemResponseToJson = (value: GroupPlanItemResponse): string =>
  JSON.stringify(value)

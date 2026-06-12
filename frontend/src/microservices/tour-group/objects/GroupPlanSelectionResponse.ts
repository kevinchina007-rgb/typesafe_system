// 本文件定义 tour-group 模块的 `GroupPlanSelectionResponse`，作为响应数据并提供 JSON 编解码。

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
export const groupPlanSelectionResponseFromJson = (json: string): GroupPlanSelectionResponse =>
  JSON.parse(json) as GroupPlanSelectionResponse

export const groupPlanSelectionResponseToJson = (value: GroupPlanSelectionResponse): string =>
  JSON.stringify(value)

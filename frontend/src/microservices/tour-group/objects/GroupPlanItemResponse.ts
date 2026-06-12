// 本文件定义 tour-group 模块的 `GroupPlanItemResponse`，作为响应数据并提供 JSON 编解码。

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

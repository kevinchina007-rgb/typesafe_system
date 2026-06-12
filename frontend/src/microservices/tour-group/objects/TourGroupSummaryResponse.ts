// 本文件定义 tour-group 模块的 `TourGroupSummaryResponse`，作为摘要响应数据并提供 JSON 编解码。

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
  coverImageUrl: string | null
  tags: string[]
  memberCount: number
  pendingSelectionCount: number
  confirmedSelectionCount: number
  convertedOrderCount: number
  status: string
  createdAt: string
}
export const tourGroupSummaryResponseFromJson = (json: string): TourGroupSummaryResponse =>
  JSON.parse(json) as TourGroupSummaryResponse

export const tourGroupSummaryResponseToJson = (value: TourGroupSummaryResponse): string =>
  JSON.stringify(value)

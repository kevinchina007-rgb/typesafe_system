// 本文件定义 tour-group 模块的 `GroupSelectionOrderLinkResponse`，作为响应数据并提供 JSON 编解码。

export type GroupSelectionOrderLinkResponse = {
  selectionId: string
  orderId: string
  createdAt: string
}
export const groupSelectionOrderLinkResponseFromJson = (json: string): GroupSelectionOrderLinkResponse =>
  JSON.parse(json) as GroupSelectionOrderLinkResponse

export const groupSelectionOrderLinkResponseToJson = (value: GroupSelectionOrderLinkResponse): string =>
  JSON.stringify(value)

export type GroupSelectionOrderLinkResponse = {
  selectionId: string
  orderId: string
  createdAt: string
}
export const groupSelectionOrderLinkResponseFromJson = (json: string): GroupSelectionOrderLinkResponse =>
  JSON.parse(json) as GroupSelectionOrderLinkResponse

export const groupSelectionOrderLinkResponseToJson = (value: GroupSelectionOrderLinkResponse): string =>
  JSON.stringify(value)

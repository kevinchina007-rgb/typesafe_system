export type GroupSelectionOrderProjectionResponse = {
  selectionId: string
  orderId: string
  orderStatus: string
  paymentStatus: string
  supplierReviewStatus: string
  refundStatus: string | null
  bookingSummaryLabel: string
}
export const groupSelectionOrderProjectionResponseFromJson = (json: string): GroupSelectionOrderProjectionResponse =>
  JSON.parse(json) as GroupSelectionOrderProjectionResponse

export const groupSelectionOrderProjectionResponseToJson = (value: GroupSelectionOrderProjectionResponse): string =>
  JSON.stringify(value)

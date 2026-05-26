export type CreateOrderCancellationMessageRequest = {
  threadId: string
  orderId: string
  reason: string
}

export const createOrderCancellationMessageRequestFromJson = (json: string): CreateOrderCancellationMessageRequest =>
  JSON.parse(json) as CreateOrderCancellationMessageRequest

export const createOrderCancellationMessageRequestToJson = (value: CreateOrderCancellationMessageRequest): string =>
  JSON.stringify(value)

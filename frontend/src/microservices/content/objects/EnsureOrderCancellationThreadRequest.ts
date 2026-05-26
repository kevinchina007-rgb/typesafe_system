export type EnsureOrderCancellationThreadRequest = {
  userId: string
  orderId: string
}

export const ensureOrderCancellationThreadRequestFromJson = (json: string): EnsureOrderCancellationThreadRequest =>
  JSON.parse(json) as EnsureOrderCancellationThreadRequest

export const ensureOrderCancellationThreadRequestToJson = (value: EnsureOrderCancellationThreadRequest): string =>
  JSON.stringify(value)

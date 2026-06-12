// 本文件定义 content 模块的 `CreateOrderCancellationMessageRequest`，作为请求参数并提供 JSON 编解码。

export type CreateOrderCancellationMessageRequest = {
  threadId: string
  orderId: string
  reason: string
}

export const createOrderCancellationMessageRequestFromJson = (json: string): CreateOrderCancellationMessageRequest =>
  JSON.parse(json) as CreateOrderCancellationMessageRequest

export const createOrderCancellationMessageRequestToJson = (value: CreateOrderCancellationMessageRequest): string =>
  JSON.stringify(value)

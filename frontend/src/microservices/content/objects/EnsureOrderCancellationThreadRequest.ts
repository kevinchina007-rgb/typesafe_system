// 本文件定义 content 模块的 `EnsureOrderCancellationThreadRequest`，作为请求参数并提供 JSON 编解码。

export type EnsureOrderCancellationThreadRequest = {
  userId: string
  orderId: string
}

export const ensureOrderCancellationThreadRequestFromJson = (json: string): EnsureOrderCancellationThreadRequest =>
  JSON.parse(json) as EnsureOrderCancellationThreadRequest

export const ensureOrderCancellationThreadRequestToJson = (value: EnsureOrderCancellationThreadRequest): string =>
  JSON.stringify(value)

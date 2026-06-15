// 本文件定义 feedback 模块的 `EnsureOrderCancellationThreadPlannerRequest`，用于在订单取消场景下确保存在对应的反馈线程。

export type EnsureOrderCancellationThreadPlannerRequest = {
  userId: string
  orderId: string
}

export const ensureOrderCancellationThreadRequestFromJson = (json: string): EnsureOrderCancellationThreadPlannerRequest =>
  JSON.parse(json) as EnsureOrderCancellationThreadPlannerRequest

export const ensureOrderCancellationThreadRequestToJson = (value: EnsureOrderCancellationThreadPlannerRequest): string =>
  JSON.stringify(value)
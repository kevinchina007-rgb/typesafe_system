// 本文件定义支付链接创建请求，和后端 `CreatePaymentLinkPlannerRequest` 保持同名镜像。

export type CreatePaymentLinkPlannerRequest = {
  orderId: string
  userId: string
  paymentMethod: string
  language: string | null
  publicBackendOrigin: string | null
}

export const createPaymentLinkPlannerRequestFromJson = (json: string): CreatePaymentLinkPlannerRequest =>
  JSON.parse(json) as CreatePaymentLinkPlannerRequest

export const createPaymentLinkPlannerRequestToJson = (value: CreatePaymentLinkPlannerRequest): string =>
  JSON.stringify(value)

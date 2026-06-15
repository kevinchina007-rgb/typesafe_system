// 本文件定义订单创建请求，和后端 `CreateOrderPlannerRequest` 保持同名镜像。

export type CreateOrderPlannerRequest = {
  ownerUserId: string
  orderCurrency: string
}

export const createOrderPlannerRequestFromJson = (json: string): CreateOrderPlannerRequest =>
  JSON.parse(json) as CreateOrderPlannerRequest

export const createOrderPlannerRequestToJson = (value: CreateOrderPlannerRequest): string =>
  JSON.stringify(value)

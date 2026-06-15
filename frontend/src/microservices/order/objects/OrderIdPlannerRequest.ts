// 本文件定义订单标识请求，和后端 `OrderIdPlannerRequest` 保持同名镜像。

export type OrderIdPlannerRequest = {
  orderId: string
}

export const orderIdPlannerRequestFromJson = (json: string): OrderIdPlannerRequest =>
  JSON.parse(json) as OrderIdPlannerRequest

export const orderIdPlannerRequestToJson = (value: OrderIdPlannerRequest): string =>
  JSON.stringify(value)

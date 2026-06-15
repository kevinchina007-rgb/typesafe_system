// 本文件定义订单行项目响应，和后端 `OrderLineItemPlannerResponse` 保持同名镜像。

export type OrderLineItemPlannerResponse = {
  orderItemId: string
  orderItemKind: string
  orderItemStatus: string
  supplierReviewStatus: string
  bookedAmount: string
  bookedCurrency: string
  summaryLabel: string
}

export const orderLineItemPlannerResponseFromJson = (json: string): OrderLineItemPlannerResponse =>
  JSON.parse(json) as OrderLineItemPlannerResponse

export const orderLineItemPlannerResponseToJson = (value: OrderLineItemPlannerResponse): string =>
  JSON.stringify(value)

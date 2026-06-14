// 本文件定义 train 模块的 `BookTrainItemPlannerResponse`，用于表示订票结果并提供 JSON 编解码。

export type BookTrainItemPlannerResponse = {
  orderId: string
  orderItemId: string
}
export const bookTrainItemPlannerResponseFromJson = (json: string): BookTrainItemPlannerResponse =>
  JSON.parse(json) as BookTrainItemPlannerResponse

export const bookTrainItemPlannerResponseToJson = (value: BookTrainItemPlannerResponse): string =>
  JSON.stringify(value)

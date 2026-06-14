// 本文件定义 train 模块的 `BookTrainItemPlannerRequest`，作为与 planner 同名的请求参数并提供 JSON 编解码。

export type BookTrainItemPlannerRequest = {
  userId: string
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
}

export const bookTrainItemPlannerRequestFromJson = (json: string): BookTrainItemPlannerRequest =>
  JSON.parse(json) as BookTrainItemPlannerRequest

export const bookTrainItemPlannerRequestToJson = (value: BookTrainItemPlannerRequest): string =>
  JSON.stringify(value)

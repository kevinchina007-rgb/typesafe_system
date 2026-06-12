// 本文件定义 train 模块的 `BookTrainItemRequest`，作为请求参数并提供 JSON 编解码。

export type BookTrainItemRequest = {
  userId: string
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
}

export const bookTrainItemRequestFromJson = (json: string): BookTrainItemRequest =>
  JSON.parse(json) as BookTrainItemRequest

export const bookTrainItemRequestToJson = (value: BookTrainItemRequest): string =>
  JSON.stringify(value)

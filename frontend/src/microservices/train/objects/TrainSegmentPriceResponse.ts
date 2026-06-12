// 本文件定义 train 模块的 `TrainSegmentPriceResponse`，作为响应数据并提供 JSON 编解码。

export type TrainSegmentPriceResponse = {
  fromStationCode: string
  toStationCode: string
  seatClass: string
  amount: string
  currency: string
}
export const trainSegmentPriceResponseFromJson = (json: string): TrainSegmentPriceResponse =>
  JSON.parse(json) as TrainSegmentPriceResponse

export const trainSegmentPriceResponseToJson = (value: TrainSegmentPriceResponse): string =>
  JSON.stringify(value)

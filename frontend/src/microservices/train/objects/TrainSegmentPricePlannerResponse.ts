// 本文件定义 train 模块的 `TrainSegmentPricePlannerResponse`，用于表示区间票价并提供 JSON 编解码。

export type TrainSegmentPricePlannerResponse = {
  fromStationCode: string
  toStationCode: string
  seatClass: string
  amount: string
  currency: string
}
export const trainSegmentPricePlannerResponseFromJson = (json: string): TrainSegmentPricePlannerResponse =>
  JSON.parse(json) as TrainSegmentPricePlannerResponse

export const trainSegmentPricePlannerResponseToJson = (value: TrainSegmentPricePlannerResponse): string =>
  JSON.stringify(value)

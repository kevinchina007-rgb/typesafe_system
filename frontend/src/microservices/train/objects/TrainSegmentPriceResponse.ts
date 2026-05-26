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

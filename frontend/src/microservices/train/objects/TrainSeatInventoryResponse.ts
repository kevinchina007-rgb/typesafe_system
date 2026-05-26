export type TrainSeatInventoryResponse = {
  inventoryId: string
  seatClass: string
  totalSeats: number
  saleableSeats: number
  status: string
}
export const trainSeatInventoryResponseFromJson = (json: string): TrainSeatInventoryResponse =>
  JSON.parse(json) as TrainSeatInventoryResponse

export const trainSeatInventoryResponseToJson = (value: TrainSeatInventoryResponse): string =>
  JSON.stringify(value)

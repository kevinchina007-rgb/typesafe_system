export type TrainSeatResponse = {
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatClass: string
  seatPositionType: string
  status: string
}
export const trainSeatResponseFromJson = (json: string): TrainSeatResponse =>
  JSON.parse(json) as TrainSeatResponse

export const trainSeatResponseToJson = (value: TrainSeatResponse): string =>
  JSON.stringify(value)

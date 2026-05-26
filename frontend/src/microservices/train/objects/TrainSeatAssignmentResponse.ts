export type TrainSeatAssignmentResponse = {
  travelerId: string
  seatId: string
  carriageNo: number
  seatNo: string
  seatLabel: string
  seatPositionType: string
}
export const trainSeatAssignmentResponseFromJson = (json: string): TrainSeatAssignmentResponse =>
  JSON.parse(json) as TrainSeatAssignmentResponse

export const trainSeatAssignmentResponseToJson = (value: TrainSeatAssignmentResponse): string =>
  JSON.stringify(value)

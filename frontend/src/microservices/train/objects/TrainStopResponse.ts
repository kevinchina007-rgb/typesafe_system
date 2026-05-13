export type TrainStopResponse = {
  stopId: string
  stationCode: string
  stationName: string
  arrivalTime: string | null
  departureTime: string | null
  sequenceNo: number
}

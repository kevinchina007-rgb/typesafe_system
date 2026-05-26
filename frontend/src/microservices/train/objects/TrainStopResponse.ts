export type TrainStopResponse = {
  stopId: string
  stationCode: string
  stationName: string
  arrivalTime: string | null
  departureTime: string | null
  sequenceNo: number
}
export const trainStopResponseFromJson = (json: string): TrainStopResponse =>
  JSON.parse(json) as TrainStopResponse

export const trainStopResponseToJson = (value: TrainStopResponse): string =>
  JSON.stringify(value)

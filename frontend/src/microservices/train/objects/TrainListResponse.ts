import type { TrainResponse } from './TrainResponse'

export type TrainListResponse = {
  trains: TrainResponse[]
}
export const trainListResponseFromJson = (json: string): TrainListResponse =>
  JSON.parse(json) as TrainListResponse

export const trainListResponseToJson = (value: TrainListResponse): string =>
  JSON.stringify(value)

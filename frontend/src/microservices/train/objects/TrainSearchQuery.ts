export type TrainSearchQuery = {
  fromStation?: string
  toStation?: string
  date?: string
}
export const trainSearchQueryFromJson = (json: string): TrainSearchQuery =>
  JSON.parse(json) as TrainSearchQuery

export const trainSearchQueryToJson = (value: TrainSearchQuery): string =>
  JSON.stringify(value)

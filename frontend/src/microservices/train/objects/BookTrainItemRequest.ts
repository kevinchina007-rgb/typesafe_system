export type BookTrainItemRequest = {
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
}
export const bookTrainItemRequestFromJson = (json: string): BookTrainItemRequest =>
  JSON.parse(json) as BookTrainItemRequest

export const bookTrainItemRequestToJson = (value: BookTrainItemRequest): string =>
  JSON.stringify(value)

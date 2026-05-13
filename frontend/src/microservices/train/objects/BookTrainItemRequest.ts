export type BookTrainItemRequest = {
  trainId: string
  travelerIds: string[]
  fromStationCode: string
  toStationCode: string
  seatClass: string
  seatPreference?: string | null
}

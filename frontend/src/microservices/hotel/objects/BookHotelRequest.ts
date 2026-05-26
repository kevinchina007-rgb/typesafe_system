export type BookHotelRequest = {
  userId: string
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}

export const bookHotelRequestFromJson = (json: string): BookHotelRequest =>
  JSON.parse(json) as BookHotelRequest

export const bookHotelRequestToJson = (value: BookHotelRequest): string =>
  JSON.stringify(value)

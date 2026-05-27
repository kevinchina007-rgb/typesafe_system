export type BookHotelPlannerRequest = {
  userId: string
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}


export const bookHotelRequestFromJson = (json: string): BookHotelPlannerRequest =>
  JSON.parse(json) as BookHotelPlannerRequest

export const bookHotelRequestToJson = (value: BookHotelPlannerRequest): string =>
  JSON.stringify(value)

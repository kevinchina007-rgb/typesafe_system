export type BookHotelPlannerRequest = {
  userId: string
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}

export const bookHotelPlannerRequestFromJson = (json: string): BookHotelPlannerRequest =>
  JSON.parse(json) as BookHotelPlannerRequest

export const bookHotelPlannerRequestToJson = (value: BookHotelPlannerRequest): string =>
  JSON.stringify(value)

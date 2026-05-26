export type HotelItemDetailsResponse = {
  hotelId: string
  hotelName: string
  location: string
  roomTypeId: string
  roomTypeName: string
  checkInDate: string
  checkOutDate: string
  guestTravelerIds: string[]
  roomCount: number
  reservationStatus: string | null
  reservationExpiresAt: string | null
  unitPrice: string
  totalPrice: string
  currency: string
}
export const hotelItemDetailsResponseFromJson = (json: string): HotelItemDetailsResponse =>
  JSON.parse(json) as HotelItemDetailsResponse

export const hotelItemDetailsResponseToJson = (value: HotelItemDetailsResponse): string =>
  JSON.stringify(value)

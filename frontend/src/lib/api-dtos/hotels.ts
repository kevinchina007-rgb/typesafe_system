export type RoomTypeSummaryResponse = {
  roomTypeId: string
  roomTypeName: string
  capacity: number
  bedType: string
  basePrice: string
  currency: string
  status: string
  isBookableForRequestedStay: boolean
  availableRoomsForRequestedStay: number | null
}

export type HotelResponse = {
  hotelId: string
  hotelName: string
  location: string
  status: string
  createdAt: string
  roomTypes: RoomTypeSummaryResponse[]
}

export type HotelListResponse = {
  hotels: HotelResponse[]
}

export type HotelSearchQueryDto = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
}

export type BookHotelRequestDto = {
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}

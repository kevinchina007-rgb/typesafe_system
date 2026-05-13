export type RoomTypeSummaryResponse = {
  roomTypeId: string
  roomTypeName: string
  capacity: number
  bedType: string
  nightlyPrice: string
  basePrice: string
  currency: string
  availableRooms: number
  availableRoomsForRequestedStay: number | null
  isBookableForRequestedStay: boolean
}

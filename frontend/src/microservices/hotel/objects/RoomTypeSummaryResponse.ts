export type RoomTypeSummaryResponse = {
  roomTypeId: string
  roomTypeName: string
  capacity: number
  bedType: string
  nightlyPrice: string
  basePrice: string
  currency: string
  imageUrl: string | null
  availableRooms: number
  availableRoomsForRequestedStay: number | null
  isBookableForRequestedStay: boolean
}
export const roomTypeSummaryResponseFromJson = (json: string): RoomTypeSummaryResponse =>
  JSON.parse(json) as RoomTypeSummaryResponse

export const roomTypeSummaryResponseToJson = (value: RoomTypeSummaryResponse): string =>
  JSON.stringify(value)

export type GetHotelDetailsPlannerRequest = {
  hotelId: string
  checkInDate?: string
  checkOutDate?: string
}

export const getHotelDetailsPlannerRequestFromJson = (json: string): GetHotelDetailsPlannerRequest =>
  JSON.parse(json) as GetHotelDetailsPlannerRequest

export const getHotelDetailsPlannerRequestToJson = (value: GetHotelDetailsPlannerRequest): string =>
  JSON.stringify(value)

export type UpdateHotelManagerProfilePlannerRequest = {
  managerId: string
  displayName: string
  email: string
  hotelName: string
  hotelLocation: string
}

export const updateHotelManagerProfilePlannerRequestFromJson = (json: string): UpdateHotelManagerProfilePlannerRequest =>
  JSON.parse(json) as UpdateHotelManagerProfilePlannerRequest

export const updateHotelManagerProfilePlannerRequestToJson = (value: UpdateHotelManagerProfilePlannerRequest): string =>
  JSON.stringify(value)

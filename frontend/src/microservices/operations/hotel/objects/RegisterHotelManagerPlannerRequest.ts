export type RegisterHotelManagerPlannerRequest = {
  email: string
  displayName: string
  hotelName: string
  location: string
  password: string
}

export const registerHotelManagerPlannerRequestFromJson = (json: string): RegisterHotelManagerPlannerRequest =>
  JSON.parse(json) as RegisterHotelManagerPlannerRequest

export const registerHotelManagerPlannerRequestToJson = (value: RegisterHotelManagerPlannerRequest): string =>
  JSON.stringify(value)

export type HotelSuggestionPlannerRequest = {
  q: string
}

export const hotelSuggestionPlannerRequestFromJson = (json: string): HotelSuggestionPlannerRequest =>
  JSON.parse(json) as HotelSuggestionPlannerRequest

export const hotelSuggestionPlannerRequestToJson = (value: HotelSuggestionPlannerRequest): string =>
  JSON.stringify(value)

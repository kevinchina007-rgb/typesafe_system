export type BookAttractionItemRequest = {
  attractionId: string
  ticketTypeId: string
  sessionId?: string | null
  travelerIds: string[]
  useDate: string
}
export const bookAttractionItemRequestFromJson = (json: string): BookAttractionItemRequest =>
  JSON.parse(json) as BookAttractionItemRequest

export const bookAttractionItemRequestToJson = (value: BookAttractionItemRequest): string =>
  JSON.stringify(value)

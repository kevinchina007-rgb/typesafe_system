export type BookAttractionItemRequest = {
  attractionId: string
  ticketTypeId: string
  sessionId?: string | null
  travelerIds: string[]
  useDate: string
}

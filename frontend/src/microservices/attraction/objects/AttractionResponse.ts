import type { AttractionTicketTypeResponse } from './AttractionTicketTypeResponse'

export type AttractionResponse = {
  attractionId: string
  attractionName: string
  city: string
  location: string
  description: string
  status: string
  ticketTypes: AttractionTicketTypeResponse[]
}

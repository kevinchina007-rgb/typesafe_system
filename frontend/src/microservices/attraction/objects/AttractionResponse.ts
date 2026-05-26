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
export const attractionResponseFromJson = (json: string): AttractionResponse =>
  JSON.parse(json) as AttractionResponse

export const attractionResponseToJson = (value: AttractionResponse): string =>
  JSON.stringify(value)

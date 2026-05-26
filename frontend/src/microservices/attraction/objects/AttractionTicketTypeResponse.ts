import type { AttractionTicketTypeRuleResponse } from './AttractionTicketTypeRuleResponse'
import type { AttractionTicketSessionResponse } from './AttractionTicketSessionResponse'

export type AttractionTicketTypeResponse = {
  ticketTypeId: string
  ticketTypeName: string
  description: string
  priceAmount: string
  priceCurrency: string
  status: string
  availableFromDate: string
  availableToDate: string
  totalQuantity: number
  validWeekdays: string[]
  availableQuantityForRequestedDate: number | null
  isAvailableForRequestedDate: boolean
  rules: AttractionTicketTypeRuleResponse[]
  sessions: AttractionTicketSessionResponse[]
}
export const attractionTicketTypeResponseFromJson = (json: string): AttractionTicketTypeResponse =>
  JSON.parse(json) as AttractionTicketTypeResponse

export const attractionTicketTypeResponseToJson = (value: AttractionTicketTypeResponse): string =>
  JSON.stringify(value)

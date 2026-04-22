export type AttractionTicketTypeRuleResponse = {
  ruleId: string
  ruleType: string
  summary: string
}

export type AttractionTicketSessionResponse = {
  sessionId: string
  sessionName: string
  useDate: string
  startsAt: string
  endsAt: string
  capacity: number
  availableQuantity: number | null
  status: string
}

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

export type AttractionResponse = {
  attractionId: string
  attractionName: string
  city: string
  location: string
  description: string
  status: string
  ticketTypes: AttractionTicketTypeResponse[]
}

export type AttractionListResponse = {
  attractions: AttractionResponse[]
}

export type AttractionSearchQueryDto = {
  city?: string
  useDate?: string
}

export type BookAttractionItemRequestDto = {
  attractionId: string
  ticketTypeId: string
  sessionId?: string | null
  travelerIds: string[]
  useDate: string
}

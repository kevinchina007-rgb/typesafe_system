export type CreateAttractionTicketTypePlannerRequest = {
  managerId: string
  attractionId: string
  ticketTypeName: string
  description: string
  unitPrice: string
  currency: string
  availableFromDate: string
  availableToDate: string
  totalQuantity: number
  validWeekdays: string[]
}

export const createAttractionTicketTypePlannerRequestFromJson = (json: string): CreateAttractionTicketTypePlannerRequest =>
  JSON.parse(json) as CreateAttractionTicketTypePlannerRequest

export const createAttractionTicketTypePlannerRequestToJson = (value: CreateAttractionTicketTypePlannerRequest): string =>
  JSON.stringify(value)

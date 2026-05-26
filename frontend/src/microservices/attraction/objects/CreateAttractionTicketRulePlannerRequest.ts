export type CreateAttractionTicketRulePlannerRequest = {
  managerId: string
  attractionId: string
  ticketTypeId: string
  ruleType: string
  ageValue?: number | null
  minAge?: number | null
  maxAge?: number | null
  documentType?: string | null
  documentNumberPrefix?: string | null
}

export const createAttractionTicketRulePlannerRequestFromJson = (json: string): CreateAttractionTicketRulePlannerRequest =>
  JSON.parse(json) as CreateAttractionTicketRulePlannerRequest

export const createAttractionTicketRulePlannerRequestToJson = (value: CreateAttractionTicketRulePlannerRequest): string =>
  JSON.stringify(value)

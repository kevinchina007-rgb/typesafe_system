export type CreateAttractionTicketSessionPlannerRequest = {
  managerId: string
  attractionId: string
  ticketTypeId: string
  sessionName: string
  useDate: string
  startsAt: string
  endsAt: string
  capacity: number
}

export const createAttractionTicketSessionPlannerRequestFromJson = (json: string): CreateAttractionTicketSessionPlannerRequest =>
  JSON.parse(json) as CreateAttractionTicketSessionPlannerRequest

export const createAttractionTicketSessionPlannerRequestToJson = (value: CreateAttractionTicketSessionPlannerRequest): string =>
  JSON.stringify(value)

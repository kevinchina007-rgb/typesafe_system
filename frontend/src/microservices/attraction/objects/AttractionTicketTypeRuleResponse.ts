export type AttractionTicketTypeRuleResponse = {
  ruleId: string
  ruleType: string
  ageValue: number | null
  minAge: number | null
  maxAge: number | null
  documentType: string | null
  documentNumberPrefix: string | null
  summary: string
}
export const attractionTicketTypeRuleResponseFromJson = (json: string): AttractionTicketTypeRuleResponse =>
  JSON.parse(json) as AttractionTicketTypeRuleResponse

export const attractionTicketTypeRuleResponseToJson = (value: AttractionTicketTypeRuleResponse): string =>
  JSON.stringify(value)

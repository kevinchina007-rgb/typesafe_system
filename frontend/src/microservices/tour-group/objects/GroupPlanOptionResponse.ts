export type GroupPlanOptionResponse = {
  optionId: string
  planItemId: string
  resourceType: string
  resourceId: string
  resourceVariantCode: string | null
  resourceContext: string | null
  label: string
  description: string
  defaultQuantity: number
  status: string
}
export const groupPlanOptionResponseFromJson = (json: string): GroupPlanOptionResponse =>
  JSON.parse(json) as GroupPlanOptionResponse

export const groupPlanOptionResponseToJson = (value: GroupPlanOptionResponse): string =>
  JSON.stringify(value)

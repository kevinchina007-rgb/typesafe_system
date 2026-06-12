// 本文件定义 attraction 模块的 `CreateAttractionTicketTypePlannerRequest`，作为planner 请求参数并提供 JSON 编解码。

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

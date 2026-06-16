// 本文件定义 attraction 模块的 `CreateAttractionTicketSessionPlannerRequest`，作为planner 请求参数并提供 JSON 编解码。

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

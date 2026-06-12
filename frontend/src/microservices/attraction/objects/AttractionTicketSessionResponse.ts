// 本文件定义 attraction 模块的 `AttractionTicketSessionResponse`，作为响应数据并提供 JSON 编解码。

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
export const attractionTicketSessionResponseFromJson = (json: string): AttractionTicketSessionResponse =>
  JSON.parse(json) as AttractionTicketSessionResponse

export const attractionTicketSessionResponseToJson = (value: AttractionTicketSessionResponse): string =>
  JSON.stringify(value)

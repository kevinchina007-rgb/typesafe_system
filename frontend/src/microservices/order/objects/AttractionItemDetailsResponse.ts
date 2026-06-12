// 本文件定义 order 模块的 `AttractionItemDetailsResponse`，作为详情响应数据并提供 JSON 编解码。

export type AttractionItemDetailsResponse = {
  attractionId: string
  attractionName: string
  ticketTypeId: string
  ticketTypeName: string
  sessionId: string | null
  sessionName: string | null
  sessionStartsAt: string | null
  sessionEndsAt: string | null
  useDate: string
  travelerIds: string[]
  unitPrice: string
  totalPrice: string
  currency: string
  eligibilityRuleSummaries: string[]
  eligibilityValidatedAt: string
}
export const attractionItemDetailsResponseFromJson = (json: string): AttractionItemDetailsResponse =>
  JSON.parse(json) as AttractionItemDetailsResponse

export const attractionItemDetailsResponseToJson = (value: AttractionItemDetailsResponse): string =>
  JSON.stringify(value)

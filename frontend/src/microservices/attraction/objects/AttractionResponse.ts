// 本文件定义 attraction 模块的 `AttractionResponse`，作为响应数据并提供 JSON 编解码。

import type { AttractionTicketTypeResponse } from './AttractionTicketTypeResponse'

export type AttractionResponse = {
  attractionId: string
  attractionName: string
  city: string
  location: string
  description: string
  imageUrl: string | null
  status: string
  ticketTypes: AttractionTicketTypeResponse[]
}
export const attractionResponseFromJson = (json: string): AttractionResponse =>
  JSON.parse(json) as AttractionResponse

export const attractionResponseToJson = (value: AttractionResponse): string =>
  JSON.stringify(value)

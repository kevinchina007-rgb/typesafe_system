// 本文件定义 auth 模块的 `AttractionAdminSessionResponse`，作为响应数据并提供 JSON 编解码。

import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'

export type AttractionAdminSessionResponse = {
  managerId: string
  email: string
  displayName: string
  status: string
  managedAttractions: AttractionResponse[]
}
export const attractionAdminSessionResponseFromJson = (json: string): AttractionAdminSessionResponse =>
  JSON.parse(json) as AttractionAdminSessionResponse

export const attractionAdminSessionResponseToJson = (value: AttractionAdminSessionResponse): string =>
  JSON.stringify(value)

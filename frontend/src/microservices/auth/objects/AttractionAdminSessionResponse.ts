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

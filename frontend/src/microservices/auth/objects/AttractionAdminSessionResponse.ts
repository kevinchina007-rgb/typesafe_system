import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'

export type AttractionAdminSessionResponse = {
  managerId: string
  email: string
  displayName: string
  status: string
  managedAttractions: AttractionResponse[]
}

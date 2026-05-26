import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { AttractionSearchQuery } from '@/microservices/attraction/objects/AttractionSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listAttractions = (query?: AttractionSearchQuery): Promise<AttractionListResponse> =>
  executeJsonApiRequest('/ListAttractionsPlanner', 'POST', {
    city: query?.city,
    useDate: query?.useDate,
  })

export const getAttraction = (attractionId: string, query?: Pick<AttractionSearchQuery, 'useDate'>): Promise<AttractionResponse> =>
  executeJsonApiRequest('/GetAttractionDetailsPlanner', 'POST', {
    attractionId,
    useDate: query?.useDate,
  })

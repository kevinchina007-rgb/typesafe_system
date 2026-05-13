import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { AttractionSearchQuery } from '@/microservices/attraction/objects/AttractionSearchQuery'
import { createQueryString, executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const listAttractions = (query?: AttractionSearchQuery): Promise<AttractionListResponse> =>
    executeApiRequest(`/attractions${createQueryString(query ?? {})}`)

export const getAttraction = (attractionId: string, query?: Pick<AttractionSearchQuery, 'useDate'>): Promise<AttractionResponse> =>
    executeApiRequest(`/attractions/${attractionId}${createQueryString(query ?? {})}`)

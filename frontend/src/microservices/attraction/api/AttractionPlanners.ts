import type { AttractionListResponse } from '@/microservices/attraction/objects/AttractionListResponse'
import type { AttractionResponse } from '@/microservices/attraction/objects/AttractionResponse'
import type { AttractionSearchQuery } from '@/microservices/attraction/objects/AttractionSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'
import { mapAttractionListResponseFromBackend, mapAttractionResponseFromBackend, type BackendAttractionListResponse, type BackendAttractionResponse } from './AttractionResponseMappers'

export const listAttractions = (query?: AttractionSearchQuery): Promise<AttractionListResponse> =>
  executeJsonApiRequest<BackendAttractionListResponse>('/ListAttractionsPlanner', 'POST', {
    city: query?.city,
    keyword: query?.keyword,
    useDate: query?.useDate,
  }).then(response => mapAttractionListResponseFromBackend(response, query?.useDate))

export const getAttraction = (attractionId: string, query?: Pick<AttractionSearchQuery, 'useDate'>): Promise<AttractionResponse> =>
  executeJsonApiRequest<BackendAttractionResponse>('/GetAttractionDetailsPlanner', 'POST', {
    attractionId,
    useDate: query?.useDate,
  }).then(response => mapAttractionResponseFromBackend(response, query?.useDate))

export { uploadAttractionImage } from './UploadAttractionImagePlanner'

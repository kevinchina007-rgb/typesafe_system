import type { AttractionListResponse, AttractionResponse, AttractionSearchQueryDto, BookAttractionItemRequestDto } from '../api-dtos/attractions'
import type { OrderResponse } from '../api-dtos/orders'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const attractionApiClient = {
  listAttractions: (query?: AttractionSearchQueryDto): Promise<AttractionListResponse> =>
    executeApiRequest(`/attractions${createQueryString(query ?? {})}`),

  getAttraction: (attractionId: string, query?: Pick<AttractionSearchQueryDto, 'useDate'>): Promise<AttractionResponse> =>
    executeApiRequest(`/attractions/${attractionId}${createQueryString(query ?? {})}`),

  addAttractionItemToOrder: (orderId: string, payload: BookAttractionItemRequestDto): Promise<OrderResponse> =>
    executeJsonApiRequest(`/orders/${orderId}/attraction-items`, 'POST', payload),
}

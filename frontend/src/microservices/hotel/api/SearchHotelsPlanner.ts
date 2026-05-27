import type { HotelListPlannerResponse } from '@/microservices/hotel/objects/HotelListPlannerResponse'
import type { HotelSearchPlannerRequest } from '@/microservices/hotel/objects/HotelSearchPlannerRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const searchHotelsPlanner = (query: HotelSearchPlannerRequest): Promise<HotelListPlannerResponse> =>
  executeJsonApiRequest('/SearchHotelsPlanner', 'POST', query)

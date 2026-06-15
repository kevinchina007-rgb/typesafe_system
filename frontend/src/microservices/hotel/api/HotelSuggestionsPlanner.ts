import type { HotelSuggestionPlannerRequest } from '@/microservices/hotel/objects/HotelSuggestionPlannerRequest'
import type { SearchSuggestionListPlannerResponse } from '@/microservices/hotel/objects/SearchSuggestionListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const hotelSuggestionsPlanner = (query: HotelSuggestionPlannerRequest): Promise<SearchSuggestionListPlannerResponse> =>
  executeJsonApiRequest('/HotelSuggestionsPlanner', 'POST', query)

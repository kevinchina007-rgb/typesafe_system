import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { AttractionSuggestionListPlannerResponse } from '@/microservices/attraction/objects/AttractionSuggestionListPlannerResponse'
import type { AttractionSuggestionRequest } from '@/microservices/attraction/objects/AttractionSuggestionRequest'

export const suggestAttractions = (payload: AttractionSuggestionRequest): Promise<AttractionSuggestionListPlannerResponse> =>
  executeJsonApiRequest<AttractionSuggestionListPlannerResponse>('/AttractionSuggestionsPlanner', 'POST', payload)

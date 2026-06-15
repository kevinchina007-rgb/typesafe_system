import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'
import type { TrainSuggestionListPlannerResponse } from '@/microservices/train/objects/TrainSuggestionListPlannerResponse'
import type { TrainSuggestionsPlannerRequest } from '@/microservices/train/objects/TrainSuggestionsPlannerRequest'

export const trainSuggestionsPlanner = (payload: TrainSuggestionsPlannerRequest): Promise<TrainSuggestionListPlannerResponse> =>
  executeJsonApiRequest('/TrainSuggestionsPlanner', 'POST', payload)

export const suggestionsPlanner = trainSuggestionsPlanner

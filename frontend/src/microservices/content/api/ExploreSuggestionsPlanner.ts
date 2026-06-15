// ExploreSuggestionsPlanner：content 域探索推荐入口。

import type { ExploreSuggestionsPlannerRequest } from '@/microservices/content/objects/ExploreSuggestionsPlannerRequest'
import type { ExploreSuggestionListPlannerResponse } from '@/microservices/content/objects/ExploreSuggestionListPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listExploreSuggestions = (payload: ExploreSuggestionsPlannerRequest): Promise<ExploreSuggestionListPlannerResponse> =>
  executeJsonApiRequest('/ExploreSuggestionsPlanner', 'POST', payload)

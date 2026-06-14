import type { ExploreSearchResponse } from '@/microservices/common/objects/ExploreSearchResponse'
import type { SearchSuggestionListResponse } from '@/microservices/common/objects/SearchSuggestionListResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listExploreSuggestions = (q: string): Promise<SearchSuggestionListResponse> =>
  executeJsonApiRequest('/ExploreSuggestionsPlanner', 'POST', { q })

export const searchExplore = (payload: { q: string; type?: string }): Promise<ExploreSearchResponse> =>
  executeJsonApiRequest('/ExploreSearchPlanner', 'POST', {
    q: payload.q,
    resourceType: payload.type && payload.type !== 'all' ? payload.type : undefined,
  })


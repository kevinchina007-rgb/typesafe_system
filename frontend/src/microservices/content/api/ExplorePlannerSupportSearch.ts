import type { ExploreSearchResponse } from '@/shared-kernel/objects/ExploreSearchResponse'
import type { SearchSuggestionListResponse } from '@/shared-kernel/objects/SearchSuggestionListResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const listExploreSuggestions = (q: string): Promise<SearchSuggestionListResponse> =>
  executeJsonApiRequest('/ExploreSuggestionsPlanner', 'POST', { q })

export const searchExplore = (payload: { q: string; type?: string }): Promise<ExploreSearchResponse> =>
  executeJsonApiRequest('/ExploreSearchPlanner', 'POST', {
    q: payload.q,
    resourceType: payload.type && payload.type !== 'all' ? payload.type : undefined,
  })


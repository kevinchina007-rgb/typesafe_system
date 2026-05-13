import type { ExploreSearchResponse } from '@/microservices/common/objects/ExploreSearchResponse'
import type { SearchSuggestionListResponse } from '@/microservices/common/objects/SearchSuggestionListResponse'
import { createQueryString, executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const listExploreSuggestions = (q: string): Promise<SearchSuggestionListResponse> =>
    executeApiRequest(`/explore/suggestions${createQueryString({ q })}`)

export const searchExplore = (payload: { q: string; type?: string }): Promise<ExploreSearchResponse> =>
    executeApiRequest(`/explore/search${createQueryString({ q: payload.q, type: payload.type && payload.type !== 'all' ? payload.type : undefined })}`)

export type HealthResponse = {
  status: string
  service: string
  backendPort: number
}

export type ApiErrorResponse = {
  code: string
  message: string
}

export type SearchSuggestionResponse = {
  resourceType: string
  value: string
  title: string
  subtitle: string
}

export type SearchSuggestionListResponse = {
  suggestions: SearchSuggestionResponse[]
}

export type ExploreSearchResultResponse = {
  resourceType: string
  resourceId: string
  title: string
  summary: string
  metaLabel: string
  navigationHint: string
  imageUrl: string | null
}

export type ExploreSearchResponse = {
  results: ExploreSearchResultResponse[]
}

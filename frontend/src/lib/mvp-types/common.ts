export type {
  ApiErrorResponse,
  ExploreSearchResponse,
  ExploreSearchResultResponse,
  HealthResponse,
  SearchSuggestionListResponse,
  SearchSuggestionResponse,
} from '../api-dtos/common'

export type AppLanguage = 'en' | 'zh'

export type AppViewKey =
  | 'overview'
  | 'smartPlanner'
  | 'orders'
  | 'blog'
  | 'reviews'
  | 'explore'
  | 'account'
  | 'travelers'
  | 'flights'
  | 'hotels'
  | 'trains'
  | 'attractions'
  | 'tourGroups'
  | 'bookings'
  | 'manager'
  | 'trainAdmin'
  | 'attractionAdmin'

export type AppNotice = {
  id: number
  kind: 'success' | 'error' | 'info'
  title: string
  description: string
  technicalMessage?: string
}

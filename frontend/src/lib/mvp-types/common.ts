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
  | 'customerFeedback'
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
  | 'managerWorkspace'
  | 'managerFeedback'
  | 'managerAdvertising'
  | 'siteAdminBlogAudit'
  | 'siteAdminAdvertisingReview'
  | 'trainAdmin'
  | 'attractionAdmin'

export type AppNotice = {
  id: number
  kind: 'success' | 'error' | 'info'
  title: string
  description: string
  technicalMessage?: string
}

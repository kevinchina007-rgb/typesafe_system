export type { HealthResponse } from '@/microservices/common/objects/HealthResponse'
export type { ApiErrorResponse } from '@/microservices/common/objects/ApiErrorResponse'
export type { ExploreSearchResponse } from '@/microservices/common/objects/ExploreSearchResponse'
export type { ExploreSearchResultResponse } from '@/microservices/common/objects/ExploreSearchResultResponse'
export type { SearchSuggestionListResponse } from '@/microservices/common/objects/SearchSuggestionListResponse'
export type { SearchSuggestionResponse } from '@/microservices/common/objects/SearchSuggestionResponse'

export type AppLanguage = 'zh'

export type AppViewKey =
  | 'overview'
  | 'smartPlanner'
  | 'orders'
  | 'flightOrders'
  | 'hotelOrders'
  | 'trainOrders'
  | 'attractionOrders'
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
  | 'managerCreateFlight'
  | 'managerFlightManagement'
  | 'managerFeedback'
  | 'managerProfile'
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

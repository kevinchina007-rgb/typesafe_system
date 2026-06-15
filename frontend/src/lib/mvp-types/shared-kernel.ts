// shared-kernel 模块只保留跨业务通用的类型，不再放 content 域的搜索对象。

export type { HealthResponse } from '@/shared-kernel/objects/HealthResponse'
export type { ApiErrorResponse } from '@/shared-kernel/objects/ApiErrorResponse'
export type { SearchSuggestionResponse } from '@/shared-kernel/objects/SearchSuggestionResponse'
export type { ExploreSearchResultResponse } from '@/shared-kernel/objects/ExploreSearchResultResponse'

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
  | 'tourGroupPlanBuilder'
  | 'bookings'
  | 'manager'
  | 'siteAdminLogin'
  | 'managerWorkspace'
  | 'managerCreateFlight'
  | 'managerFlightManagement'
  | 'managerFeedback'
  | 'managerProfile'
  | 'managerAdvertising'
  | 'siteAdminBlogAudit'
  | 'siteAdminAdvertisingReview'
  | 'siteAdminHotelAdvertisingReview'
  | 'siteAdminTrainAdvertisingReview'
  | 'siteAdminAttractionAdvertisingReview'
  | 'siteAdminFeedback'
  | 'trainAdmin'
  | 'attractionAdmin'

export type AppNotice = {
  id: number
  kind: 'success' | 'error' | 'info'
  title: string
  description: string
  technicalMessage?: string
}

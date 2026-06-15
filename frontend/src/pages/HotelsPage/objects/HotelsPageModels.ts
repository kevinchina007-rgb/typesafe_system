import type { RefObject } from 'react'

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AppLanguage, AppViewKey, ResourceReviewSummaryPlannerResponse, ReviewPlannerResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { HotelPlannerResponse } from '@/lib/mvp-types/index'
import type { HotelPreference } from '@/app/stores/models/hotel-booking-model'

// 酒店页面顶层参数，负责把全局语言、登录态和导航能力传给页面。
export type HotelsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

// 酒店搜索通知的统一结构。
export type HotelSearchNotice = {
  kind: 'error' | 'warning'
  message: string
}

// 酒店页面头部标题区参数。
export type HotelPageHeroProps = {
  title: string
  description: string
}

// 酒店搜索提示区参数。
export type HotelSearchNoticeProps = {
  notice: HotelSearchNotice | null
}

// 广告展示区参数。
export type HotelAdvertisingSectionProps = {
  featuredAdvertisement: AdvertisementResponse | null
  selectedAdvertisement: AdvertisementResponse | null
  translate: (translationKey: string) => string
  onOpenAdvertisement: (advertisementId: string) => Promise<void>
}

// 酒店搜索卡片参数。
export type HotelSearchCardProps = {
  isBusy: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  hotDestinations: string[]
  translate: (translationKey: string) => string
  onSearchLocationChange: (value: string) => void
  onSearchCheckInDateChange: (value: string) => void
  onSearchCheckOutDateChange: (value: string) => void
  onSelectDestination: (value: string) => void
  onSearch: () => void
}

// 酒店日期价格条参数。
export type HotelDatePriceStripProps = {
  dateWindowStart: string
  selectedDate: string
  lowestPrice: number | null
  isBusy: boolean
  onPrevious: () => void
  onNext: () => void
  onDateSelect: (date: string) => void
}

// 酒店筛选条参数。
export type HotelFilterBarProps = {
  hotelPreference: HotelPreference
  nearbyPreference: string
  isBusy: boolean
  translate: (translationKey: string) => string
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
}

// 酒店结果区参数。
export type HotelResultsSectionProps = {
  currentLanguage: AppLanguage
  hotelResponses: HotelPlannerResponse[]
  isBusy: boolean
  isGuestMode: boolean
  defaultRoomCount: number
  searchCheckInDate: string
  searchCheckOutDate: string
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  translate: (translationKey: string) => string
  onToggleTravelerSelection: (travelerId: string) => void
  onRequireLogin: () => void
  onBookHotel: (payload: {
    roomTypeId: string
    guestTravelerIds: string[]
    checkInDate: string
    checkOutDate: string
    roomCount: number
  }) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryPlannerResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewPlannerResponse[]>
}

// 酒店页面 controller 暴露给视图层的全部状态和动作。
export type HotelsPageController = {
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  isBusy: boolean
  isGuestMode: boolean
  isTourGroupTargetMode: boolean
  targetHotelResponses: HotelPlannerResponse[]
  hotelResponses: HotelPlannerResponse[]
  hasSearchedHotels: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  roomCount: number
  hotelPreference: 'Economy' | 'Luxury' | 'Homestay' | 'Family' | 'Business'
  nearbyPreference: string
  searchNotice: HotelSearchNotice | null
  featuredAdvertisement: AdvertisementResponse | null
  selectedAdvertisement: AdvertisementResponse | null
  hotelLowestNightlyPrice: number | null
  dateWindowStart: string
  isAuthDialogOpen: boolean
  onPreviousDateWindow: () => void
  onNextDateWindow: () => void
  executeHotelSearch: (nextLocation: string, nextCheckInDate: string, nextCheckOutDate: string) => Promise<void>
  openAdvertisement: (advertisementId: string) => Promise<void>
  handleDateSelect: (nextCheckInDate: string) => Promise<void>
  setSearchLocation: (value: string) => void
  setSearchCheckInDate: (value: string) => void
  setSearchCheckOutDate: (value: string) => void
  setHotelPreference: (value: 'Economy' | 'Luxury' | 'Homestay' | 'Family' | 'Business') => void
  setNearbyPreference: (value: string) => void
  toggleTravelerSelection: (travelerId: string) => void
  onRequireLogin: () => void
  onAuthDialogClose: () => void
  onAuthDialogConfirm: () => void
  loadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryPlannerResponse>
  loadReviewsByResource: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewPlannerResponse[]>
  bookHotel: (payload: {
    roomTypeId: string
    guestTravelerIds: string[]
    checkInDate: string
    checkOutDate: string
    roomCount: number
  }) => Promise<void>
  resultsSectionRef: RefObject<HTMLDivElement | null>
  noticeSectionRef: RefObject<HTMLDivElement | null>
}

// 酒店搜索请求结构。
export type HotelSearchRequest = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
  roomCount?: number
  guestCount?: number
  hotelPreference?: 'Economy' | 'Luxury' | 'Homestay' | 'Family' | 'Business'
  nearbyPreference?: string
}

// 酒店预订请求结构。
export type HotelBookRequest = {
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}

// 复用的评论资源加载参数。
export type ReviewLoaderRequest = { resourceType: string; resourceId: string }

// 评论资源加载器集合。
export type ReviewLoaders = {
  onLoadReviewSummary: (payload: ReviewLoaderRequest) => Promise<ResourceReviewSummaryPlannerResponse>
  onLoadReviews: (payload: ReviewLoaderRequest) => Promise<ReviewPlannerResponse[]>
}

import type { RefObject } from 'react'

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { AppLanguage, AppViewKey, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { HotelPlannerResponse } from '@/lib/mvp-types/index'
import type { HotelPreference } from '@/app/stores/models/hotel-booking-model'

export type HotelsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export type HotelSearchNotice = {
  kind: 'error' | 'warning'
  message: string
}

export type HotelPageHeroProps = {
  title: string
  description: string
}

export type HotelSearchNoticeProps = {
  notice: HotelSearchNotice | null
}

export type HotelAdvertisingSectionProps = {
  featuredAdvertisement: AdvertisementResponse | null
  selectedAdvertisement: AdvertisementResponse | null
  translate: (translationKey: string) => string
  onOpenAdvertisement: (advertisementId: string) => Promise<void>
}

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

export type HotelDatePriceStripProps = {
  dateWindowStart: string
  selectedDate: string
  lowestPrice: number | null
  isBusy: boolean
  onPrevious: () => void
  onNext: () => void
  onDateSelect: (date: string) => void
}

export type HotelFilterBarProps = {
  hotelPreference: HotelPreference
  nearbyPreference: string
  isBusy: boolean
  translate: (translationKey: string) => string
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
}

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
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export type HotelsPageController = {
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  isBusy: boolean
  isGuestMode: boolean
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
  loadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  loadReviewsByResource: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
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

export type HotelSearchRequest = {
  location?: string
  checkInDate?: string
  checkOutDate?: string
  roomCount?: number
  guestCount?: number
  hotelPreference?: 'Economy' | 'Luxury' | 'Homestay' | 'Family' | 'Business'
  nearbyPreference?: string
}

export type HotelBookRequest = {
  roomTypeId: string
  guestTravelerIds: string[]
  checkInDate: string
  checkOutDate: string
  roomCount: number
}

export type ReviewLoaderRequest = { resourceType: string; resourceId: string }

export type ReviewLoaders = {
  onLoadReviewSummary: (payload: ReviewLoaderRequest) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: ReviewLoaderRequest) => Promise<ReviewResponse[]>
}

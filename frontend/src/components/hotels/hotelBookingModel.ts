import type {
  AppLanguage,
  HotelResponse,
  ResourceReviewSummaryResponse,
  ReviewResponse,
  TravelerResponse,
} from '../../lib/mvp-types'

export type HotelQuickDatePreset = 'tonight' | 'weekend' | 'nextWeek' | 'holiday'
export type HotelPreference = 'Economy' | 'Luxury' | 'Homestay' | 'Family' | 'Business'

export type HotelsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onSearchHotels: (payload: {
    location?: string
    checkInDate?: string
    checkOutDate?: string
    roomCount?: number
    guestCount?: number
    hotelPreference?: HotelPreference
    nearbyPreference?: string
  }) => Promise<HotelResponse[]>
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

export const defaultHotelSearchState = {
  location: '杭州西湖',
  checkInDate: '2026-04-05',
  checkOutDate: '2026-04-07',
  roomCount: 1,
  guestCount: 2,
  hotelPreference: 'Business' as HotelPreference,
  nearbyPreference: 'ScenicSpot',
  selectedQuickDatePreset: null as HotelQuickDatePreset | null,
}

export const hotelLocationPlaceholder = '杭州 / 西湖 / 上海 / 外滩'
export const hotelHotDestinations = ['杭州 西湖', '上海 外滩', '北京 三里屯', '东京 银座']
export const hotelRecentSearches = ['杭州 西湖', '上海 陆家嘴', '北京 国贸', '东京 银座']
export const hotelNearbyOptions = ['ScenicSpot', 'Metro', 'Station', 'BusinessDistrict'] as const
export const hotelPreferenceOptions: HotelPreference[] = ['Economy', 'Luxury', 'Homestay', 'Family', 'Business']
export const hotelFilterOptions = ['priceRange', 'starLevel', 'guestRating', 'distance', 'breakfast', 'freeCancellation', 'stayDeal', 'brand'] as const

export function renderHotelTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function applyHotelQuickDatePreset(preset: HotelQuickDatePreset, today = new Date()) {
  const checkInDate = new Date(today)
  const checkOutDate = new Date(today)

  if (preset === 'weekend') {
    const day = checkInDate.getDay()
    const daysUntilFriday = (5 - day + 7) % 7
    checkInDate.setDate(checkInDate.getDate() + daysUntilFriday)
    checkOutDate.setDate(checkInDate.getDate() + 2)
  } else if (preset === 'nextWeek') {
    checkInDate.setDate(checkInDate.getDate() + 7)
    checkOutDate.setDate(checkInDate.getDate() + 9)
  } else if (preset === 'holiday') {
    checkInDate.setDate(checkInDate.getDate() + 14)
    checkOutDate.setDate(checkInDate.getDate() + 17)
  } else {
    checkOutDate.setDate(checkOutDate.getDate() + 1)
  }

  return {
    checkInDate: checkInDate.toISOString().slice(0, 10),
    checkOutDate: checkOutDate.toISOString().slice(0, 10),
  }
}

export function countHotelStayNights(checkInDate: string, checkOutDate: string) {
  if (!checkInDate || !checkOutDate) {
    return 0
  }
  const start = Date.parse(checkInDate)
  const end = Date.parse(checkOutDate)
  if (Number.isNaN(start) || Number.isNaN(end) || end <= start) {
    return 0
  }
  return Math.round((end - start) / (1000 * 60 * 60 * 24))
}

export function formatHotelPriceInsight(hotels: HotelResponse[], translate: (translationKey: string) => string) {
  const nightlyRates = hotels.flatMap(hotel =>
    hotel.roomTypes.map(roomType => Number(roomType.basePrice)).filter(amount => Number.isFinite(amount)),
  )
  if (nightlyRates.length === 0) {
    return translate('hotels.priceInsightFallback')
  }
  const averageRate = Math.round(nightlyRates.reduce((sum, amount) => sum + amount, 0) / nightlyRates.length)
  return translate('hotels.priceInsightValue').replace('{amount}', String(averageRate))
}


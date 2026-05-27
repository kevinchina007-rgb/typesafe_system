import type { AppLanguage, HotelPlannerResponse, ResourceReviewSummaryResponse, ReviewResponse, TravelerResponse } from '@/lib/mvp-types/index'

export type HotelQuickDatePreset = 'tonight' | 'weekend' | 'nextWeek' | 'holiday'
export type HotelPreference = 'Economy' | 'Luxury' | 'Homestay' | 'Family' | 'Business'

export type HotelsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onValidationError: (message: string) => void
  onSearchHotels: (payload: {
    location?: string
    checkInDate?: string
    checkOutDate?: string
    roomCount?: number
    guestCount?: number
    hotelPreference?: HotelPreference
    nearbyPreference?: string
  }) => Promise<HotelPlannerResponse[]>
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
  location: '杭州',
  checkInDate: '2026-06-01',
  checkOutDate: '2026-06-03',
  roomCount: 1,
  guestCount: 2,
  hotelPreference: 'Business' as HotelPreference,
  nearbyPreference: 'ScenicSpot',
  selectedQuickDatePreset: null as HotelQuickDatePreset | null,
}

export const hotelHotDestinations = ['北京', '上海', '广州', '深圳', '成都', '重庆', '杭州', '南京', '武汉', '西安', '天津', '郑州', '长沙', '青岛', '厦门']
export const hotelRecentSearches = ['杭州', '上海', '北京', '深圳']
export const hotelNearbyOptions = ['ScenicSpot', 'Metro', 'Station', 'BusinessDistrict'] as const
export const hotelPreferenceOptions: HotelPreference[] = ['Economy', 'Luxury', 'Homestay', 'Family', 'Business']
export const hotelFilterOptions = ['priceRange', 'starLevel', 'guestRating', 'distance', 'breakfast', 'freeCancellation', 'stayDeal', 'brand'] as const

export function renderHotelTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function applyHotelQuickDatePreset(preset: HotelQuickDatePreset, today = new Date()) {
  const checkInDate = createHotelDateFromParts(today.getFullYear(), today.getMonth(), today.getDate())
  const checkOutDate = createHotelDateFromParts(today.getFullYear(), today.getMonth(), today.getDate())

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
    checkInDate: formatHotelDateText(checkInDate),
    checkOutDate: formatHotelDateText(checkOutDate),
  }
}

export function countHotelStayNights(checkInDate: string, checkOutDate: string) {
  if (!checkInDate || !checkOutDate) {
    return 0
  }
  const start = parseHotelDateText(checkInDate)
  const end = parseHotelDateText(checkOutDate)
  if (Number.isNaN(start.getTime()) || Number.isNaN(end.getTime()) || end <= start) {
    return 0
  }
  return Math.round((end.getTime() - start.getTime()) / (1000 * 60 * 60 * 24))
}

export function formatHotelPriceInsight(hotels: HotelPlannerResponse[], translate: (translationKey: string) => string) {
  const nightlyRates = hotels.flatMap(hotel =>
    hotel.roomTypes.map(roomType => Number(roomType.basePrice)).filter(amount => Number.isFinite(amount)),
  )
  if (nightlyRates.length === 0) {
    return translate('hotels.priceInsightFallback')
  }
  const averageRate = Math.round(nightlyRates.reduce((sum, amount) => sum + amount, 0) / nightlyRates.length)
  return translate('hotels.priceInsightValue').replace('{amount}', String(averageRate))
}

export function addHotelDays(dateText: string, deltaDays: number): string {
  const date = parseHotelDateText(dateText)
  date.setDate(date.getDate() + deltaDays)
  return formatHotelDateText(date)
}

export function formatHotelDateLabel(dateText: string): string {
  const date = parseHotelDateText(dateText)
  return date.toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
  })
}

export function formatHotelWeekdayLabel(dateText: string): string {
  const date = parseHotelDateText(dateText)
  return date.toLocaleDateString('zh-CN', {
    weekday: 'short',
  })
}

function parseHotelDateText(dateText: string): Date {
  const [yearText, monthText, dayText] = dateText.split('-')
  const year = Number(yearText)
  const month = Number(monthText)
  const day = Number(dayText)
  if (!Number.isInteger(year) || !Number.isInteger(month) || !Number.isInteger(day)) {
    return new Date('Invalid Date')
  }
  return createHotelDateFromParts(year, month - 1, day)
}

function createHotelDateFromParts(year: number, zeroBasedMonth: number, day: number): Date {
  return new Date(year, zeroBasedMonth, day, 12, 0, 0, 0)
}

function formatHotelDateText(date: Date): string {
  const year = date.getFullYear()
  const month = `${date.getMonth() + 1}`.padStart(2, '0')
  const day = `${date.getDate()}`.padStart(2, '0')
  return `${year}-${month}-${day}`
}

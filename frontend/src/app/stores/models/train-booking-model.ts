import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse, TrainResponse, TravelerResponse } from '@/lib/mvp-types/index'

export type TrainTripType = 'oneWay' | 'roundTrip'
export type TrainQuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'nextWeek'
export type TrainSeatPreference = 'Business' | 'FirstClass' | 'SecondClass' | 'SoftSleeper' | 'HardSleeper' | 'NoSeat'
export type TrainTypePreference = 'HighSpeed' | 'Bullet' | 'Regular'

export type TrainsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onRequireLogin: () => void
  onSearchTrains: (payload: {
    fromStation?: string
    toStation?: string
    date?: string
    returnDate?: string
    tripType?: TrainTripType
    passengerCount?: number
    seatPreference?: TrainSeatPreference
    trainTypePreference?: TrainTypePreference
  }) => Promise<TrainResponse[]>
  onBookTrain: (payload: {
    trainId: string
    travelerIds: string[]
    fromStationCode: string
    toStationCode: string
    seatClass: string
    seatPreference?: string | null
    orderCurrency: string
  }) => Promise<void>
  onLoadReviewSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export const defaultTrainSearchState = {
  tripType: 'oneWay' as TrainTripType,
  date: '2026-06-01',
  returnDate: '2026-06-03',
  fromStation: '北京南',
  toStation: '上海虹桥',
  passengerCount: 1,
  seatPreference: 'SecondClass' as TrainSeatPreference,
  trainTypePreference: 'HighSpeed' as TrainTypePreference,
  selectedQuickDatePreset: null as TrainQuickDatePreset | null,
}

export const trainHotRoutes = [
  { id: 'beijing-shanghai', departureLabel: '北京南', arrivalLabel: '上海虹桥' },
  { id: 'shanghai-nanjing', departureLabel: '上海虹桥', arrivalLabel: '南京南' },
  { id: 'guangzhou-shenzhen', departureLabel: '广州南', arrivalLabel: '深圳北' },
  { id: 'chengdu-changsha', departureLabel: '成都东', arrivalLabel: '长沙南' },
]

export const trainRecentSearches = ['北京南', '上海虹桥', '南京南', '广州南']
export const trainPopularStations = ['北京南', '上海虹桥', '广州南', '成都东', '长沙南']
export const trainSeatPreferences: TrainSeatPreference[] = [
  'Business',
  'FirstClass',
  'SecondClass',
  'SoftSleeper',
  'HardSleeper',
  'NoSeat',
]
export const trainTypePreferences: TrainTypePreference[] = ['HighSpeed', 'Bullet', 'Regular']
export const trainFilterOptions = ['departureTime', 'arrivalTime', 'duration', 'seatClass', 'availability', 'directOnly'] as const

export function renderTrainTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

function normalizeTrainStationInput(value: string): string {
  return value.trim()
}

function normalizeTrainStationCode(value: string): string {
  return value.trim().toUpperCase()
}

export function findTrainStopByQuery(train: TrainResponse, stationQuery: string) {
  const trimmedQuery = normalizeTrainStationInput(stationQuery)
  if (!trimmedQuery) {
    return null
  }

  const normalizedQuery = trimmedQuery.toUpperCase()
  return (
    train.stops.find(stop => {
      const stopCode = stop.stationCode.trim().toUpperCase()
      const stopName = stop.stationName.trim()
      return stopCode === normalizedQuery || stopName === trimmedQuery || stopName.toUpperCase() === normalizedQuery
    }) ?? null
  )
}

export function resolveTrainStationCodes(
  train: TrainResponse,
  fromStationQuery: string,
  toStationQuery: string,
): { fromStationCode: string; toStationCode: string } | null {
  const fromStop = findTrainStopByQuery(train, fromStationQuery)
  const toStop = findTrainStopByQuery(train, toStationQuery)

  if (!fromStop || !toStop) {
    return null
  }

  return {
    fromStationCode: fromStop.stationCode,
    toStationCode: toStop.stationCode,
  }
}

export function quoteTrainSegmentAmount(
  train: TrainResponse,
  fromStationCode: string,
  toStationCode: string,
  seatClass: string,
): { amount: string; currency: string } | null {
  const fromStop = findTrainStopByQuery(train, fromStationCode)
  const toStop = findTrainStopByQuery(train, toStationCode)
  const normalizedSeatClass = seatClass.trim().toLowerCase()
  const fromIndex = fromStop ? train.stops.findIndex(stop => stop.stationCode.trim().toUpperCase() === normalizeTrainStationCode(fromStop.stationCode)) : -1
  const toIndex = toStop ? train.stops.findIndex(stop => stop.stationCode.trim().toUpperCase() === normalizeTrainStationCode(toStop.stationCode)) : -1

  if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
    return null
  }

  const pathStops = train.stops.slice(fromIndex, toIndex + 1)
  const matchingSegmentPrices = pathStops.slice(0, -1).map((currentStop, index) =>
    train.segmentPrices.find(
      segmentPrice =>
        segmentPrice.fromStationCode.toUpperCase() === currentStop.stationCode.toUpperCase() &&
        segmentPrice.toStationCode.toUpperCase() === pathStops[index + 1].stationCode.toUpperCase() &&
        segmentPrice.seatClass.trim().toLowerCase() === normalizedSeatClass,
    ),
  )

  if (matchingSegmentPrices.some(segmentPrice => !segmentPrice)) {
    return null
  }

  const segmentPrices = matchingSegmentPrices.flatMap(segmentPrice => (segmentPrice ? [segmentPrice] : []))
  const currency = segmentPrices[0]?.currency

  if (!currency || segmentPrices.some(segmentPrice => segmentPrice.currency !== currency)) {
    return null
  }

  const amount = segmentPrices.reduce((currentAmount, segmentPrice) => currentAmount + Number(segmentPrice.amount), 0)
  return { amount: amount.toString(), currency }
}

export function renderTrainStopSummary(train: TrainResponse): string {
  return train.stops.map(stop => stop.stationName).join(' → ')
}

export function applyTrainQuickDatePreset(preset: TrainQuickDatePreset, today = new Date()) {
  const baseDate = new Date(today)
  if (preset === 'tomorrow') {
    baseDate.setDate(baseDate.getDate() + 1)
  }
  if (preset === 'weekend') {
    const day = baseDate.getDay()
    const daysUntilSaturday = (6 - day + 7) % 7
    baseDate.setDate(baseDate.getDate() + daysUntilSaturday)
  }
  if (preset === 'nextWeek') {
    baseDate.setDate(baseDate.getDate() + 7)
  }

  return baseDate.toISOString().slice(0, 10)
}

export function formatTrainPriceInsight(trains: TrainResponse[], translate: (translationKey: string) => string) {
  const allSegmentPrices = trains.flatMap(train => train.segmentPrices.map(segmentPrice => Number(segmentPrice.amount)))
  if (allSegmentPrices.length === 0) {
    return translate('trains.priceInsightFallback')
  }

  const lowestPrice = Math.min(...allSegmentPrices)
  return translate('trains.priceInsightValue').replace('{amount}', String(lowestPrice))
}

export function formatTrainRecommendation(searchFromStation: string, searchToStation: string, translate: (translationKey: string) => string) {
  if (!searchFromStation || !searchToStation) {
    return translate('trains.recommendationFallback')
  }
  return translate('trains.recommendationValue')
    .replace('{from}', searchFromStation)
    .replace('{to}', searchToStation)
}

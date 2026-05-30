import type { TrainResponse, TravelerResponse } from '@/lib/mvp-types/index'

export type TrainTripType = 'oneWay' | 'roundTrip'
export type TrainQuickDatePreset = 'today' | 'tomorrow' | 'weekend' | 'nextWeek'
export type TrainSeatPreference = 'Business' | 'FirstClass' | 'SecondClass' | 'SoftSleeper' | 'HardSleeper' | 'NoSeat'
export type TrainTypePreference = 'HighSpeed' | 'Bullet' | 'Regular'
export type TrainSortMode = 'highSpeedPriority' | 'lowPricePriority' | 'departureTimeEarly'

export type TrainSearchSegment = {
  fromStop: TrainResponse['stops'][number]
  toStop: TrainResponse['stops'][number]
  segmentStops: TrainResponse['stops']
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

export function renderTrainTravelerOptionLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

function normalizeTrainStationInput(value: string): string {
  return value.trim()
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

function findTrainStopIndexByQuery(train: TrainResponse, stationQuery: string): number {
  const stop = findTrainStopByQuery(train, stationQuery)
  if (!stop) {
    return -1
  }
  return train.stops.findIndex(item => item.stopId === stop.stopId)
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

export function resolveTrainSearchSegment(
  train: TrainResponse,
  fromStationQuery: string,
  toStationQuery: string,
): TrainSearchSegment | null {
  const fromIndex = findTrainStopIndexByQuery(train, fromStationQuery)
  const toIndex = findTrainStopIndexByQuery(train, toStationQuery)

  if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
    return null
  }

  const fromStop = train.stops[fromIndex]
  const toStop = train.stops[toIndex]
  return {
    fromStop,
    toStop,
    segmentStops: train.stops.slice(fromIndex, toIndex + 1),
  }
}

export function quoteTrainSegmentAmount(
  train: TrainResponse,
  fromStationCode: string,
  toStationCode: string,
  seatClass: string,
): { amount: string; currency: string } | null {
  const routeSegment = resolveTrainSearchSegment(train, fromStationCode, toStationCode)
  const normalizedSeatClass = seatClass.trim().toLowerCase()
  if (!routeSegment) {
    return null
  }

  const pathStops = routeSegment.segmentStops
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

export function renderTrainSearchSegmentSummary(train: TrainResponse, fromStationQuery: string, toStationQuery: string): string {
  const routeSegment = resolveTrainSearchSegment(train, fromStationQuery, toStationQuery)
  if (!routeSegment) {
    return renderTrainStopSummary(train)
  }
  return routeSegment.segmentStops.map(stop => stop.stationName).join(' → ')
}

function getTrainPriorityScore(trainNumber: string): number {
  const firstChar = trainNumber.trim().toUpperCase().charAt(0)
  if (firstChar === 'G') {
    return 0
  }
  if (firstChar === 'D' || firstChar === 'C') {
    return 1
  }
  return 2
}

function getTrainRouteLowestPrice(train: TrainResponse, fromStationQuery: string, toStationQuery: string): number {
  const routeSegment = resolveTrainSearchSegment(train, fromStationQuery, toStationQuery)
  if (!routeSegment) {
    return Number.POSITIVE_INFINITY
  }

  const routeQuotes = train.seatInventories
    .map(inventory => quoteTrainSegmentAmount(train, fromStationQuery, toStationQuery, inventory.seatClass))
    .filter((quote): quote is { amount: string; currency: string } => quote !== null)
    .map(quote => Number(quote.amount))

  if (routeQuotes.length === 0) {
    return Number.POSITIVE_INFINITY
  }

  return Math.min(...routeQuotes)
}

function getTrainRouteDepartureTimestamp(train: TrainResponse, fromStationQuery: string, toStationQuery: string): number {
  const routeSegment = resolveTrainSearchSegment(train, fromStationQuery, toStationQuery)
  const departureValue = routeSegment?.fromStop.departureTime ?? routeSegment?.fromStop.arrivalTime ?? null
  if (!departureValue) {
    return Number.POSITIVE_INFINITY
  }
  const timestamp = Date.parse(departureValue)
  return Number.isNaN(timestamp) ? Number.POSITIVE_INFINITY : timestamp
}

export function sortTrainResponses(
  trainResponses: TrainResponse[],
  fromStationQuery: string,
  toStationQuery: string,
  sortMode: TrainSortMode,
): TrainResponse[] {
  return [...trainResponses].sort((left, right) => {
    switch (sortMode) {
      case 'highSpeedPriority': {
        const priorityDiff = getTrainPriorityScore(left.trainNumber) - getTrainPriorityScore(right.trainNumber)
        if (priorityDiff !== 0) {
          return priorityDiff
        }
        return left.trainNumber.localeCompare(right.trainNumber)
      }
      case 'lowPricePriority': {
        const priceDiff = getTrainRouteLowestPrice(left, fromStationQuery, toStationQuery) - getTrainRouteLowestPrice(right, fromStationQuery, toStationQuery)
        if (priceDiff !== 0) {
          return priceDiff
        }
        return left.trainNumber.localeCompare(right.trainNumber)
      }
      case 'departureTimeEarly': {
        const departureDiff = getTrainRouteDepartureTimestamp(left, fromStationQuery, toStationQuery) - getTrainRouteDepartureTimestamp(right, fromStationQuery, toStationQuery)
        if (departureDiff !== 0) {
          return departureDiff
        }
        return left.trainNumber.localeCompare(right.trainNumber)
      }
    }
  })
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

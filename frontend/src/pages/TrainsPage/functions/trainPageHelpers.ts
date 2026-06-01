import type { TrainResponse } from '@/lib/mvp-types/index'
import { quoteTrainSegmentAmount, resolveTrainSearchSegment } from '@/app/stores/models/train-booking-model'
import type { TrainSortMode } from '../objects'

const trainStationQueryAliases: Record<string, string> = {
  北京南: 'BJS',
  天津南: 'TJS',
  济南西: 'JNW',
  南京南: 'NJS',
  上海虹桥: 'SHH',
  深圳北: 'SZN',
  杭州东: 'HZD',
  宁波: 'NGB',
  温州南: 'WZS',
  福州南: 'FZN',
  厦门北: 'XMN',
  成都东: 'CDD',
  重庆北: 'CQB',
  武汉: 'WUH',
  长沙南: 'CSN',
  郑州东: 'ZZD',
  合肥南: 'HFN',
}

export function normalizeTrainSearchStationQuery(query: string): string {
  const trimmed = query.trim()
  if (!trimmed) {
    return trimmed
  }
  return trainStationQueryAliases[trimmed] ?? trimmed
}

export function normalizeTrainSearchRequestStations(fromStation: string, toStation: string): { fromStation: string; toStation: string } {
  return {
    fromStation: normalizeTrainSearchStationQuery(fromStation),
    toStation: normalizeTrainSearchStationQuery(toStation),
  }
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

export function filterTrainResponsesBySearchCriteria(
  trainResponses: TrainResponse[],
  fromStationQuery: string,
  toStationQuery: string,
  searchDate: string,
): TrainResponse[] {
  const normalizedSearchDate = searchDate.trim()
  return trainResponses.filter(trainResponse => {
    const routeSegment = resolveTrainSearchSegment(trainResponse, fromStationQuery, toStationQuery)
    if (!routeSegment) {
      return false
    }

    if (!normalizedSearchDate) {
      return true
    }

    return routeSegment.segmentStops.some(stop => {
      const departureValue = stop.departureTime ?? stop.arrivalTime ?? null
      return typeof departureValue === 'string' && departureValue.startsWith(normalizedSearchDate)
    })
  })
}

export function formatTrainRecommendation(searchFromStation: string, searchToStation: string, translate: (translationKey: string) => string) {
  if (!searchFromStation || !searchToStation) {
    return translate('trains.recommendationFallback')
  }
  return translate('trains.recommendationValue')
    .replace('{from}', searchFromStation)
    .replace('{to}', searchToStation)
}

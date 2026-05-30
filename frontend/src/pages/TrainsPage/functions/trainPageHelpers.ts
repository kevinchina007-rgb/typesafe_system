import type { TrainResponse } from '@/lib/mvp-types/index'
import { quoteTrainSegmentAmount, resolveTrainSearchSegment } from '@/app/stores/models/train-booking-model'
import type { TrainSortMode } from '../objects'

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

export function formatTrainRecommendation(searchFromStation: string, searchToStation: string, translate: (translationKey: string) => string) {
  if (!searchFromStation || !searchToStation) {
    return translate('trains.recommendationFallback')
  }
  return translate('trains.recommendationValue')
    .replace('{from}', searchFromStation)
    .replace('{to}', searchToStation)
}

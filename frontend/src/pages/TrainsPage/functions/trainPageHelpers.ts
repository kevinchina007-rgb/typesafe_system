import type { TrainPlannerResponse } from '@/lib/mvp-types/index'
import { quoteTrainSegmentAmount, resolveTrainSearchSegment } from '@/app/stores/models/train-booking-model'
import type { TrainSortMode } from '../objects'

// 火车站查询别名表，只负责把中文站名映射成页面常用简称。
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

// 规范化火车站查询词，优先替换成站点别名。
export function normalizeTrainSearchStationQuery(query: string): string {
  const trimmed = query.trim()
  if (!trimmed) {
    return trimmed
  }
  return trainStationQueryAliases[trimmed] ?? trimmed
}

// 把去程/返程站点一起规范化，供列表查询直接使用。
export function normalizeTrainSearchRequestStations(fromStation: string, toStation: string): { fromStation: string; toStation: string } {
  return {
    fromStation: normalizeTrainSearchStationQuery(fromStation),
    toStation: normalizeTrainSearchStationQuery(toStation),
  }
}

// 给列车编号打一个优先级分数，供排序逻辑使用。
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

// 读取某列车在指定路线上的最低价，只做排序辅助。
function getTrainRouteLowestPrice(train: TrainPlannerResponse, fromStationQuery: string, toStationQuery: string): number {
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

// 读取某列车在指定路线上的出发时间戳，只做排序辅助。
function getTrainRouteDepartureTimestamp(train: TrainPlannerResponse, fromStationQuery: string, toStationQuery: string): number {
  const routeSegment = resolveTrainSearchSegment(train, fromStationQuery, toStationQuery)
  const departureValue = routeSegment?.fromStop.departureTime ?? routeSegment?.fromStop.arrivalTime ?? null
  if (!departureValue) {
    return Number.POSITIVE_INFINITY
  }
  const timestamp = Date.parse(departureValue)
  return Number.isNaN(timestamp) ? Number.POSITIVE_INFINITY : timestamp
}

// 对列车响应按页面当前排序方式排序。
export function sortTrainResponses(
  trainResponses: TrainPlannerResponse[],
  fromStationQuery: string,
  toStationQuery: string,
  sortMode: TrainSortMode,
): TrainPlannerResponse[] {
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

// 按搜索条件过滤列车响应，只保留可用路线和日期匹配项。
export function filterTrainResponsesBySearchCriteria(
  trainResponses: TrainPlannerResponse[],
  fromStationQuery: string,
  toStationQuery: string,
  searchDate: string,
): TrainPlannerResponse[] {
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

// 生成列车推荐文案，作为页面空状态或提示文案。
export function formatTrainRecommendation(searchFromStation: string, searchToStation: string, translate: (translationKey: string) => string) {
  if (!searchFromStation || !searchToStation) {
    return translate('trains.recommendationFallback')
  }
  return translate('trains.recommendationValue')
    .replace('{from}', searchFromStation)
    .replace('{to}', searchToStation)
}

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightDailyLowestPricesPlannerRequest } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerRequest'
import type { FlightDailyLowestPricePlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPricePlannerResponse'
import type { FlightDailyLowestPricesPlannerResponse } from '@/microservices/flight/objects/FlightDailyLowestPricesPlannerResponse'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import {
  getFlightDetailsPlannerAirlineDisplayNameByCode,
  getFlightDetailsPlannerAirlineLogoPathByCode,
} from '@/app/stores/models/flights/flightAirlineCatalog'
import { formatFlightRouteCity } from '@/app/stores/models/flights/flightConstants'
import {
  formatFlightAirportLabel,
  getFlightDetailsPlannerCityAirportCodes,
  normalizeFlightAirportForApi,
} from '@/app/stores/models/flights/flightConstants'

// FlightsPage 使用的排序方式，只保留价格和起飞时间两种。
export type FlightSortMode = 'price' | 'departureTime'

// FlightsPage 当前激活的航线信息，用于驱动结果区展示。
export type FlightResultsRoute = {
  departureAirport: string
  arrivalAirport: string
  departureDate: string
}

// FlightsPage 将航班响应整理后的展示对象，避免在视图里重复计算。
export type DisplayFlight = {
  flight: FlightPlannerResponse
  airlineName: string
  airlineLogoPath: string | null
  departureAirportName: string
  arrivalAirportName: string
  displayCabinClass: string
  displayCabinLabel: string
  displayPrice: number
  displayCurrency: string
  isDisplayCabinBookable: boolean
  priceTone: 'lowest' | 'discount' | 'standard'
}

// 不同舱位在页面上的中文标签映射，只负责文案转换。
const cabinLabelByClass: Record<string, string> = {
  ECONOMY: '经济舱',
  PREMIUM_ECONOMY: '超级经济舱',
  BUSINESS: '商务舱',
  FIRST: '头等舱',
}

// 舱位排序顺序，结果区筛选时按这个顺序排列。
export const cabinOrder = ['ECONOMY', 'PREMIUM_ECONOMY', 'BUSINESS', 'FIRST']

// 起飞时间筛选窗口，结果区直接按这些时间段展示。
export const departureTimeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']

// 根据是否存在晚订费，生成 FlightsPage 里的提示文案。
export function buildLateBookingNotice(
  flightResponse: FlightPlannerResponse,
  translate: (translationKey: string) => string,
): string {
  if (!flightResponse.lateBookingSurchargeAmount) {
    return translate('flights.surchargeDialogDescription')
  }

  return translate('flights.surchargeNoticeWithAmount')
    .replace('{amount}', flightResponse.lateBookingSurchargeAmount)
    .replace('{currency}', flightResponse.lateBookingSurchargeCurrency || flightResponse.currency)
}

// 根据搜索态把结果区拆成多个查询任务，供结果区逐组展示。
export function loadFlightResultGroups(
  searchState: {
    tripType: 'oneWay' | 'roundTrip' | 'multiCity'
    departureAirport: string
    arrivalAirport: string
    departureDate: string
    returnDate: string
    multiCitySegments: Array<{
      id: string
      departureAirport: string
      arrivalAirport: string
      departureDate: string
      arrivalDate: string
    }>
  },
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>,
): Promise<Array<{ id: string; title: string; subtitle: string; flightResponses: FlightPlannerResponse[] }>> {
  const requests = buildFlightSearchRequests(searchState)
  if (requests.some(request => !request.query.departureAirport || !request.query.arrivalAirport || !request.query.date)) {
    return Promise.resolve([])
  }

  return Promise.all(
    requests.map(async request => ({
      id: request.id,
      title: request.title,
      subtitle: request.subtitle,
      flightResponses: await searchFlightsAcrossAirportCodes(request.query, onSearchFlights),
    })),
  )
}

// 校验 FlightsPage 的搜索条件是否满足提交要求。
export function validateFlightSearchState(searchState: {
  tripType: 'oneWay' | 'roundTrip' | 'multiCity'
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  multiCitySegments: Array<{
    departureAirport: string
    arrivalAirport: string
    departureDate: string
  }>
}): string | null {
  const routes =
    searchState.tripType === 'multiCity'
      ? searchState.multiCitySegments
      : [{ departureAirport: searchState.departureAirport, arrivalAirport: searchState.arrivalAirport }]

  if (
    routes.some(route => {
      const departureAirport = route.departureAirport.trim()
      const arrivalAirport = route.arrivalAirport.trim()
      return !departureAirport || !arrivalAirport
    })
  ) {
    return '小飞机找不到跑道呀：请先选好出发地和目的地。'
  }

  if (
    (searchState.tripType === 'multiCity' && searchState.multiCitySegments.some(segment => !segment.departureDate.trim())) ||
    (searchState.tripType !== 'multiCity' && !searchState.departureDate.trim()) ||
    (searchState.tripType === 'roundTrip' && !searchState.returnDate.trim())
  ) {
    return '小日历还没翻开呢：请先选好出发日期。'
  }

  if (
    routes.some(route => {
      const departureAirport = route.departureAirport.trim()
      const arrivalAirport = route.arrivalAirport.trim()
      return departureAirport === arrivalAirport
    })
  ) {
    return '小飞机原地转圈啦：出发地和目的地不能选成同一个地方。'
  }

  if (
    searchState.tripType === 'roundTrip' &&
    searchState.departureDate &&
    searchState.returnDate &&
    searchState.returnDate < searchState.departureDate
  ) {
    return '往返小飞机还没等去程起飞呢：返回时间不能早于出发时间。'
  }

  if (searchState.tripType === 'multiCity') {
    for (let index = 1; index < searchState.multiCitySegments.length; index += 1) {
      const previousDate = searchState.multiCitySegments[index - 1]?.departureDate
      const nextDate = searchState.multiCitySegments[index]?.departureDate
      if (previousDate && nextDate && nextDate < previousDate) {
        return '行程时间线打结啦：下一程时间不能早于上一程时间。'
      }
    }
  }

  return null
}

// 基于搜索条件加载每日最低价，作为结果区顶部日期条的数据来源。
export function loadDailyLowestPricesFromSearch(
  request: FlightDailyLowestPricesPlannerRequest,
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>,
): Promise<FlightDailyLowestPricesPlannerResponse> {
  const start = parseSearchDate(request.startDate)
  return Promise.all(
    Array.from({ length: request.days }, async (_, index) => {
      const date = new Date(start)
      date.setDate(start.getDate() + index)
      const dateText = formatDateInput(date)
      const flights = await onSearchFlights({
        departureAirport: request.departureAirport,
        arrivalAirport: request.arrivalAirport,
        date: dateText,
      })
      const priceValues = flights
        .flatMap(flight => flight.cabinInventories)
        .map(cabin => Number(cabin.unitPrice))
        .filter(Number.isFinite)
      const lowestPrice = priceValues.length > 0 ? Math.min(...priceValues) : null

      return {
        date: dateText,
        lowestPrice: lowestPrice === null ? null : String(lowestPrice),
        currency: lowestPrice === null ? null : 'CNY',
      }
    }),
  ).then(prices => ({ prices }))
}

// 当同一路线对应多个机场代码时，分别发起搜索。
export async function loadDailyLowestPricesAcrossAirportCodes(
  request: FlightDailyLowestPricesPlannerRequest,
  onLoadDailyLowestPrices: (payload: FlightDailyLowestPricesPlannerRequest) => Promise<FlightDailyLowestPricesPlannerResponse>,
): Promise<FlightDailyLowestPricesPlannerResponse> {
  const departureAirportOptions = expandAirportSearchValues(request.departureAirport)
  const arrivalAirportOptions = expandAirportSearchValues(request.arrivalAirport)

  if (departureAirportOptions.length <= 1 && arrivalAirportOptions.length <= 1) {
    return onLoadDailyLowestPrices(request)
  }

  const responses = await Promise.all(
    departureAirportOptions.flatMap(departureAirport =>
      arrivalAirportOptions.map(arrivalAirport =>
        onLoadDailyLowestPrices({
          ...request,
          departureAirport,
          arrivalAirport,
        }),
      ),
    ),
  )

  const pricesByDate = new Map<string, FlightDailyLowestPricePlannerResponse>()
  responses.flatMap(response => response.prices).forEach(price => {
    const currentPrice = pricesByDate.get(price.date)
    const nextAmount = price.lowestPrice === null ? null : Number(price.lowestPrice)
    const currentAmount = currentPrice?.lowestPrice === null || currentPrice?.lowestPrice === undefined ? null : Number(currentPrice.lowestPrice)

    if (!currentPrice || (nextAmount !== null && (currentAmount === null || nextAmount < currentAmount))) {
      pricesByDate.set(price.date, price)
    }
  })

  return { prices: [...pricesByDate.values()].sort((left, right) => left.date.localeCompare(right.date)) }
}

// 展开机场搜索值，必要时拆成多个机场代码。
export function expandAirportSearchValues(value: string | undefined): string[] {
  if (!value) {
    return []
  }

  const airportCodes = getFlightDetailsPlannerCityAirportCodes(value)
  if (airportCodes.length > 0) {
    return airportCodes
  }

  return [normalizeFlightAirportForApi(value) ?? value]
}

// 构造空的日期价格窗，保证结果区始终有固定长度。
export function buildEmptyDateWindow(startDate: string, days: number): FlightDailyLowestPricePlannerResponse[] {
  const start = parseSearchDate(startDate)
  return Array.from({ length: days }, (_, index) => {
    const date = new Date(start)
    date.setDate(start.getDate() + index)
    return {
      date: formatDateInput(date),
      lowestPrice: null,
      currency: null,
    }
  })
}

// 把机场代码转成页面可读的机场名称。
export function formatAirportName(value: string): string {
  return formatFlightAirportLabel(value)
}

// 把舱位代码转成页面可读的中文标签。
export function formatCabinLabel(value: string): string {
  return cabinLabelByClass[normalizeCabinClass(value)] ?? value
}

// 读取航司在页面上的展示名称。
export function getAirlineDisplayName(flight: FlightPlannerResponse): string {
  return getFlightDetailsPlannerAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)
}

// 读取航司在页面上的 logo 路径。
export function getAirlineLogoPath(flight: FlightPlannerResponse): string | null {
  return getFlightDetailsPlannerAirlineLogoPathByCode(flight.airlineCode, flight.airlineLogoPath)
}

// 规范化舱位字符串，统一成筛选使用的标准格式。
export function normalizeCabinClass(value: string): string {
  return value.trim().replace('-', '_').toUpperCase()
}

// 对字符串数组去重并过滤空值。
export function unique(values: string[]): string[] {
  return [...new Set(values.filter(Boolean))]
}

// 把搜索日期字符串解析成 Date，供日期窗计算使用。
export function parseSearchDate(value: string): Date {
  if (!value) {
    return new Date()
  }
  return new Date(`${value}T00:00:00`)
}

// 把 Date 转成 input date 需要的 yyyy-mm-dd 格式。
export function formatDateInput(date: Date): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 把航线日期格式化成页面卡片需要的短日期文本。
export function formatRouteDate(date: string): string {
  if (!date) {
    return ''
  }

  return new Date(`${date}T00:00:00`).toLocaleDateString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    weekday: 'short',
  })
}

// 把航班响应和当前舱位组合成页面展示对象。
export function toDisplayFlight(flight: FlightPlannerResponse, selectedCabin: string): DisplayFlight {
  const selectedInventory =
    selectedCabin === 'all'
      ? [...flight.cabinInventories].sort((left, right) => Number(left.unitPrice) - Number(right.unitPrice))[0]
      : flight.cabinInventories.find(cabin => normalizeCabinClass(cabin.cabinClass) === selectedCabin)
  const cabinClass = normalizeCabinClass(selectedInventory?.cabinClass ?? 'ECONOMY')
  const price = Number(selectedInventory?.unitPrice ?? flight.basePrice)

  return {
    flight,
    airlineName: getAirlineDisplayName(flight),
    airlineLogoPath: getAirlineLogoPath(flight),
    departureAirportName: formatAirportName(flight.departureAirport),
    arrivalAirportName: formatAirportName(flight.arrivalAirport),
    displayCabinClass: cabinClass,
    displayCabinLabel: formatCabinLabel(cabinClass),
    displayPrice: price,
    displayCurrency: selectedInventory?.currency ?? flight.currency,
    isDisplayCabinBookable: Boolean(selectedInventory?.isBookable),
    priceTone: 'standard',
  }
}

// 组装当前搜索条件下需要执行的航班查询任务。
function buildFlightSearchRequests(searchState: {
  tripType: 'oneWay' | 'roundTrip' | 'multiCity'
  departureAirport: string
  arrivalAirport: string
  departureDate: string
  returnDate: string
  multiCitySegments: Array<{
    id: string
    departureAirport: string
    arrivalAirport: string
    departureDate: string
    arrivalDate: string
  }>
}): Array<{ id: string; title: string; subtitle: string; query: FlightSearchPlannerRequest }> {
  if (searchState.tripType === 'multiCity') {
    return searchState.multiCitySegments.map((segment, index) => ({
      id: segment.id,
      title: `第 ${index + 1} 程`,
      subtitle: `${formatFlightRouteCity(segment.departureAirport)} -> ${formatFlightRouteCity(segment.arrivalAirport)} ${segment.departureDate}`,
      query: {
        departureAirport: normalizeFlightAirportForApi(segment.departureAirport),
        arrivalAirport: normalizeFlightAirportForApi(segment.arrivalAirport),
        date: segment.departureDate || undefined,
      },
    }))
  }

  if (searchState.tripType === 'roundTrip') {
    return [
      {
        id: 'outbound',
        title: '去程',
        subtitle: `${formatFlightRouteCity(searchState.departureAirport)} -> ${formatFlightRouteCity(searchState.arrivalAirport)} ${searchState.departureDate}`,
        query: {
          departureAirport: normalizeFlightAirportForApi(searchState.departureAirport),
          arrivalAirport: normalizeFlightAirportForApi(searchState.arrivalAirport),
          date: searchState.departureDate || undefined,
        },
      },
      {
        id: 'return',
        title: '返程',
        subtitle: `${formatFlightRouteCity(searchState.arrivalAirport)} -> ${formatFlightRouteCity(searchState.departureAirport)} ${searchState.returnDate}`,
        query: {
          departureAirport: normalizeFlightAirportForApi(searchState.arrivalAirport),
          arrivalAirport: normalizeFlightAirportForApi(searchState.departureAirport),
          date: searchState.returnDate || undefined,
        },
      },
    ]
  }

  return [
    {
      id: 'one-way',
      title: '单程',
      subtitle: `${formatFlightRouteCity(searchState.departureAirport)} -> ${formatFlightRouteCity(searchState.arrivalAirport)} ${searchState.departureDate}`,
      query: {
        departureAirport: normalizeFlightAirportForApi(searchState.departureAirport),
        arrivalAirport: normalizeFlightAirportForApi(searchState.arrivalAirport),
        date: searchState.departureDate || undefined,
      },
    },
  ]
}

// 当同一路线对应多个机场代码时，分别发起搜索。
async function searchFlightsAcrossAirportCodes(
  query: FlightSearchPlannerRequest,
  onSearchFlights: (payload: FlightSearchPlannerRequest) => Promise<FlightPlannerResponse[]>,
): Promise<FlightPlannerResponse[]> {
  const departureAirportOptions = expandAirportSearchValues(query.departureAirport)
  const arrivalAirportOptions = expandAirportSearchValues(query.arrivalAirport)

  if (departureAirportOptions.length <= 1 && arrivalAirportOptions.length <= 1) {
    return onSearchFlights(query)
  }

  const responses = await Promise.all(
    departureAirportOptions.flatMap(departureAirport =>
      arrivalAirportOptions.map(arrivalAirport =>
        onSearchFlights({
          ...query,
          departureAirport,
          arrivalAirport,
        }),
      ),
    ),
  )
  return uniqueFlights(responses.flat())
}

// 按 flightId 去重，避免机场展开后出现重复航班。
function uniqueFlights(flights: FlightPlannerResponse[]): FlightPlannerResponse[] {
  const seenFlightIds = new Set<string>()
  return flights.filter(flight => {
    if (seenFlightIds.has(flight.flightId)) {
      return false
    }
    seenFlightIds.add(flight.flightId)
    return true
  })
}

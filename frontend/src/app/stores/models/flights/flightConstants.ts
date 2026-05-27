import type { FlightSearchSegment, FlightSearchState } from '@/app/stores/models/flights/flightTypes'
import type { AircraftModel } from '@/microservices/flight/objects/AircraftModel'
import type { FlightCity } from '@/microservices/flight/objects/FlightCity'

export const flightCities: FlightCity[] = [
  { cityName: '北京', airports: [{ airportCode: 'PEK', cityName: '北京', airportName: '首都国际机场' }, { airportCode: 'PKX', cityName: '北京', airportName: '大兴国际机场' }] },
  { cityName: '上海', airports: [{ airportCode: 'SHA', cityName: '上海', airportName: '虹桥国际机场' }, { airportCode: 'PVG', cityName: '上海', airportName: '浦东国际机场' }] },
  { cityName: '广州', airports: [{ airportCode: 'CAN', cityName: '广州', airportName: '白云国际机场' }] },
  { cityName: '深圳', airports: [{ airportCode: 'SZX', cityName: '深圳', airportName: '宝安国际机场' }] },
  { cityName: '成都', airports: [{ airportCode: 'CTU', cityName: '成都', airportName: '双流国际机场' }, { airportCode: 'TFU', cityName: '成都', airportName: '天府国际机场' }] },
  { cityName: '重庆', airports: [{ airportCode: 'CKG', cityName: '重庆', airportName: '江北国际机场' }] },
  { cityName: '杭州', airports: [{ airportCode: 'HGH', cityName: '杭州', airportName: '萧山国际机场' }] },
  { cityName: '南京', airports: [{ airportCode: 'NKG', cityName: '南京', airportName: '禄口国际机场' }] },
  { cityName: '武汉', airports: [{ airportCode: 'WUH', cityName: '武汉', airportName: '天河国际机场' }] },
  { cityName: '西安', airports: [{ airportCode: 'XIY', cityName: '西安', airportName: '咸阳国际机场' }] },
  { cityName: '天津', airports: [{ airportCode: 'TSN', cityName: '天津', airportName: '滨海国际机场' }] },
  { cityName: '郑州', airports: [{ airportCode: 'CGO', cityName: '郑州', airportName: '新郑国际机场' }] },
  { cityName: '长沙', airports: [{ airportCode: 'CSX', cityName: '长沙', airportName: '黄花国际机场' }] },
  { cityName: '青岛', airports: [{ airportCode: 'TAO', cityName: '青岛', airportName: '胶东国际机场' }] },
  { cityName: '厦门', airports: [{ airportCode: 'XMN', cityName: '厦门', airportName: '高崎国际机场' }] },
]

export const flightCityOptions = flightCities.map(city => city.cityName)

export const flightAirportOptions = flightCities.flatMap(city =>
  city.airports.map(airport => ({
    airportCode: airport.airportCode,
    cityName: city.cityName,
    airportName: airport.airportName,
    label: `${city.cityName}${airport.airportName}`,
  })),
)

const airportOptionByCode = new Map(flightAirportOptions.map(option => [option.airportCode, option]))
const cityByAirportCode = new Map(flightAirportOptions.map(option => [option.airportCode, option.cityName]))

export function normalizeFlightAirportForApi(value: string | undefined): string | undefined {
  const trimmedValue = value?.trim()
  if (!trimmedValue) {
    return undefined
  }

  const upperValue = trimmedValue.toUpperCase()
  if (airportOptionByCode.has(upperValue)) {
    return upperValue
  }

  const matchedOption = flightAirportOptions.find(
    option => option.label === trimmedValue || option.airportName === trimmedValue || `${option.cityName}${option.airportName}` === trimmedValue,
  )
  if (matchedOption) {
    return matchedOption.airportCode
  }

  return trimmedValue
}

export function formatFlightAirportLabel(value: string): string {
  const option = airportOptionByCode.get(value.toUpperCase())
  return option?.label ?? value
}

export function formatFlightRouteCity(value: string): string {
  const option = airportOptionByCode.get(value.toUpperCase())
  return option?.cityName ?? value
}

export function getFlightDetailsPlannerCityAirportCodes(cityOrAirport: string): string[] {
  const cityName = formatFlightRouteCity(cityOrAirport)
  return flightCities.find(city => city.cityName === cityName)?.airports.map(airport => airport.airportCode) ?? []
}

export function getFlightDetailsPlannerCityByAirportCode(airportCode: string): string {
  return cityByAirportCode.get(airportCode.toUpperCase()) ?? airportCode
}

export const aircraftModels: AircraftModel[] = [
  { modelName: 'A320neo' },
  { modelName: 'A321neo' },
  { modelName: 'B737-800' },
  { modelName: 'B787-9' },
  { modelName: 'C919' },
]

export function createFlightSearchSegment(
  id: string,
  departureAirport: string,
  arrivalAirport: string,
  departureDate: string,
  arrivalDate = departureDate,
): FlightSearchSegment {
  return {
    id,
    departureAirport,
    arrivalAirport,
    departureDate,
    arrivalDate,
  }
}

export const defaultFlightSearchState: FlightSearchState = {
  tripType: 'oneWay',
  departureAirport: '',
  arrivalAirport: '',
  departureDate: '',
  returnDate: '',
  multiCitySegments: [
    createFlightSearchSegment('segment-1', '', '', ''),
    createFlightSearchSegment('segment-2', '', '', ''),
  ],
  adults: 1,
  childrenCount: 0,
}

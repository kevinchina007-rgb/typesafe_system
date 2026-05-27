import type {
  FlightDailyLowestPricesPlannerRequest,
  FlightDailyLowestPricesPlannerResponse,
} from '@/microservices/flight/objects/FlightDailyLowestPrices'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const flightDailyLowestPricesPlanner = (
  payload: FlightDailyLowestPricesPlannerRequest,
): Promise<FlightDailyLowestPricesPlannerResponse> =>
  executeJsonApiRequest('/FlightDailyLowestPricesPlanner', 'POST', payload)

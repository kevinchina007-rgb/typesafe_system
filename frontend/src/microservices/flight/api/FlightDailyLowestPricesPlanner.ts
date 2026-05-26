import type {
  FlightDailyLowestPricesRequest,
  FlightDailyLowestPricesResponse,
} from '@/microservices/flight/objects/FlightDailyLowestPrices'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listFlightDailyLowestPrices = (
  payload: FlightDailyLowestPricesRequest,
): Promise<FlightDailyLowestPricesResponse> =>
  executeJsonApiRequest('/FlightDailyLowestPricesPlanner', 'POST', payload)

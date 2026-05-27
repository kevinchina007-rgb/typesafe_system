import type { FlightListPlannerResponse } from '@/microservices/flight/objects/FlightListPlannerResponse'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchPlannerRequest'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const searchFlightsPlanner = (query: FlightSearchPlannerRequest): Promise<FlightListPlannerResponse> =>
  executeJsonApiRequest('/SearchFlightsPlanner', 'POST', query)

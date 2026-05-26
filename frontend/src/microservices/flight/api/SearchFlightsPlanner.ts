import type { FlightListPlannerResponse } from '@/microservices/flight/objects/FlightListResponse'
import type { FlightSearchPlannerRequest } from '@/microservices/flight/objects/FlightSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listFlights = (query: FlightSearchPlannerRequest): Promise<FlightListPlannerResponse> =>
  executeJsonApiRequest('/SearchFlightsPlanner', 'POST', query)

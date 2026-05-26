import type { FlightListResponse } from '@/microservices/flight/objects/FlightListResponse'
import type { FlightSearchQuery } from '@/microservices/flight/objects/FlightSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listFlights = (query: FlightSearchQuery): Promise<FlightListResponse> =>
  executeJsonApiRequest('/SearchFlightsPlanner', 'POST', query)

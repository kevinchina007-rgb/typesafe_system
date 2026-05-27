import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightPlannerResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getFlightDetailsPlanner = (flightId: string): Promise<FlightPlannerResponse> =>
  executeJsonApiRequest('/GetFlightDetailsPlanner', 'POST', { flightId })

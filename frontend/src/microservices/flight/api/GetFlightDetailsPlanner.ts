import type { FlightPlannerResponse } from '@/microservices/flight/objects/FlightResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getFlight = (flightId: string): Promise<FlightPlannerResponse> =>
  executeJsonApiRequest('/GetFlightDetailsPlanner', 'POST', { flightId })

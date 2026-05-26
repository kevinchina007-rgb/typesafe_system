import type { FlightResponse } from '@/microservices/flight/objects/FlightResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const getFlight = (flightId: string): Promise<FlightResponse> =>
  executeJsonApiRequest('/GetFlightDetailsPlanner', 'POST', { flightId })

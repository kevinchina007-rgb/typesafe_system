import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightPlannerRequest'
import type { FlightBookingPlannerResponse } from '@/microservices/flight/objects/FlightBookingPlannerResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const bookFlightPlanner = (payload: BookFlightPlannerRequest): Promise<FlightBookingPlannerResponse> =>
  executeJsonApiRequest('/BookFlightPlanner', 'POST', payload)

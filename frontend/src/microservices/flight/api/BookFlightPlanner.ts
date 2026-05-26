import type { BookFlightPlannerRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { FlightBookingPlannerResponse } from '@/microservices/flight/objects/FlightBookingPlannerResponse'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const createFlightOrder = (payload: BookFlightPlannerRequest): Promise<FlightBookingPlannerResponse> =>
  executeJsonApiRequest('/BookFlightPlanner', 'POST', payload)

import type { BookHotelPlannerRequest } from '@/microservices/hotel/objects/BookHotelPlannerRequest'
import type { HotelBookingPlannerResponse } from '@/microservices/hotel/objects/HotelBookingPlannerResponse'
import { executeJsonApiRequest } from '@/shared-kernel/api/ApiTransport'

export const bookHotelPlanner = (payload: BookHotelPlannerRequest): Promise<HotelBookingPlannerResponse> =>
  executeJsonApiRequest('/BookHotelPlanner', 'POST', payload)

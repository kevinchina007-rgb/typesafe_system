import type { BookFlightRequest } from '@/microservices/flight/objects/BookFlightRequest'
import type { FlightListResponse } from '@/microservices/flight/objects/FlightListResponse'
import type { FlightResponse } from '@/microservices/flight/objects/FlightResponse'
import type { FlightSearchQuery } from '@/microservices/flight/objects/FlightSearchQuery'
import type { OrderResponse } from '@/microservices/order/objects/OrderResponse'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listFlights = (query: FlightSearchQuery): Promise<FlightListResponse> =>
    executeApiRequest(`/flights${createQueryString(query)}`)

export const getFlight = (flightId: string): Promise<FlightResponse> =>
    executeApiRequest(`/flights/${flightId}`)

export const createFlightOrder = (payload: BookFlightRequest): Promise<OrderResponse> =>
    executeJsonApiRequest('/flights/book', 'POST', payload)

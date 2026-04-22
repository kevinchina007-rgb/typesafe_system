import type { BookFlightRequestDto, FlightListResponse, FlightResponse, FlightSearchQueryDto } from '../api-dtos/flights'
import type { OrderResponse } from '../api-dtos/orders'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const flightApiClient = {
  listFlights: (query: FlightSearchQueryDto): Promise<FlightListResponse> =>
    executeApiRequest(`/flights${createQueryString(query)}`),

  getFlight: (flightId: string): Promise<FlightResponse> =>
    executeApiRequest(`/flights/${flightId}`),

  createFlightOrder: (payload: BookFlightRequestDto): Promise<OrderResponse> =>
    executeJsonApiRequest('/flights/book', 'POST', payload),
}

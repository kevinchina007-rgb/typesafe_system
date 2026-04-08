import type { AttractionListResponse, AttractionResponse, FlightListResponse, FlightResponse, HotelListResponse, HotelResponse, TrainListResponse, TrainResponse } from '../api-dtos'
import { createQueryString, executeApiRequest } from '../api-transport'

export const resourceApiClient = {
  listFlights: (query: { departureAirport?: string; arrivalAirport?: string; date?: string }): Promise<FlightListResponse> =>
    executeApiRequest(`/flights${createQueryString(query)}`),

  getFlight: (flightId: string): Promise<FlightResponse> =>
    executeApiRequest(`/flights/${flightId}`),

  listHotels: (query: { location?: string; checkInDate?: string; checkOutDate?: string }): Promise<HotelListResponse> =>
    executeApiRequest(`/hotels${createQueryString(query)}`),

  getHotel: (hotelId: string, query?: { checkInDate?: string; checkOutDate?: string }): Promise<HotelResponse> =>
    executeApiRequest(`/hotels/${hotelId}${createQueryString(query ?? {})}`),

  listTrains: (query: { fromStation?: string; toStation?: string; date?: string }): Promise<TrainListResponse> =>
    executeApiRequest(`/trains${createQueryString(query)}`),

  getTrain: (trainId: string): Promise<TrainResponse> =>
    executeApiRequest(`/trains/${trainId}`),

  listAttractions: (query?: { city?: string; useDate?: string }): Promise<AttractionListResponse> =>
    executeApiRequest(`/attractions${createQueryString(query ?? {})}`),

  getAttraction: (attractionId: string, query?: { useDate?: string }): Promise<AttractionResponse> =>
    executeApiRequest(`/attractions/${attractionId}${createQueryString(query ?? {})}`),
}

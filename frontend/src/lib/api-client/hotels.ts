import type { BookHotelRequestDto, HotelListResponse, HotelResponse, HotelSearchQueryDto } from '../api-dtos/hotels'
import type { OrderResponse } from '../api-dtos/orders'
import { createQueryString, executeApiRequest, executeJsonApiRequest } from '../api-transport'

export const hotelApiClient = {
  listHotels: (query: HotelSearchQueryDto): Promise<HotelListResponse> =>
    executeApiRequest(`/hotels${createQueryString(query)}`),

  getHotel: (hotelId: string, query?: Pick<HotelSearchQueryDto, 'checkInDate' | 'checkOutDate'>): Promise<HotelResponse> =>
    executeApiRequest(`/hotels/${hotelId}${createQueryString(query ?? {})}`),

  createHotelOrder: (payload: BookHotelRequestDto): Promise<OrderResponse> =>
    executeJsonApiRequest('/hotels/book', 'POST', payload),
}

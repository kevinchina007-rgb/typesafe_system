import type { HotelListResponse } from '@/microservices/hotel/objects/HotelListResponse'
import type { HotelResponse } from '@/microservices/hotel/objects/HotelResponse'
import type { HotelSearchQuery } from '@/microservices/hotel/objects/HotelSearchQuery'
import { executeJsonApiRequest } from '@/microservices/common/api/ApiTransport'

export const listHotels = (query: HotelSearchQuery): Promise<HotelListResponse> =>
  executeJsonApiRequest('/SearchHotelsPlanner', 'POST', query)

export const getHotel = (hotelId: string, query?: Pick<HotelSearchQuery, 'checkInDate' | 'checkOutDate'>): Promise<HotelResponse> =>
  executeJsonApiRequest('/GetHotelDetailsPlanner', 'POST', {
    hotelId,
    checkInDate: query?.checkInDate,
    checkOutDate: query?.checkOutDate,
  })

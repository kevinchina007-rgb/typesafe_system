import type { HotelListResponse } from '@/microservices/hotel/objects/HotelListResponse'
import type { HotelResponse } from '@/microservices/hotel/objects/HotelResponse'
import type { HotelSearchQuery } from '@/microservices/hotel/objects/HotelSearchQuery'
import { createQueryString, executeApiRequest } from '@/microservices/common/api/ApiTransport'

export const listHotels = (query: HotelSearchQuery): Promise<HotelListResponse> =>
    executeApiRequest(`/hotels${createQueryString(query)}`)

export const getHotel = (hotelId: string, query?: Pick<HotelSearchQuery, 'checkInDate' | 'checkOutDate'>): Promise<HotelResponse> =>
    executeApiRequest(`/hotels/${hotelId}${createQueryString(query ?? {})}`)

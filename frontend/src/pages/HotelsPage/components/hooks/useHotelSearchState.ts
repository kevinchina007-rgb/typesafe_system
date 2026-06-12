// 本文件定义 HotelsPage 页面的状态控制逻辑，负责条件维护、请求触发和动作调度。

import { useHotelSearchStore } from '@/app/stores/hotel-search-store'

export function useHotelSearchState() {
  const hotelResponses = useHotelSearchStore(state => state.hotelResponses)
  const hasSearchedHotels = useHotelSearchStore(state => state.hasSearchedHotels)
  const searchLocation = useHotelSearchStore(state => state.searchLocation)
  const searchCheckInDate = useHotelSearchStore(state => state.searchCheckInDate)
  const searchCheckOutDate = useHotelSearchStore(state => state.searchCheckOutDate)
  const roomCount = useHotelSearchStore(state => state.roomCount)
  const guestCount = useHotelSearchStore(state => state.guestCount)
  const hotelPreference = useHotelSearchStore(state => state.hotelPreference)
  const nearbyPreference = useHotelSearchStore(state => state.nearbyPreference)
  const selectedQuickDatePreset = useHotelSearchStore(state => state.selectedQuickDatePreset)
  const setHotelPlannerResponses = useHotelSearchStore(state => state.setHotelPlannerResponses)
  const setHasSearchedHotels = useHotelSearchStore(state => state.setHasSearchedHotels)
  const setSearchLocation = useHotelSearchStore(state => state.setSearchLocation)
  const setSearchCheckInDate = useHotelSearchStore(state => state.setSearchCheckInDate)
  const setSearchCheckOutDate = useHotelSearchStore(state => state.setSearchCheckOutDate)
  const setRoomCount = useHotelSearchStore(state => state.setRoomCount)
  const setGuestCount = useHotelSearchStore(state => state.setGuestCount)
  const setHotelPreference = useHotelSearchStore(state => state.setHotelPreference)
  const setNearbyPreference = useHotelSearchStore(state => state.setNearbyPreference)
  const setSelectedQuickDatePreset = useHotelSearchStore(state => state.setSelectedQuickDatePreset)

  return {
    hotelResponses,
    hasSearchedHotels,
    searchLocation,
    searchCheckInDate,
    searchCheckOutDate,
    roomCount,
    guestCount,
    hotelPreference,
    nearbyPreference,
    selectedQuickDatePreset,
    setHotelPlannerResponses,
    setHasSearchedHotels,
    setSearchLocation,
    setSearchCheckInDate,
    setSearchCheckOutDate,
    setRoomCount,
    setGuestCount,
    setHotelPreference,
    setNearbyPreference,
    setSelectedQuickDatePreset,
  }
}

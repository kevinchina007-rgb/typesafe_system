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
  const setHotelResponses = useHotelSearchStore(state => state.setHotelResponses)
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
    setHotelResponses,
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

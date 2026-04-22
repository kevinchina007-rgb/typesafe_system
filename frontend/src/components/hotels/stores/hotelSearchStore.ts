import { create } from 'zustand'

import type { HotelResponse } from '../../../lib/mvp-types'
import {
  defaultHotelSearchState,
  type HotelPreference,
  type HotelQuickDatePreset,
} from '../hotelBookingModel'

type HotelSearchStore = {
  hotelResponses: HotelResponse[]
  hasSearchedHotels: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  roomCount: number
  guestCount: number
  hotelPreference: HotelPreference
  nearbyPreference: string
  selectedQuickDatePreset: HotelQuickDatePreset | null
  setHotelResponses: (hotelResponses: HotelResponse[]) => void
  setHasSearchedHotels: (hasSearchedHotels: boolean) => void
  setSearchLocation: (searchLocation: string) => void
  setSearchCheckInDate: (searchCheckInDate: string) => void
  setSearchCheckOutDate: (searchCheckOutDate: string) => void
  setRoomCount: (roomCount: number) => void
  setGuestCount: (guestCount: number) => void
  setHotelPreference: (hotelPreference: HotelPreference) => void
  setNearbyPreference: (nearbyPreference: string) => void
  setSelectedQuickDatePreset: (selectedQuickDatePreset: HotelQuickDatePreset | null) => void
}

export const useHotelSearchStore = create<HotelSearchStore>(set => ({
  hotelResponses: [],
  hasSearchedHotels: false,
  searchLocation: defaultHotelSearchState.location,
  searchCheckInDate: defaultHotelSearchState.checkInDate,
  searchCheckOutDate: defaultHotelSearchState.checkOutDate,
  roomCount: defaultHotelSearchState.roomCount,
  guestCount: defaultHotelSearchState.guestCount,
  hotelPreference: defaultHotelSearchState.hotelPreference,
  nearbyPreference: defaultHotelSearchState.nearbyPreference,
  selectedQuickDatePreset: defaultHotelSearchState.selectedQuickDatePreset,
  setHotelResponses: hotelResponses => set({ hotelResponses }),
  setHasSearchedHotels: hasSearchedHotels => set({ hasSearchedHotels }),
  setSearchLocation: searchLocation => set({ searchLocation }),
  setSearchCheckInDate: searchCheckInDate => set({ searchCheckInDate }),
  setSearchCheckOutDate: searchCheckOutDate => set({ searchCheckOutDate }),
  setRoomCount: roomCount => set({ roomCount }),
  setGuestCount: guestCount => set({ guestCount }),
  setHotelPreference: hotelPreference => set({ hotelPreference }),
  setNearbyPreference: nearbyPreference => set({ nearbyPreference }),
  setSelectedQuickDatePreset: selectedQuickDatePreset => set({ selectedQuickDatePreset }),
}))

export function getHotelSearchSnap() {
  const {
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
  } = useHotelSearchStore.getState()
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
  }
}

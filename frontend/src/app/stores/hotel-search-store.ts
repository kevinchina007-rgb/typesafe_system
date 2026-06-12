// 本文件封装状态管理逻辑。

import type { HotelPreference, HotelQuickDatePreset } from '@/app/stores/models/hotel-booking-model'
﻿import { create } from 'zustand'

import type { HotelPlannerResponse } from '@/lib/mvp-types/index'
import { defaultHotelSearchState } from '@/app/stores/models/hotel-booking-model'

type HotelSearchStore = {
  hotelResponses: HotelPlannerResponse[]
  hasSearchedHotels: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  roomCount: number
  guestCount: number
  hotelPreference: HotelPreference
  nearbyPreference: string
  selectedQuickDatePreset: HotelQuickDatePreset | null
  setHotelPlannerResponses: (hotelResponses: HotelPlannerResponse[]) => void
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
  setHotelPlannerResponses: hotelResponses => set({ hotelResponses }),
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

export function getHotelDetailsPlannerSearchSnap() {
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

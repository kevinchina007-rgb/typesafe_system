import { create } from 'zustand'

import type { TrainResponse } from '../../../lib/mvp-types'
import {
  defaultTrainSearchState,
  type TrainQuickDatePreset,
  type TrainSeatPreference,
  type TrainTripType,
  type TrainTypePreference,
} from '../trainBookingModel'

type TrainSearchStore = {
  trainResponses: TrainResponse[]
  hasSearchedTrains: boolean
  tripType: TrainTripType
  searchDate: string
  returnDate: string
  searchFromStation: string
  searchToStation: string
  passengerCount: number
  seatPreference: TrainSeatPreference
  trainTypePreference: TrainTypePreference
  selectedQuickDatePreset: TrainQuickDatePreset | null
  setTrainResponses: (trainResponses: TrainResponse[]) => void
  setHasSearchedTrains: (hasSearchedTrains: boolean) => void
  setTripType: (tripType: TrainTripType) => void
  setSearchDate: (searchDate: string) => void
  setReturnDate: (returnDate: string) => void
  setSearchFromStation: (searchFromStation: string) => void
  setSearchToStation: (searchToStation: string) => void
  setPassengerCount: (passengerCount: number) => void
  setSeatPreference: (seatPreference: TrainSeatPreference) => void
  setTrainTypePreference: (trainTypePreference: TrainTypePreference) => void
  setSelectedQuickDatePreset: (selectedQuickDatePreset: TrainQuickDatePreset | null) => void
}

export const useTrainSearchStore = create<TrainSearchStore>(set => ({
  trainResponses: [],
  hasSearchedTrains: false,
  tripType: defaultTrainSearchState.tripType,
  searchDate: defaultTrainSearchState.date,
  returnDate: defaultTrainSearchState.returnDate,
  searchFromStation: defaultTrainSearchState.fromStation,
  searchToStation: defaultTrainSearchState.toStation,
  passengerCount: defaultTrainSearchState.passengerCount,
  seatPreference: defaultTrainSearchState.seatPreference,
  trainTypePreference: defaultTrainSearchState.trainTypePreference,
  selectedQuickDatePreset: defaultTrainSearchState.selectedQuickDatePreset,
  setTrainResponses: trainResponses => set({ trainResponses }),
  setHasSearchedTrains: hasSearchedTrains => set({ hasSearchedTrains }),
  setTripType: tripType => set({ tripType }),
  setSearchDate: searchDate => set({ searchDate }),
  setReturnDate: returnDate => set({ returnDate }),
  setSearchFromStation: searchFromStation => set({ searchFromStation }),
  setSearchToStation: searchToStation => set({ searchToStation }),
  setPassengerCount: passengerCount => set({ passengerCount }),
  setSeatPreference: seatPreference => set({ seatPreference }),
  setTrainTypePreference: trainTypePreference => set({ trainTypePreference }),
  setSelectedQuickDatePreset: selectedQuickDatePreset => set({ selectedQuickDatePreset }),
}))

export function getTrainSearchSnap() {
  const {
    trainResponses,
    hasSearchedTrains,
    tripType,
    searchDate,
    returnDate,
    searchFromStation,
    searchToStation,
    passengerCount,
    seatPreference,
    trainTypePreference,
    selectedQuickDatePreset,
  } = useTrainSearchStore.getState()
  return {
    trainResponses,
    hasSearchedTrains,
    tripType,
    searchDate,
    returnDate,
    searchFromStation,
    searchToStation,
    passengerCount,
    seatPreference,
    trainTypePreference,
    selectedQuickDatePreset,
  }
}

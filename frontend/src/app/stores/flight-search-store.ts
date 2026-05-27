import { create } from 'zustand'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightResultGroup, FlightSearchState } from '@/app/stores/models/flights/flightTypes'
import { createFlightSearchSegment, defaultFlightSearchState } from '@/app/stores/models/flights'

function buildDefaultSearchState(tripType: FlightSearchState['tripType']): FlightSearchState {
  return {
    ...defaultFlightSearchState,
    tripType,
    returnDate: tripType === 'oneWay' ? '' : defaultFlightSearchState.returnDate,
    multiCitySegments: defaultFlightSearchState.multiCitySegments.map(segment => ({ ...segment })),
  }
}

type FlightSearchStoreState = {
  searchState: FlightSearchState
  flightResponses: FlightPlannerResponse[]
  flightResultGroups: FlightResultGroup[]
  hasSearchedFlights: boolean
}

type FlightSearchStoreActions = {
  setSearchState: (searchState: FlightSearchState) => void
  setFlightPlannerResponses: (flightResponses: FlightPlannerResponse[]) => void
  setFlightResultGroups: (flightResultGroups: FlightResultGroup[]) => void
  setHasSearchedFlights: (hasSearchedFlights: boolean) => void
  updateSearchState: <K extends keyof FlightSearchState>(key: K, value: FlightSearchState[K]) => void
  updateTripType: (tripType: FlightSearchState['tripType']) => void
  updateMultiCitySegment: (
    segmentId: string,
    key: 'departureAirport' | 'arrivalAirport' | 'departureDate' | 'arrivalDate',
    value: string,
  ) => void
  addMultiCitySegment: () => void
  removeMultiCitySegment: (segmentId: string) => void
}

type FlightSearchStore = FlightSearchStoreState & FlightSearchStoreActions

const flightSearchStoreDefaultState: FlightSearchStoreState = {
  searchState: defaultFlightSearchState,
  flightResponses: [],
  flightResultGroups: [],
  hasSearchedFlights: false,
}

export const useFlightSearchStore = create<FlightSearchStore>(set => ({
  ...flightSearchStoreDefaultState,
  setSearchState: searchState => set({ searchState }),
  setFlightPlannerResponses: flightResponses => set({ flightResponses }),
  setFlightResultGroups: flightResultGroups => set({ flightResultGroups }),
  setHasSearchedFlights: hasSearchedFlights => set({ hasSearchedFlights }),
  updateSearchState: (key, value) =>
    set(state => ({
      searchState: {
        ...state.searchState,
        [key]: value,
      },
    })),
  updateTripType: tripType =>
    set({
      searchState: buildDefaultSearchState(tripType),
      flightResponses: [],
      flightResultGroups: [],
      hasSearchedFlights: false,
    }),
  updateMultiCitySegment: (segmentId, key, value) =>
    set(state => ({
      searchState: {
        ...state.searchState,
        multiCitySegments: state.searchState.multiCitySegments.map(segment =>
          segment.id === segmentId ? { ...segment, [key]: value } : segment,
        ),
      },
    })),
  addMultiCitySegment: () =>
    set(state => {
      const nextIndex = state.searchState.multiCitySegments.length + 1
      const previousSegment = state.searchState.multiCitySegments[state.searchState.multiCitySegments.length - 1]
      return {
        searchState: {
          ...state.searchState,
          multiCitySegments: [
            ...state.searchState.multiCitySegments,
            createFlightSearchSegment(
              `segment-${nextIndex}`,
              previousSegment?.arrivalAirport || state.searchState.arrivalAirport,
              '',
              previousSegment?.departureDate || state.searchState.departureDate,
            ),
          ],
        },
      }
    }),
  removeMultiCitySegment: segmentId =>
    set(state => ({
      searchState: {
        ...state.searchState,
        multiCitySegments: state.searchState.multiCitySegments.filter(segment => segment.id !== segmentId),
      },
    })),
}))

export function getFlightDetailsPlannerSearchSnap() {
  const { searchState, flightResponses, flightResultGroups, hasSearchedFlights } = useFlightSearchStore.getState()
  return { searchState, flightResponses, flightResultGroups, hasSearchedFlights }
}

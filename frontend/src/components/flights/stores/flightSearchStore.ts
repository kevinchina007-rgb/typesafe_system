import { create } from 'zustand'

import type { FlightResponse } from '../../../lib/mvp-types/flights'
import {
  createFlightSearchSegment,
  defaultFlightSearchState,
  getQuickDatePresetSegments,
  getQuickDatePresetValue,
  type FlightSearchState,
  type QuickDatePreset,
} from '../flightSearchModel'

type FlightSearchStoreState = {
  searchState: FlightSearchState
  flightResponses: FlightResponse[]
  hasSearchedFlights: boolean
}

type FlightSearchStoreActions = {
  setSearchState: (searchState: FlightSearchState) => void
  setFlightResponses: (flightResponses: FlightResponse[]) => void
  setHasSearchedFlights: (hasSearchedFlights: boolean) => void
  updateSearchState: <K extends keyof FlightSearchState>(key: K, value: FlightSearchState[K]) => void
  applyQuickDatePreset: (preset: QuickDatePreset) => void
  updateTripType: (tripType: FlightSearchState['tripType']) => void
  updateMultiCitySegment: (
    segmentId: string,
    key: 'departureAirport' | 'arrivalAirport' | 'departureDate' | 'arrivalDate',
    value: string,
  ) => void
  addMultiCitySegment: () => void
  removeMultiCitySegment: (segmentId: string) => void
  applyRouteSelection: (departureLabel: string, arrivalLabel: string) => void
}

type FlightSearchStore = FlightSearchStoreState & FlightSearchStoreActions

const flightSearchStoreDefaultState: FlightSearchStoreState = {
  searchState: defaultFlightSearchState,
  flightResponses: [],
  hasSearchedFlights: false,
}

export const useFlightSearchStore = create<FlightSearchStore>(set => ({
  ...flightSearchStoreDefaultState,
  setSearchState: searchState => set({ searchState }),
  setFlightResponses: flightResponses => set({ flightResponses }),
  setHasSearchedFlights: hasSearchedFlights => set({ hasSearchedFlights }),
  updateSearchState: (key, value) =>
    set(state => ({
      searchState: {
        ...state.searchState,
        [key]: value,
      },
    })),
  applyQuickDatePreset: preset =>
    set(state => {
      const nextDates = getQuickDatePresetValue(preset, state.searchState.tripType)
      if (state.searchState.tripType === 'multiCity') {
        return {
          searchState: {
            ...state.searchState,
            selectedQuickDatePreset: preset,
            multiCitySegments: getQuickDatePresetSegments(preset, state.searchState.multiCitySegments),
          },
        }
      }

      return {
        searchState: {
          ...state.searchState,
          selectedQuickDatePreset: preset,
          departureDate: nextDates.departureDate,
          returnDate:
            state.searchState.tripType === 'roundTrip'
              ? nextDates.returnDate ?? state.searchState.returnDate
              : '',
        },
      }
    }),
  updateTripType: tripType =>
    set(state => {
      if (tripType === 'multiCity') {
        return {
          searchState: {
            ...state.searchState,
            tripType,
            multiCitySegments:
              state.searchState.multiCitySegments.length >= 2
                ? state.searchState.multiCitySegments
                : [
                    createFlightSearchSegment(
                      'segment-1',
                      state.searchState.departureAirport,
                      state.searchState.arrivalAirport,
                      state.searchState.departureDate,
                    ),
                    createFlightSearchSegment(
                      'segment-2',
                      state.searchState.arrivalAirport,
                      '香港',
                      state.searchState.returnDate || state.searchState.departureDate,
                    ),
                  ],
          },
        }
      }

      return {
        searchState: {
          ...state.searchState,
          tripType,
          returnDate: tripType === 'roundTrip' ? state.searchState.returnDate || state.searchState.departureDate : '',
        },
      }
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
  applyRouteSelection: (departureLabel, arrivalLabel) =>
    set(state => ({
      searchState: {
        ...state.searchState,
        departureAirport: departureLabel,
        arrivalAirport: arrivalLabel,
        multiCitySegments:
          state.searchState.tripType === 'multiCity'
            ? state.searchState.multiCitySegments.map((segment, index) =>
                index === 0
                  ? {
                      ...segment,
                      departureAirport: departureLabel,
                      arrivalAirport: arrivalLabel,
                    }
                  : segment,
              )
            : state.searchState.multiCitySegments,
      },
    })),
}))

export function getFlightSearchSnap() {
  const { searchState, flightResponses, hasSearchedFlights } = useFlightSearchStore.getState()
  return { searchState, flightResponses, hasSearchedFlights }
}

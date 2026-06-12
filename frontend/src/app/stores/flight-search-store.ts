// 本文件封装状态管理逻辑。

import { create } from 'zustand'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightResultGroup, FlightSearchState } from '@/app/stores/models/flights/flightTypes'
import { createFlightSearchSegment, defaultFlightSearchState } from '@/app/stores/models/flights'

// 根据行程类型重建一份默认搜索条件，避免共享可变对象。
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

// 航班搜索仓库，保存搜索条件和搜索结果的页面级状态。
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

// 读取航班搜索页面当前快照，供非 React 场景直接消费。
export function getFlightDetailsPlannerSearchSnap() {
  const { searchState, flightResponses, flightResultGroups, hasSearchedFlights } = useFlightSearchStore.getState()
  return { searchState, flightResponses, flightResultGroups, hasSearchedFlights }
}

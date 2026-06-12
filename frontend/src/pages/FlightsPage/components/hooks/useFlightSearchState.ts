import { useFlightSearchStore } from '@/app/stores/flight-search-store'

// FlightsPage 搜索态的轻封装，只把 store 里的字段和动作暴露出来。
export function useFlightSearchState() {
  const searchState = useFlightSearchStore(state => state.searchState)
  const flightResponses = useFlightSearchStore(state => state.flightResponses)
  const flightResultGroups = useFlightSearchStore(state => state.flightResultGroups)
  const hasSearchedFlights = useFlightSearchStore(state => state.hasSearchedFlights)
  const setSearchState = useFlightSearchStore(state => state.setSearchState)
  const setFlightPlannerResponses = useFlightSearchStore(state => state.setFlightPlannerResponses)
  const setFlightResultGroups = useFlightSearchStore(state => state.setFlightResultGroups)
  const setHasSearchedFlights = useFlightSearchStore(state => state.setHasSearchedFlights)
  const updateSearchState = useFlightSearchStore(state => state.updateSearchState)
  const updateTripType = useFlightSearchStore(state => state.updateTripType)
  const updateMultiCitySegment = useFlightSearchStore(state => state.updateMultiCitySegment)
  const addMultiCitySegment = useFlightSearchStore(state => state.addMultiCitySegment)
  const removeMultiCitySegment = useFlightSearchStore(state => state.removeMultiCitySegment)

  return {
    searchState,
    flightResponses,
    flightResultGroups,
    hasSearchedFlights,
    setSearchState,
    setFlightPlannerResponses,
    setFlightResultGroups,
    setHasSearchedFlights,
    updateSearchState,
    updateTripType,
    updateMultiCitySegment,
    addMultiCitySegment,
    removeMultiCitySegment,
  }
}

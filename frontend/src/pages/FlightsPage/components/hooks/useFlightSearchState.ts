import { useFlightSearchStore } from '@/app/stores/flight-search-store'

export function useFlightSearchState() {
  const searchState = useFlightSearchStore(state => state.searchState)
  const flightResponses = useFlightSearchStore(state => state.flightResponses)
  const flightResultGroups = useFlightSearchStore(state => state.flightResultGroups)
  const hasSearchedFlights = useFlightSearchStore(state => state.hasSearchedFlights)
  const setSearchState = useFlightSearchStore(state => state.setSearchState)
  const setFlightResponses = useFlightSearchStore(state => state.setFlightResponses)
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
    setFlightResponses,
    setFlightResultGroups,
    setHasSearchedFlights,
    updateSearchState,
    updateTripType,
    updateMultiCitySegment,
    addMultiCitySegment,
    removeMultiCitySegment,
  }
}

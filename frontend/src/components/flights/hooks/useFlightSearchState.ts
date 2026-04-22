import { useFlightSearchStore } from '../stores/flightSearchStore'

export function useFlightSearchState() {
  const searchState = useFlightSearchStore(state => state.searchState)
  const flightResponses = useFlightSearchStore(state => state.flightResponses)
  const hasSearchedFlights = useFlightSearchStore(state => state.hasSearchedFlights)
  const setSearchState = useFlightSearchStore(state => state.setSearchState)
  const setFlightResponses = useFlightSearchStore(state => state.setFlightResponses)
  const setHasSearchedFlights = useFlightSearchStore(state => state.setHasSearchedFlights)
  const updateSearchState = useFlightSearchStore(state => state.updateSearchState)
  const applyQuickDatePreset = useFlightSearchStore(state => state.applyQuickDatePreset)
  const updateTripType = useFlightSearchStore(state => state.updateTripType)
  const updateMultiCitySegment = useFlightSearchStore(state => state.updateMultiCitySegment)
  const addMultiCitySegment = useFlightSearchStore(state => state.addMultiCitySegment)
  const removeMultiCitySegment = useFlightSearchStore(state => state.removeMultiCitySegment)
  const applyRouteSelection = useFlightSearchStore(state => state.applyRouteSelection)

  return {
    searchState,
    flightResponses,
    hasSearchedFlights,
    setSearchState,
    setFlightResponses,
    setHasSearchedFlights,
    updateSearchState,
    applyQuickDatePreset,
    updateTripType,
    updateMultiCitySegment,
    addMultiCitySegment,
    removeMultiCitySegment,
    applyRouteSelection,
  }
}

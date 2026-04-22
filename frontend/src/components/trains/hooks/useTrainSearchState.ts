import { useTrainSearchStore } from '../stores/trainSearchStore'

export function useTrainSearchState() {
  const trainResponses = useTrainSearchStore(state => state.trainResponses)
  const hasSearchedTrains = useTrainSearchStore(state => state.hasSearchedTrains)
  const tripType = useTrainSearchStore(state => state.tripType)
  const searchDate = useTrainSearchStore(state => state.searchDate)
  const returnDate = useTrainSearchStore(state => state.returnDate)
  const searchFromStation = useTrainSearchStore(state => state.searchFromStation)
  const searchToStation = useTrainSearchStore(state => state.searchToStation)
  const passengerCount = useTrainSearchStore(state => state.passengerCount)
  const seatPreference = useTrainSearchStore(state => state.seatPreference)
  const trainTypePreference = useTrainSearchStore(state => state.trainTypePreference)
  const selectedQuickDatePreset = useTrainSearchStore(state => state.selectedQuickDatePreset)
  const setTrainResponses = useTrainSearchStore(state => state.setTrainResponses)
  const setHasSearchedTrains = useTrainSearchStore(state => state.setHasSearchedTrains)
  const setTripType = useTrainSearchStore(state => state.setTripType)
  const setSearchDate = useTrainSearchStore(state => state.setSearchDate)
  const setReturnDate = useTrainSearchStore(state => state.setReturnDate)
  const setSearchFromStation = useTrainSearchStore(state => state.setSearchFromStation)
  const setSearchToStation = useTrainSearchStore(state => state.setSearchToStation)
  const setPassengerCount = useTrainSearchStore(state => state.setPassengerCount)
  const setSeatPreference = useTrainSearchStore(state => state.setSeatPreference)
  const setTrainTypePreference = useTrainSearchStore(state => state.setTrainTypePreference)
  const setSelectedQuickDatePreset = useTrainSearchStore(state => state.setSelectedQuickDatePreset)

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
    setTrainResponses,
    setHasSearchedTrains,
    setTripType,
    setSearchDate,
    setReturnDate,
    setSearchFromStation,
    setSearchToStation,
    setPassengerCount,
    setSeatPreference,
    setTrainTypePreference,
    setSelectedQuickDatePreset,
  }
}

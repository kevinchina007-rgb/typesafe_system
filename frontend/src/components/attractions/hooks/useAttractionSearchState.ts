import { useAttractionSearchStore } from '../stores/attractionSearchStore'

export function useAttractionSearchState() {
  const attractionResponses = useAttractionSearchStore(state => state.attractionResponses)
  const hasSearchedAttractions = useAttractionSearchStore(state => state.hasSearchedAttractions)
  const searchCity = useAttractionSearchStore(state => state.searchCity)
  const keyword = useAttractionSearchStore(state => state.keyword)
  const useDateDraft = useAttractionSearchStore(state => state.useDateDraft)
  const travelerCount = useAttractionSearchStore(state => state.travelerCount)
  const attractionType = useAttractionSearchStore(state => state.attractionType)
  const sortPreference = useAttractionSearchStore(state => state.sortPreference)
  const selectedQuickDatePreset = useAttractionSearchStore(state => state.selectedQuickDatePreset)
  const setAttractionResponses = useAttractionSearchStore(state => state.setAttractionResponses)
  const setHasSearchedAttractions = useAttractionSearchStore(state => state.setHasSearchedAttractions)
  const setSearchCity = useAttractionSearchStore(state => state.setSearchCity)
  const setKeyword = useAttractionSearchStore(state => state.setKeyword)
  const setUseDateDraft = useAttractionSearchStore(state => state.setUseDateDraft)
  const setTravelerCount = useAttractionSearchStore(state => state.setTravelerCount)
  const setAttractionType = useAttractionSearchStore(state => state.setAttractionType)
  const setSortPreference = useAttractionSearchStore(state => state.setSortPreference)
  const setSelectedQuickDatePreset = useAttractionSearchStore(state => state.setSelectedQuickDatePreset)

  return {
    attractionResponses,
    hasSearchedAttractions,
    searchCity,
    keyword,
    useDateDraft,
    travelerCount,
    attractionType,
    sortPreference,
    selectedQuickDatePreset,
    setAttractionResponses,
    setHasSearchedAttractions,
    setSearchCity,
    setKeyword,
    setUseDateDraft,
    setTravelerCount,
    setAttractionType,
    setSortPreference,
    setSelectedQuickDatePreset,
  }
}

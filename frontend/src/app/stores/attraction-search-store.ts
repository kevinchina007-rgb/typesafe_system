// 本文件封装状态管理逻辑。

import type { AttractionQuickDatePreset, AttractionSortPreference, AttractionTypePreference } from '@/app/stores/models/attraction-booking-model'
﻿import { create } from 'zustand'

import type { AttractionResponse } from '@/lib/mvp-types/index'
import { defaultAttractionSearchState } from '@/app/stores/models/attraction-booking-model'

type AttractionSearchStore = {
  attractionResponses: AttractionResponse[]
  hasSearchedAttractions: boolean
  searchCity: string
  keyword: string
  useDateDraft: string
  travelerCount: number
  attractionType: AttractionTypePreference
  sortPreference: AttractionSortPreference
  selectedQuickDatePreset: AttractionQuickDatePreset | null
  setAttractionResponses: (attractionResponses: AttractionResponse[]) => void
  setHasSearchedAttractions: (hasSearchedAttractions: boolean) => void
  setSearchCity: (searchCity: string) => void
  setKeyword: (keyword: string) => void
  setUseDateDraft: (useDateDraft: string) => void
  setTravelerCount: (travelerCount: number) => void
  setAttractionType: (attractionType: AttractionTypePreference) => void
  setSortPreference: (sortPreference: AttractionSortPreference) => void
  setSelectedQuickDatePreset: (selectedQuickDatePreset: AttractionQuickDatePreset | null) => void
}

export const useAttractionSearchStore = create<AttractionSearchStore>(set => ({
  attractionResponses: [],
  hasSearchedAttractions: false,
  searchCity: defaultAttractionSearchState.city,
  keyword: defaultAttractionSearchState.keyword,
  useDateDraft: defaultAttractionSearchState.useDate,
  travelerCount: defaultAttractionSearchState.travelerCount,
  attractionType: defaultAttractionSearchState.attractionType,
  sortPreference: defaultAttractionSearchState.sortPreference,
  selectedQuickDatePreset: defaultAttractionSearchState.selectedQuickDatePreset,
  setAttractionResponses: attractionResponses => set({ attractionResponses }),
  setHasSearchedAttractions: hasSearchedAttractions => set({ hasSearchedAttractions }),
  setSearchCity: searchCity => set({ searchCity }),
  setKeyword: keyword => set({ keyword }),
  setUseDateDraft: useDateDraft => set({ useDateDraft }),
  setTravelerCount: travelerCount => set({ travelerCount }),
  setAttractionType: attractionType => set({ attractionType }),
  setSortPreference: sortPreference => set({ sortPreference }),
  setSelectedQuickDatePreset: selectedQuickDatePreset => set({ selectedQuickDatePreset }),
}))

export function getAttractionSearchSnap() {
  const {
    attractionResponses,
    hasSearchedAttractions,
    searchCity,
    keyword,
    useDateDraft,
    travelerCount,
    attractionType,
    sortPreference,
    selectedQuickDatePreset,
  } = useAttractionSearchStore.getState()
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
  }
}

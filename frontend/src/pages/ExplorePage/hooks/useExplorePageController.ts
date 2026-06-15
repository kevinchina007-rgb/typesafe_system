// 本文件定义 ExplorePage 页面的状态控制逻辑，负责条件维护、请求触发和动作调度。

import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { ExploreSearchResultResponse, SearchSuggestionResponse } from '@/lib/mvp-types/index'
import type { ExplorePageController, ExplorePageProps, ExploreSearchType } from '../objects'
import { groupExploreResults } from '../functions'

export function useExplorePageController(_: ExplorePageProps): ExplorePageController {
  const [searchType, setSearchType] = useState<ExploreSearchType>('all')
  const [searchDraft, setSearchDraft] = useState('')
  const [searchText, setSearchText] = useState('')
  const [isSearching, setIsSearching] = useState(false)
  const [results, setResults] = useState<ExploreSearchResultResponse[]>([])
  const [suggestions, setSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false)

  useEffect(() => {
    let cancelled = false
    const normalizedDraft = searchDraft.trim()
    if (normalizedDraft.length < 2) {
      setSuggestions([])
      return () => {
        cancelled = true
      }
    }

    const timeoutId = window.setTimeout(async () => {
      try {
        setIsLoadingSuggestions(true)
        const response = await travelMvpApiClient.listExploreSuggestions({ q: normalizedDraft })
        if (!cancelled) {
          setSuggestions(response.suggestions)
        }
      } catch {
        if (!cancelled) {
          setSuggestions([])
        }
      } finally {
        if (!cancelled) {
          setIsLoadingSuggestions(false)
        }
      }
    }, 180)

    return () => {
      cancelled = true
      window.clearTimeout(timeoutId)
    }
  }, [searchDraft])

  useEffect(() => {
    let cancelled = false
    const normalizedSearchText = searchText.trim()
    if (!normalizedSearchText) {
      setResults([])
      return () => {
        cancelled = true
      }
    }

    void (async () => {
      try {
        setIsSearching(true)
        const response = await travelMvpApiClient.searchExplore({
          q: normalizedSearchText,
          resourceType: searchType === 'all' ? undefined : searchType,
        })
        if (!cancelled) {
          setResults(response.results)
        }
      } catch {
        if (!cancelled) {
          setResults([])
        }
      } finally {
        if (!cancelled) {
          setIsSearching(false)
        }
      }
    })()

    return () => {
      cancelled = true
    }
  }, [searchText, searchType])

  const groupedResults = useMemo(() => groupExploreResults(results), [results])

  return {
    searchType,
    searchDraft,
    searchText,
    isSearching,
    results,
    groupedResults,
    suggestions,
    isLoadingSuggestions,
    setSearchType,
    setSearchDraft,
    runSearch: () => setSearchText(searchDraft.trim()),
    applySuggestion: suggestion => {
      setSearchDraft(suggestion.value)
      setSearchText(suggestion.value)
      setSearchType(suggestion.resourceType as ExploreSearchType)
    },
  }
}

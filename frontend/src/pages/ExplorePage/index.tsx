import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppViewKey, ExploreSearchResultResponse, SearchSuggestionResponse } from '@/lib/mvp-types/index'

type ExploreSearchType = 'all' | 'flight' | 'hotel' | 'train' | 'attraction' | 'blog'

type ExplorePageProps = {
  translate: (translationKey: string) => string
  onOpenView: (viewKey: AppViewKey) => void
}

const searchableTabs: ExploreSearchType[] = ['all', 'flight', 'hotel', 'train', 'attraction', 'blog']

function targetViewFromResult(resourceType: string): AppViewKey {
  switch (resourceType) {
    case 'flight':
      return 'flights'
    case 'hotel':
      return 'hotels'
    case 'train':
      return 'trains'
    case 'attraction':
      return 'attractions'
    case 'blog':
      return 'blog'
    default:
      return 'explore'
  }
}

export function ExplorePage({ translate, onOpenView }: ExplorePageProps) {
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
        const response = await travelMvpApiClient.listExploreSuggestions(normalizedDraft)
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
          type: searchType,
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

  const groupedResults = useMemo(() => {
    return results.reduce<Record<string, ExploreSearchResultResponse[]>>((currentGroups, result) => {
      const groupKey = result.resourceType
      currentGroups[groupKey] = [...(currentGroups[groupKey] ?? []), result]
      return currentGroups
    }, {})
  }, [results])

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('nav.explore')}</p>
            <h2>{translate('explore.title')}</h2>
          </div>
        </div>

        <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('explore.description')}</p>

        <div className="flex flex-wrap items-center gap-3">
          {searchableTabs.map(tabValue => (
            <button
              key={tabValue}
              type="button"
              className={searchType === tabValue ? '' : 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'}
              onClick={() => setSearchType(tabValue)}
            >
              {translate(`search.type.${tabValue}`)}
            </button>
          ))}
        </div>

        <div className="grid gap-4">
          <label>
            {translate('search.global.label')}
            <input
              value={searchDraft}
              onChange={event => setSearchDraft(event.target.value)}
            />
          </label>
          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isSearching} onClick={() => setSearchText(searchDraft.trim())}>
            {translate('search.confirm')}
          </button>
        </div>

        {suggestions.length > 0 || isLoadingSuggestions ? (
          <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <p className="text-sm font-bold text-slate-500">{translate('search.suggestions')}</p>
            {isLoadingSuggestions && suggestions.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('search.loading')}</p> : null}
            <ul className="grid gap-3">
              {suggestions.map(suggestion => (
                <li key={`${suggestion.resourceType}:${suggestion.value}`}>
                  <button
                    type="button"
                    className="inline-flex items-center justify-center text-sm font-bold text-sky-600 underline-offset-4 hover:underline"
                    onClick={() => {
                      setSearchDraft(suggestion.value)
                      setSearchText(suggestion.value)
                      setSearchType(suggestion.resourceType as ExploreSearchType)
                    }}
                  >
                    <strong>{suggestion.title}</strong>
                  </button>
                  <p>{suggestion.subtitle}</p>
                </li>
              ))}
            </ul>
          </div>
        ) : null}

        {searchText.trim().length === 0 ? (
          <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <p className="text-sm leading-6 text-slate-500">{translate('search.emptyHint')}</p>
          </div>
        ) : null}

        {searchText.trim().length > 0 && results.length === 0 && !isSearching ? (
          <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <p className="text-sm leading-6 text-slate-500">{translate('search.emptyResults')}</p>
          </div>
        ) : null}

        {Object.entries(groupedResults).map(([resourceType, groupedItems]) => (
          <div key={resourceType} className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
            <div className="text-lg font-bold text-slate-950">
              <div>
                <p className="text-sm font-bold text-slate-500">{translate(`search.type.${resourceType}`)}</p>
                <h3>{translate('search.resultGroupTitle')}</h3>
              </div>
            </div>

            <ul className="grid gap-3">
              {groupedItems.map(result => (
                <li key={`${result.resourceType}:${result.resourceId}`}>
                  <div className="search-result-card">
                    <div>
                      <strong>{result.title}</strong>
                      <p>{result.summary}</p>
                      <p>{result.metaLabel}</p>
                    </div>
                    <div className="flex flex-wrap items-center gap-3">
                      <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" onClick={() => onOpenView(targetViewFromResult(result.resourceType))}>
                        {translate('search.openResult')}
                      </button>
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        ))}
      </div>
    </section>
  )
}

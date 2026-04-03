import { useEffect, useMemo, useState } from 'react'

import { travelMvpApiClient } from '../../lib/api-client'
import type { AppViewKey, ExploreSearchResultResponse, SearchSuggestionResponse } from '../../lib/mvp-types'

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
    <section className="page-card">
      <div className="panel-card">
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('nav.explore')}</p>
            <h2>{translate('explore.title')}</h2>
          </div>
        </div>

        <p className="hero-copy">{translate('explore.description')}</p>

        <div className="manager-task-actions">
          {searchableTabs.map(tabValue => (
            <button
              key={tabValue}
              type="button"
              className={searchType === tabValue ? '' : 'secondary-button'}
              onClick={() => setSearchType(tabValue)}
            >
              {translate(`search.type.${tabValue}`)}
            </button>
          ))}
        </div>

        <div className="stack-form">
          <label>
            {translate('search.global.label')}
            <input
              value={searchDraft}
              onChange={event => setSearchDraft(event.target.value)}
              placeholder={translate('search.global.placeholder')}
            />
          </label>
          <button type="button" disabled={isSearching} onClick={() => setSearchText(searchDraft.trim())}>
            {translate('search.confirm')}
          </button>
        </div>

        {suggestions.length > 0 || isLoadingSuggestions ? (
          <div className="list-surface">
            <p className="eyebrow-label">{translate('search.suggestions')}</p>
            {isLoadingSuggestions && suggestions.length === 0 ? <p className="empty-state">{translate('search.loading')}</p> : null}
            <ul className="entity-list">
              {suggestions.map(suggestion => (
                <li key={`${suggestion.resourceType}:${suggestion.value}`}>
                  <button
                    type="button"
                    className="tour-group-link-button"
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
          <div className="list-surface">
            <p className="empty-state">{translate('search.emptyHint')}</p>
          </div>
        ) : null}

        {searchText.trim().length > 0 && results.length === 0 && !isSearching ? (
          <div className="list-surface">
            <p className="empty-state">{translate('search.emptyResults')}</p>
          </div>
        ) : null}

        {Object.entries(groupedResults).map(([resourceType, groupedItems]) => (
          <div key={resourceType} className="list-surface">
            <div className="panel-heading">
              <div>
                <p className="eyebrow-label">{translate(`search.type.${resourceType}`)}</p>
                <h3>{translate('search.resultGroupTitle')}</h3>
              </div>
            </div>

            <ul className="entity-list">
              {groupedItems.map(result => (
                <li key={`${result.resourceType}:${result.resourceId}`}>
                  <div className="search-result-card">
                    <div>
                      <strong>{result.title}</strong>
                      <p>{result.summary}</p>
                      <p>{result.metaLabel}</p>
                    </div>
                    <div className="action-row">
                      <button type="button" onClick={() => onOpenView(targetViewFromResult(result.resourceType))}>
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

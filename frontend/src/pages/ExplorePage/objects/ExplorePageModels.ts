import type { AppViewKey, ExploreSearchResultResponse, SearchSuggestionResponse } from '@/lib/mvp-types/index'

export type ExploreSearchType = 'all' | 'flight' | 'hotel' | 'train' | 'attraction' | 'blog'

export type ExplorePageProps = {
  translate: (translationKey: string) => string
  onOpenView: (viewKey: AppViewKey) => void
}

export type ExplorePageController = {
  searchType: ExploreSearchType
  searchDraft: string
  searchText: string
  isSearching: boolean
  results: ExploreSearchResultResponse[]
  groupedResults: Record<string, ExploreSearchResultResponse[]>
  suggestions: SearchSuggestionResponse[]
  isLoadingSuggestions: boolean
  setSearchType: (searchType: ExploreSearchType) => void
  setSearchDraft: (searchDraft: string) => void
  runSearch: () => void
  applySuggestion: (suggestion: SearchSuggestionResponse) => void
}

export type ExplorePageRegionKey = 'header' | 'search' | 'suggestions' | 'results'

export type ExplorePageRegion = {
  key: ExplorePageRegionKey
  title: string
  description: string
}

export const EXPLORE_PAGE_REGIONS: ExplorePageRegion[] = [
  {
    key: 'header',
    title: '页面标题区',
    description: '展示探索页标题和说明。',
  },
  {
    key: 'search',
    title: '搜索区',
    description: '承载类型筛选、关键词输入和搜索按钮。',
  },
  {
    key: 'suggestions',
    title: '建议区',
    description: '展示联想建议结果。',
  },
  {
    key: 'results',
    title: '结果区',
    description: '按资源类型分组展示搜索结果。',
  },
]

export const EXPLORE_SEARCH_TYPES: ExploreSearchType[] = ['all', 'flight', 'hotel', 'train', 'attraction', 'blog']

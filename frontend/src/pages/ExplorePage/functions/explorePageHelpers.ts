import type { AppViewKey, ExploreSearchResultResponse } from '@/lib/mvp-types/index'

export function targetViewFromResult(resourceType: string): AppViewKey {
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

export function groupExploreResults(results: ExploreSearchResultResponse[]) {
  return results.reduce<Record<string, ExploreSearchResultResponse[]>>((currentGroups, result) => {
    const groupKey = result.resourceType
    currentGroups[groupKey] = [...(currentGroups[groupKey] ?? []), result]
    return currentGroups
  }, {})
}

import type { AppLanguage, ReviewResponse } from '@/lib/mvp-types/index'
import { localizeReviewResourceType } from '@/lib/presenters/content-presenter'

export function getReviewsPageResourceTypes(reviews: ReviewResponse[]) {
  return ['All', ...new Set(reviews.map(review => review.resourceType))]
}

export function getVisibleReviews(reviews: ReviewResponse[], activeResourceType: string, searchText: string) {
  const normalizedSearchText = searchText.trim().toLowerCase()
  return reviews.filter(review => {
    const matchesType = activeResourceType === 'All' || review.resourceType === activeResourceType
    const matchesSearch =
      normalizedSearchText.length === 0 ||
      review.resourceSummaryTitle.toLowerCase().includes(normalizedSearchText) ||
      review.title.toLowerCase().includes(normalizedSearchText) ||
      review.content.toLowerCase().includes(normalizedSearchText)
    return matchesType && matchesSearch
  })
}

export function getReviewTabLabel(resourceType: string, currentLanguage: AppLanguage, translate: (translationKey: string) => string) {
  return resourceType === 'All' ? translate('reviews.filterAll') : localizeReviewResourceType(resourceType, currentLanguage)
}


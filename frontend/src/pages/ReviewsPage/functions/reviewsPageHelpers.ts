// 本文件定义 ReviewsPage 的辅助函数，负责整理点评列表、评分信息和举报状态。

import type { AppLanguage, ReviewPlannerResponse } from '@/lib/mvp-types/index'
import { localizeReviewResourceType } from '@/lib/presenters/content-presenter'

export function getReviewsPageResourceTypes(reviews: ReviewPlannerResponse[]) {
  return ['All', ...new Set(reviews.map(review => review.resourceType))]
}

export function getVisibleReviews(reviews: ReviewPlannerResponse[], activeResourceType: string, searchText: string) {
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


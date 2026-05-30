import type { AppLanguage, ContentImageResponse, ReviewResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

export type ReviewsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export type ReviewsPageController = {
  currentLanguage: AppLanguage
  isBusy: boolean
  signedInUser: UserResponse | null
  reviews: ReviewResponse[]
  activeResourceType: string
  searchDraft: string
  searchText: string
  editingReview: ReviewResponse | null
  visibleReviews: ReviewResponse[]
  resourceTypes: string[]
  translate: (translationKey: string) => string
  setSearchDraft: (value: string) => void
  setSearchText: (value: string) => void
  setActiveResourceType: (value: string) => void
  setEditingReview: (value: ReviewResponse | null) => void
  refreshReviews: () => Promise<void>
  handleUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  handleUpdateReview: (reviewId: string, payload: { rating: number; title: string; content: string; images: ContentImageResponse[] }) => Promise<ReviewResponse>
  handleDeleteReview: (reviewId: string) => Promise<void>
  handleSubmitReviewEdit: (payload: { rating: number; title: string; content: string; images: ContentImageResponse[] }) => Promise<void>
}

export type ReviewsPageRegion = 'hero' | 'list' | 'dialog'

export const REVIEWS_PAGE_REGIONS: ReviewsPageRegion[] = ['hero', 'list', 'dialog']


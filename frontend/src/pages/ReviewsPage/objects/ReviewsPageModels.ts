import type { AppLanguage, ContentImageResponse, ReviewResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

// ReviewsPage 的页面参数，只传入语言、登录用户和提示回调。
export type ReviewsPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

// ReviewsPage 控制器暴露给视图层的状态集合。
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

// ReviewsPage 内部使用的区域标识，方便分块渲染。
export type ReviewsPageRegion = 'hero' | 'list' | 'dialog'

// 页面中会渲染的固定区域列表。
export const REVIEWS_PAGE_REGIONS: ReviewsPageRegion[] = ['hero', 'list', 'dialog']

import type { AppLanguage, ContentImagePlannerResponse, ReviewPlannerResponse, UserResponse } from '@/lib/mvp-types/index'
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
  reviews: ReviewPlannerResponse[]
  activeResourceType: string
  searchDraft: string
  searchText: string
  editingReview: ReviewPlannerResponse | null
  visibleReviews: ReviewPlannerResponse[]
  resourceTypes: string[]
  translate: (translationKey: string) => string
  setSearchDraft: (value: string) => void
  setSearchText: (value: string) => void
  setActiveResourceType: (value: string) => void
  setEditingReview: (value: ReviewPlannerResponse | null) => void
  refreshReviews: () => Promise<void>
  handleUploadImage: (imageFile: File) => Promise<ContentImagePlannerResponse>
  handleUpdateReview: (reviewId: string, payload: { rating: number; title: string; content: string; images: ContentImagePlannerResponse[] }) => Promise<ReviewPlannerResponse>
  handleDeleteReview: (reviewId: string) => Promise<void>
  handleSubmitReviewEdit: (payload: { rating: number; title: string; content: string; images: ContentImagePlannerResponse[] }) => Promise<void>
}

// ReviewsPage 内部使用的区域标识，方便分块渲染。
export type ReviewsPageRegion = 'hero' | 'list' | 'dialog'

// 页面中会渲染的固定区域列表。
export const REVIEWS_PAGE_REGIONS: ReviewsPageRegion[] = ['hero', 'list', 'dialog']

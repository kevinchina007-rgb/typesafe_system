import type { AppLanguage, UserResponse } from '@/lib/mvp-types/index'
import type { FeedbackThread } from '@/microservices/content/objects/FeedbackThread'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

export type CancellationOrderOption = {
  orderId: string
  title: string
  category: OrderCategory
}

export type CustomerFeedbackPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

export type CustomerFeedbackPageController = {
  cancellationOrders: CancellationOrderOption[]
  orderedThreads: FeedbackThread[]
}

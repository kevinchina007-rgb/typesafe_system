import type { AppLanguage, UserResponse } from '@/lib/mvp-types/index'
import type { FeedbackThread } from '@/microservices/feedback/objects/FeedbackThread'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { OrderCategory } from '@/pages/BookingsPage/objects'

// 反馈页里可选的订单项，供用户发起取消申请时挑选。
export type CancellationOrderOption = {
  orderId: string
  title: string
  detailLabel: string
  category: OrderCategory
}

// 客服反馈页的输入参数，只保留页面渲染和提示所需的数据。
export type CustomerFeedbackPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onShowNotice: PageNoticeHandler
}

// 客服反馈页控制器暴露给视图层的状态集合。
export type CustomerFeedbackPageController = {
  cancellationOrders: CancellationOrderOption[]
  orderedThreads: FeedbackThread[]
}

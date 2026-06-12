// 本文件定义共享页面组件或工具，负责页面间复用逻辑。

import type { AppLanguage, ReviewResponse } from '@/lib/mvp-types/index'
import { formatReviewMeta, localizeReviewResourceType, localizeReviewStatus, summarizeRating } from '@/lib/presenters/content-presenter'
import { ContentImageGallery } from '@/pages/shared/content/ContentImageGallery'

// 评价列表弹窗的输入参数。
type ReviewListDialogProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isOpen: boolean
  title: string
  reviews: ReviewResponse[]
  translate: (translationKey: string) => string
  onClose: () => void
}

// 评价列表弹窗，负责展示摘要、图片和每条评价的状态。
export function ReviewListDialog({
  currentLanguage,
  isBusy,
  isOpen,
  title,
  reviews,
  translate,
  onClose,
}: ReviewListDialogProps) {
  if (!isOpen) {
    return null
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6" onClick={onClose}>
      <section className="grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20" onClick={event => event.stopPropagation()}>
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('reviews.dialogEyebrow')}</p>
            <h3>{title}</h3>
          </div>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onClose}>
            关闭
          </button>
        </div>

        {reviews.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('reviews.empty')}</p> : null}

        {/* 列表区域按条目展示评价内容、图片和状态。 */}
        {reviews.length > 0 ? (
          <ul className="grid gap-3">
            {reviews.map(review => (
              <li key={review.reviewId}>
                <div>
                  <strong>{review.title}</strong>
                  <p>{`${review.authorDisplayName} · ${summarizeRating(review.rating)}`}</p>
                  <p>{review.content}</p>
                  <ContentImageGallery images={review.images} />
                  <p>{`${localizeReviewResourceType(review.resourceType, currentLanguage)} · ${review.resourceSummarySubtitle}`}</p>
                  <p>{formatReviewMeta(review, translate('booking.notYet'))}</p>
                </div>
                <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{localizeReviewStatus(review.status, currentLanguage)}</span>
              </li>
            ))}
          </ul>
        ) : null}
      </section>
    </div>
  )
}

// 本文件定义 ReviewsPage 页面的卡片组件，负责单条数据摘要展示。

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { ContentImageGallery } from '@/pages/shared/content/ContentImageGallery'
import { formatReviewMeta, localizeReviewResourceType, localizeReviewStatus, summarizeRating } from '@/lib/presenters/content-presenter'
import type { ReviewPlannerResponse } from '@/lib/mvp-types/index'

type ReviewCardProps = {
  review: ReviewPlannerResponse
  currentLanguage: import('@/lib/mvp-types/index').AppLanguage
  isBusy: boolean
  translate: (translationKey: string) => string
  onEdit: () => void
  onDelete: () => void
}

export function ReviewCard({ review, currentLanguage, isBusy, translate, onEdit, onDelete }: ReviewCardProps) {
  return (
    <article className="grid gap-5 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="grid gap-1">
        <div>
          <p className="text-sm font-bold text-slate-500">{localizeReviewResourceType(review.resourceType, currentLanguage)}</p>
          <h3 className="m-0 text-xl font-bold text-slate-950">{review.resourceSummaryTitle}</h3>
          <p className="text-sm text-slate-500">{review.resourceSummarySubtitle}</p>
        </div>
        <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
          {localizeReviewStatus(review.status, currentLanguage)}
        </span>
      </div>

      <div className="flex items-center gap-2">
        <strong>{review.title}</strong>
        <span>{summarizeRating(review.rating)}</span>
      </div>

      <p className="text-sm leading-6 text-slate-600">{review.content}</p>
      <ContentImageGallery images={review.images} />

      <div className="text-sm text-slate-500">
        <span className="inline-flex items-center gap-2">
          <BackendAssetImage
            className="h-8 w-8 object-cover"
            assetUrl={review.authorAvatarUrl}
            alt={review.authorDisplayName}
            fallbackContent={review.authorDisplayName.slice(0, 1).toUpperCase()}
          />
          <span>{review.authorDisplayName}</span>
        </span>
        <span>{formatReviewMeta(review, translate('booking.notYet'))}</span>
      </div>

      <div className="flex flex-wrap items-center gap-3">
        {review.canEdit ? (
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onEdit}>
            {translate('reviews.edit')}
          </button>
        ) : null}
        {review.canDelete ? (
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onDelete}>
            {translate('reviews.delete')}
          </button>
        ) : null}
      </div>
    </article>
  )
}


// 本文件定义 ReviewsPage 页面的页面分区，负责某一块独立内容的展示。

import type { AppLanguage, ReviewResponse } from '@/lib/mvp-types/index'
import { ReviewCard } from './ReviewCard'

type ReviewListSectionProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  reviews: ReviewResponse[]
  signedInUser: unknown | null
  translate: (translationKey: string) => string
  onEditReview: (review: ReviewResponse) => void
  onDeleteReview: (reviewId: string) => void
}

export function ReviewListSection({
  currentLanguage,
  isBusy,
  reviews,
  signedInUser,
  translate,
  onEditReview,
  onDeleteReview,
}: ReviewListSectionProps) {
  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('community.listEyebrow')}</p>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('community.reviewListTitle')}</h2>
        </div>
      </div>

      {signedInUser && reviews.length === 0 ? (
        <div className="grid place-items-center gap-3 border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
          <p className="text-sm leading-6 text-slate-500">{translate('reviews.empty')}</p>
        </div>
      ) : null}

      {reviews.length > 0 ? (
        <div className="grid gap-4 lg:grid-cols-2">
          {reviews.map(review => (
            <ReviewCard
              key={review.reviewId}
              review={review}
              currentLanguage={currentLanguage}
              isBusy={isBusy}
              translate={translate}
              onEdit={() => onEditReview(review)}
              onDelete={() => onDeleteReview(review.reviewId)}
            />
          ))}
        </div>
      ) : null}
    </section>
  )
}


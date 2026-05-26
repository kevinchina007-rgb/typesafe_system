import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, ContentImageResponse, ReviewResponse, UserResponse } from '@/lib/mvp-types/index'
import { formatReviewMeta, localizeReviewResourceType, localizeReviewStatus, summarizeRating } from '@/lib/presenters/content-presenter'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { ContentImageGallery } from '@/pages/shared/content/ContentImageGallery'
import { ReviewComposerDialog } from '@/pages/shared/content/ReviewComposerDialog'
import { CommunityHero } from '@/pages/shared/community/CommunityHero'

type MyReviewsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onListMyReviews: () => Promise<ReviewResponse[]>
  onUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  onUpdateReview: (reviewId: string, payload: { rating: number; title: string; content: string; images: ContentImageResponse[] }) => Promise<ReviewResponse>
  onDeleteReview: (reviewId: string) => Promise<void>
}

export function MyReviewsPanel({
  currentLanguage,
  isBusy,
  signedInUser,
  translate,
  onListMyReviews,
  onUploadImage,
  onUpdateReview,
  onDeleteReview,
}: MyReviewsPanelProps) {
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [activeResourceType, setActiveResourceType] = useState<string>('All')
  const [searchText, setSearchText] = useState('')
  const [searchDraft, setSearchDraft] = useState('')
  const [editingReview, setEditingReview] = useState<ReviewResponse | null>(null)

  useEffect(() => {
    if (!signedInUser) {
      setReviews([])
      return
    }
    void onListMyReviews().then(setReviews)
  }, [signedInUser?.userId, onListMyReviews])

  const resourceTypes = useMemo(() => ['All', ...new Set(reviews.map(review => review.resourceType))], [reviews])
  const visibleReviews = reviews.filter(review => {
    const matchesType = activeResourceType === 'All' || review.resourceType === activeResourceType
    const normalizedSearchText = searchText.trim().toLowerCase()
    const matchesSearch =
      normalizedSearchText.length === 0 ||
      review.resourceSummaryTitle.toLowerCase().includes(normalizedSearchText) ||
      review.title.toLowerCase().includes(normalizedSearchText) ||
      review.content.toLowerCase().includes(normalizedSearchText)
    return matchesType && matchesSearch
  })

  return (
    <section className="grid gap-5 grid gap-5">
      <CommunityHero
        eyebrow={translate('community.reviewEyebrow')}
        title={translate('reviews.title')}
        searchValue={searchDraft}
        searchButtonLabel={translate('search.confirm')}
        tabs={resourceTypes.map(resourceType => ({
          key: resourceType,
          label: resourceType === 'All' ? translate('reviews.filterAll') : localizeReviewResourceType(resourceType, currentLanguage),
        }))}
        activeTab={activeResourceType}
        secondaryActionLabel={translate('reviews.refresh')}
        isBusy={isBusy}
        onSearchChange={setSearchDraft}
        onSearchSubmit={() => setSearchText(searchDraft)}
        onSelectTab={setActiveResourceType}
        onSecondaryAction={() => void onListMyReviews().then(setReviews)}
      />

      {!signedInUser ? <p className="text-sm leading-6 text-slate-500">{translate('reviews.guest')}</p> : null}

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('community.listEyebrow')}</p>
            <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('community.reviewListTitle')}</h2>
          </div>
        </div>

        {signedInUser && visibleReviews.length === 0 ? (
          <div className="grid place-items-center gap-3 border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
            <p className="text-sm leading-6 text-slate-500">{translate('reviews.empty')}</p>
          </div>
        ) : null}

        {visibleReviews.length > 0 ? (
          <div className="grid gap-4 lg:grid-cols-2">
            {visibleReviews.map(review => (
              <article key={review.reviewId} className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-3">
                <div className="grid gap-1">
                  <div>
                    <p className="text-sm font-bold text-slate-500">{localizeReviewResourceType(review.resourceType, currentLanguage)}</p>
                    <h3 className="m-0 text-xl font-bold text-slate-950">{review.resourceSummaryTitle}</h3>
                    <p className="text-sm text-slate-500">{review.resourceSummarySubtitle}</p>
                  </div>
                  <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{localizeReviewStatus(review.status, currentLanguage)}</span>
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
                    <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => setEditingReview(review)}>
                      {translate('reviews.edit')}
                    </button>
                  ) : null}
                  {review.canDelete ? (
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      disabled={isBusy}
                      onClick={() =>
                        void onDeleteReview(review.reviewId).then(async () => {
                          const nextReviews = await onListMyReviews()
                          setReviews(nextReviews)
                        })
                      }
                    >
                      {translate('reviews.delete')}
                    </button>
                  ) : null}
                </div>
              </article>
            ))}
          </div>
        ) : null}
      </section>

      <ReviewComposerDialog
        isOpen={editingReview !== null}
        isBusy={isBusy}
        eligibility={null}
        mode="edit"
        initialValue={
          editingReview
            ? {
                rating: editingReview.rating,
                title: editingReview.title,
                content: editingReview.content,
                images: editingReview.images,
              }
            : null
        }
        title={editingReview?.resourceSummaryTitle ?? ''}
        translate={translate}
        onClose={() => setEditingReview(null)}
        onUploadImage={onUploadImage}
        onSubmit={async payload => {
          if (!editingReview) {
            return
          }
          await onUpdateReview(editingReview.reviewId, payload)
          const nextReviews = await onListMyReviews()
          setReviews(nextReviews)
          setEditingReview(null)
        }}
      />
    </section>
  )
}

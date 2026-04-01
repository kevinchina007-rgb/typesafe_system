import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, ContentImageResponse, ReviewResponse, UserResponse } from '../lib/mvp-types'
import { formatReviewMeta, localizeReviewResourceType, localizeReviewStatus, summarizeRating } from '../lib/content-presenter'
import { ContentImageGallery } from './ContentImageGallery'
import { ReviewComposerDialog } from './ReviewComposerDialog'

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
  const [editingReview, setEditingReview] = useState<ReviewResponse | null>(null)

  useEffect(() => {
    if (!signedInUser) {
      setReviews([])
      return
    }
    void onListMyReviews().then(setReviews)
  }, [signedInUser?.userId])

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
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.reviews')}</p>
          <h2>{translate('reviews.title')}</h2>
        </div>
        <button type="button" className="secondary-button" disabled={isBusy || !signedInUser} onClick={() => void onListMyReviews().then(setReviews)}>
          {translate('reviews.refresh')}
        </button>
      </div>

      <p className="hero-copy">{translate('reviews.description')}</p>

      {!signedInUser ? <p className="empty-state">{translate('reviews.guest')}</p> : null}

      {signedInUser ? (
        <>
          <label>
            {translate('blog.search')}
            <input value={searchText} onChange={event => setSearchText(event.target.value)} placeholder={translate('reviews.searchHint')} />
          </label>

          <div className="manager-task-actions">
            {resourceTypes.map(resourceType => (
              <button
                key={resourceType}
                type="button"
                className={activeResourceType === resourceType ? '' : 'secondary-button'}
                onClick={() => setActiveResourceType(resourceType)}
              >
                {resourceType === 'All' ? translate('reviews.filterAll') : localizeReviewResourceType(resourceType, currentLanguage)}
              </button>
            ))}
          </div>
        </>
      ) : null}

      {signedInUser && visibleReviews.length === 0 ? <p className="empty-state">{translate('reviews.empty')}</p> : null}

      {visibleReviews.length > 0 ? (
        <ul className="entity-list">
          {visibleReviews.map(review => (
            <li key={review.reviewId}>
              <div>
                <strong>{review.resourceSummaryTitle}</strong>
                <p>{review.resourceSummarySubtitle}</p>
                <p>{`${localizeReviewResourceType(review.resourceType, currentLanguage)} · ${summarizeRating(review.rating)}`}</p>
                <p>{review.title}</p>
                <p>{review.content}</p>
                <ContentImageGallery images={review.images} />
                <p>{formatReviewMeta(review, translate('booking.notYet'))}</p>
              </div>
              <div className="compact-action-block">
                <span className="tag-chip">{localizeReviewStatus(review.status, currentLanguage)}</span>
                {review.canEdit ? (
                  <button type="button" className="secondary-button" disabled={isBusy} onClick={() => setEditingReview(review)}>
                    {translate('reviews.edit')}
                  </button>
                ) : null}
                {review.canDelete ? (
                  <button
                    type="button"
                    className="secondary-button"
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
            </li>
          ))}
        </ul>
      ) : null}

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

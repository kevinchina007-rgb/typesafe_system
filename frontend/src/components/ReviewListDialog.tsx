import type { AppLanguage, ReviewResponse } from '../lib/mvp-types'
import { formatReviewMeta, localizeReviewResourceType, localizeReviewStatus, summarizeRating } from '../lib/content-presenter'
import { ContentImageGallery } from './ContentImageGallery'

type ReviewListDialogProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isOpen: boolean
  title: string
  reviews: ReviewResponse[]
  translate: (translationKey: string) => string
  onClose: () => void
}

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
    <div className="dialog-backdrop" onClick={onClose}>
      <section className="dialog-card" onClick={event => event.stopPropagation()}>
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('reviews.dialogEyebrow')}</p>
            <h3>{title}</h3>
          </div>
          <button type="button" className="secondary-button modal-close-button" disabled={isBusy} onClick={onClose}>
            ×
          </button>
        </div>

        {reviews.length === 0 ? <p className="empty-state">{translate('reviews.empty')}</p> : null}

        {reviews.length > 0 ? (
          <ul className="entity-list">
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
                <span className="tag-chip">{localizeReviewStatus(review.status, currentLanguage)}</span>
              </li>
            ))}
          </ul>
        ) : null}
      </section>
    </div>
  )
}

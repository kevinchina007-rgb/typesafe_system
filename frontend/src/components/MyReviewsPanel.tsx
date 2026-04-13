import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, ContentImageResponse, ReviewResponse, UserResponse } from '../lib/mvp-types'
import { formatReviewMeta, localizeReviewResourceType, localizeReviewStatus, summarizeRating } from '../lib/content-presenter'
import { BackendAssetImage } from './BackendAssetImage'
import { ContentImageGallery } from './ContentImageGallery'
import { ReviewComposerDialog } from './ReviewComposerDialog'
import { CommunityHero } from './community/CommunityHero'

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
    <section className="page-stack community-page-stack">
      <CommunityHero
        eyebrow={translate('community.reviewEyebrow')}
        title={translate('reviews.title')}
        searchValue={searchDraft}
        searchPlaceholder={translate('reviews.searchHint')}
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

      {!signedInUser ? <p className="empty-state">{translate('reviews.guest')}</p> : null}

      <section className="page-card community-list-section">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('community.listEyebrow')}</p>
            <h2 className="section-title">{translate('community.reviewListTitle')}</h2>
          </div>
        </div>

        {signedInUser && visibleReviews.length === 0 ? (
          <div className="empty-state-panel">
            <p className="empty-state">{translate('reviews.empty')}</p>
          </div>
        ) : null}

        {visibleReviews.length > 0 ? (
          <div className="community-review-grid">
            {visibleReviews.map(review => (
              <article key={review.reviewId} className="page-card community-review-card">
                <div className="community-review-head">
                  <div>
                    <p className="eyebrow-label">{localizeReviewResourceType(review.resourceType, currentLanguage)}</p>
                    <h3 className="community-review-title">{review.resourceSummaryTitle}</h3>
                    <p className="community-review-subtitle">{review.resourceSummarySubtitle}</p>
                  </div>
                  <span className="tag-chip">{localizeReviewStatus(review.status, currentLanguage)}</span>
                </div>

                <div className="community-review-rating-row">
                  <strong>{review.title}</strong>
                  <span>{summarizeRating(review.rating)}</span>
                </div>

                <p className="community-review-content">{review.content}</p>
                <ContentImageGallery images={review.images} />

                <div className="community-article-meta">
                  <span className="community-inline-identity">
                    <BackendAssetImage
                      className="community-inline-avatar"
                      assetUrl={review.authorAvatarUrl}
                      alt={review.authorDisplayName}
                      fallbackContent={review.authorDisplayName.slice(0, 1).toUpperCase()}
                    />
                    <span>{review.authorDisplayName}</span>
                  </span>
                  <span>{formatReviewMeta(review, translate('booking.notYet'))}</span>
                </div>

                <div className="action-row">
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

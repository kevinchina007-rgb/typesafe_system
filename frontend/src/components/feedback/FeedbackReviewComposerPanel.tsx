import { useEffect, useState } from 'react'

import type { ContentImageResponse, ReviewEligibilityResponse } from '../../lib/api-dtos/content'
import { ContentImageUploader } from '../ContentImageUploader'

type FeedbackReviewComposerPanelProps = {
  isBusy: boolean
  eligibility: ReviewEligibilityResponse | null
  title: string
  translate: (translationKey: string) => string
  onUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  onSubmit: (payload: { rating: number; title: string; content: string; images: ContentImageResponse[] }) => Promise<void>
  onCancel: () => void
}

export function FeedbackReviewComposerPanel({
  isBusy,
  eligibility,
  title,
  translate,
  onUploadImage,
  onSubmit,
  onCancel,
}: FeedbackReviewComposerPanelProps) {
  const [rating, setRating] = useState(5)
  const [reviewTitle, setReviewTitle] = useState('')
  const [content, setContent] = useState('')
  const [images, setImages] = useState<ContentImageResponse[]>([])

  useEffect(() => {
    setRating(5)
    setReviewTitle('')
    setContent('')
    setImages([])
  }, [title])

  return (
    <section className="page-card feedback-review-composer">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('reviews.dialogEyebrow')}</p>
          <h2>{title}</h2>
        </div>
        <button type="button" className="secondary-button" disabled={isBusy} onClick={onCancel}>
          {translate('tourGroups.cancel')}
        </button>
      </div>

      {eligibility && !eligibility.canReview ? <p className="empty-state">{eligibility.reason ?? translate('reviews.notEligible')}</p> : null}

      {!eligibility || eligibility.canReview ? (
        <form
          className="stack-form"
          onSubmit={async event => {
            event.preventDefault()
            await onSubmit({
              rating,
              title: reviewTitle,
              content,
              images,
            })
          }}
        >
          <label>
            {translate('reviews.rating')}
            <select value={rating} onChange={event => setRating(Number(event.target.value))} disabled={isBusy}>
              {[5, 4, 3, 2, 1].map(value => (
                <option key={value} value={value}>
                  {value}
                </option>
              ))}
            </select>
          </label>

          <label>
            {translate('reviews.reviewTitle')}
            <input value={reviewTitle} onChange={event => setReviewTitle(event.target.value)} disabled={isBusy} />
          </label>

          <label>
            {translate('reviews.reviewContent')}
            <textarea value={content} onChange={event => setContent(event.target.value)} disabled={isBusy} rows={6} />
          </label>

          <ContentImageUploader
            images={images}
            isBusy={isBusy}
            translate={translate}
            onUploadImage={onUploadImage}
            onChangeImages={setImages}
          />

          <div className="action-row">
            <button type="submit" disabled={isBusy}>
              {translate('reviews.submit')}
            </button>
          </div>
        </form>
      ) : null}
    </section>
  )
}

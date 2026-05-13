import { useEffect, useState } from 'react'

import type { ContentImageResponse, ReviewEligibilityResponse } from '@/lib/mvp-types/index'
import { ContentImageUploader } from '@/pages/shared/content/ContentImageUploader'

type ReviewComposerDialogProps = {
  isOpen: boolean
  isBusy: boolean
  eligibility: ReviewEligibilityResponse | null
  title: string
  mode?: 'create' | 'edit'
  initialValue?: {
    rating: number
    title: string
    content: string
    images: ContentImageResponse[]
  } | null
  translate: (translationKey: string) => string
  onClose: () => void
  onUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  onSubmit: (payload: { rating: number; title: string; content: string; images: ContentImageResponse[] }) => Promise<void>
}

export function ReviewComposerDialog({
  isOpen,
  isBusy,
  eligibility,
  title,
  mode = 'create',
  initialValue,
  translate,
  onClose,
  onUploadImage,
  onSubmit,
}: ReviewComposerDialogProps) {
  const [rating, setRating] = useState(5)
  const [reviewTitle, setReviewTitle] = useState('')
  const [content, setContent] = useState('')
  const [images, setImages] = useState<ContentImageResponse[]>([])

  useEffect(() => {
    if (isOpen) {
      setRating(initialValue?.rating ?? 5)
      setReviewTitle(initialValue?.title ?? '')
      setContent(initialValue?.content ?? '')
      setImages(initialValue?.images ?? [])
    }
  }, [initialValue, isOpen])

  if (!isOpen) {
    return null
  }

  return (
    <div className="dialog-backdrop">
      <section className="dialog-card">
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('reviews.dialogEyebrow')}</p>
            <h3>{title}</h3>
          </div>
          <button type="button" className="secondary-button" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>

        {mode === 'create' && eligibility && !eligibility.canReview ? <p className="empty-state">{eligibility.reason ?? translate('reviews.notEligible')}</p> : null}

        {mode === 'edit' || eligibility?.canReview ? (
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
              <textarea value={content} onChange={event => setContent(event.target.value)} disabled={isBusy} rows={5} />
            </label>

            <ContentImageUploader
              images={images}
              isBusy={isBusy}
              translate={translate}
              onUploadImage={onUploadImage}
              onChangeImages={setImages}
            />

            <button type="submit" disabled={isBusy}>
              {mode === 'edit' ? translate('reviews.save') : translate('reviews.submit')}
            </button>
          </form>
        ) : null}
      </section>
    </div>
  )
}

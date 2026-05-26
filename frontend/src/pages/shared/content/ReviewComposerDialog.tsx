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
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6">
      <section className="grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20">
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('reviews.dialogEyebrow')}</p>
            <h3>{title}</h3>
          </div>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>

        {mode === 'create' && eligibility && !eligibility.canReview ? <p className="text-sm leading-6 text-slate-500">{eligibility.reason ?? translate('reviews.notEligible')}</p> : null}

        {mode === 'edit' || eligibility?.canReview ? (
          <form
            className="grid gap-4"
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

            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
              {mode === 'edit' ? translate('reviews.save') : translate('reviews.submit')}
            </button>
          </form>
        ) : null}
      </section>
    </div>
  )
}

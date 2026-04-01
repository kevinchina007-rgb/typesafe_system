import { useEffect, useState } from 'react'

import type { AppLanguage, ResourceReviewSummaryResponse, ReviewResponse } from '../lib/mvp-types'
import { ResourceReviewSummary } from './ResourceReviewSummary'
import { ReviewListDialog } from './ReviewListDialog'

type ResourceReviewSummaryLoaderProps = {
  currentLanguage: AppLanguage
  isBusy?: boolean
  isEnabled: boolean
  resourceType: string
  resourceId: string
  title: string
  translate: (translationKey: string) => string
  onLoadSummary: (payload: { resourceType: string; resourceId: string }) => Promise<ResourceReviewSummaryResponse>
  onLoadReviews: (payload: { resourceType: string; resourceId: string }) => Promise<ReviewResponse[]>
}

export function ResourceReviewSummaryLoader({
  currentLanguage,
  isBusy = false,
  isEnabled,
  resourceType,
  resourceId,
  title,
  translate,
  onLoadSummary,
  onLoadReviews,
}: ResourceReviewSummaryLoaderProps) {
  const [summary, setSummary] = useState<ResourceReviewSummaryResponse | null>(null)
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [isDialogOpen, setIsDialogOpen] = useState(false)

  useEffect(() => {
    if (!isEnabled) {
      setSummary(null)
      return
    }
    void onLoadSummary({ resourceType, resourceId }).then(setSummary).catch(() => setSummary(null))
  }, [isEnabled, onLoadSummary, resourceId, resourceType])

  if (!isEnabled) {
    return null
  }

  return (
    <>
      <div className="compact-action-block">
        <ResourceReviewSummary currentLanguage={currentLanguage} summary={summary} translate={translate} />
        <button
          type="button"
          className="secondary-button"
          disabled={isBusy}
          onClick={() => {
            void onLoadReviews({ resourceType, resourceId }).then(nextReviews => {
              setReviews(nextReviews)
              setIsDialogOpen(true)
            })
          }}
        >
          {translate('reviews.view')}
        </button>
      </div>

      <ReviewListDialog
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        isOpen={isDialogOpen}
        title={title}
        reviews={reviews}
        translate={translate}
        onClose={() => setIsDialogOpen(false)}
      />
    </>
  )
}

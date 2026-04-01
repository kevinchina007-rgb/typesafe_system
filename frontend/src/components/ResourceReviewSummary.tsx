import type { AppLanguage, ResourceReviewSummaryResponse } from '../lib/mvp-types'
import { summarizeReviewAggregate } from '../lib/content-presenter'

type ResourceReviewSummaryProps = {
  currentLanguage: AppLanguage
  summary: ResourceReviewSummaryResponse | null
  translate: (translationKey: string) => string
}

export function ResourceReviewSummary({
  currentLanguage,
  summary,
  translate,
}: ResourceReviewSummaryProps) {
  if (!summary) {
    return <p className="detail-label">{translate('reviews.summaryEmpty')}</p>
  }

  return <p className="detail-label">{summarizeReviewAggregate(summary, currentLanguage)}</p>
}

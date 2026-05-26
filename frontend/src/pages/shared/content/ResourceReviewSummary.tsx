import type { AppLanguage, ResourceReviewSummaryResponse } from '@/lib/mvp-types/index'
import { summarizeReviewAggregate } from '@/lib/presenters/content-presenter'

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
    return <p className="text-sm font-medium text-slate-500">{translate('reviews.summaryEmpty')}</p>
  }

  return <p className="text-sm font-medium text-slate-500">{summarizeReviewAggregate(summary, currentLanguage)}</p>
}

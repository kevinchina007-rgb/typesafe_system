import type { AttractionResultsSectionProps } from '../../objects'
import { AttractionResultCard } from './AttractionResultCard'

export function AttractionResultsSection({
  attractionResponses,
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  selectedTravelerIds,
  translate,
  useDateDraft,
  onRequireLogin,
  onBookAttraction,
  onLoadReviewSummary,
  onLoadReviews,
}: AttractionResultsSectionProps) {
  return (
    <div className="grid gap-3">
      {attractionResponses.length > 0 ? (
        attractionResponses.map(attractionResponse => (
          <AttractionResultCard
            key={attractionResponse.attractionId}
            attractionResponse={attractionResponse}
            currentLanguage={currentLanguage}
            isBusy={isBusy}
            isGuestMode={isGuestMode}
            travelers={travelers}
            selectedTravelerIds={selectedTravelerIds}
            translate={translate}
            useDateDraft={useDateDraft}
            onRequireLogin={onRequireLogin}
            onBookAttraction={onBookAttraction}
            onLoadReviewSummary={onLoadReviewSummary}
            onLoadReviews={onLoadReviews}
          />
        ))
      ) : (
        <p className="text-sm leading-6 text-slate-500">{translate('attractions.empty')}</p>
      )}
    </div>
  )
}

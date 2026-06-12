import type { AttractionResultsSectionProps } from '../../objects'
import { AttractionResultCard } from './AttractionResultCard'

// AttractionsPage 的结果列表区域，只负责把景点结果逐条渲染出来。
export function AttractionResultsSection({
  attractionResponses,
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  selectedTravelerIds,
  focusedSessionId,
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
            focusedSessionId={focusedSessionId}
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

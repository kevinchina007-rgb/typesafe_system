import type { TrainResultsSectionProps } from '@/pages/TrainsPage/objects'
import { TrainResultCard } from './TrainResultCard'

// TrainsPage 的结果列表区域，只负责把列车结果逐条渲染出来。
export function TrainResultsSection({
  currentLanguage,
  isBusy,
  isGuestMode,
  searchFromStation,
  searchToStation,
  trainResponses,
  selectedTravelerIds,
  translate,
  onRequireLogin,
  onBookTrain,
  onLoadReviewSummary,
  onLoadReviews,
}: TrainResultsSectionProps) {
  return (
    <div className="grid gap-4">
      {trainResponses.length > 0 ? (
        trainResponses.map(trainResponse => (
          <TrainResultCard
            key={trainResponse.trainId}
            currentLanguage={currentLanguage}
            isBusy={isBusy}
            isGuestMode={isGuestMode}
            searchFromStation={searchFromStation}
            searchToStation={searchToStation}
            trainResponse={trainResponse}
            selectedTravelerIds={selectedTravelerIds}
            translate={translate}
            onRequireLogin={onRequireLogin}
            onBookTrain={onBookTrain}
            onLoadReviewSummary={onLoadReviewSummary}
            onLoadReviews={onLoadReviews}
          />
        ))
      ) : (
        <p className="text-sm leading-6 text-slate-500">{translate('trains.empty')}</p>
      )}
    </div>
  )
}

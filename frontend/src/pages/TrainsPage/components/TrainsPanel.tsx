import { useMemo, useState } from 'react'

import { useTrainSearchState } from '@/pages/TrainsPage/components/hooks/useTrainSearchState'
import { formatTrainRecommendation, sortTrainResponses, type TrainSortMode } from '@/app/stores/models/train-booking-model'
import { TrainFilterBar } from '@/pages/TrainsPage/components/sections/TrainFilterBar'
import { TrainPageHero } from '@/pages/TrainsPage/components/sections/TrainPageHero'
import { TrainResultsSection } from '@/pages/TrainsPage/components/sections/TrainResultsSection'
import { TrainSearchCard } from '@/pages/TrainsPage/components/sections/TrainSearchCard'
import type { TrainsPanelProps } from '@/app/stores/models/train-booking-model'

export function TrainsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onRequireLogin,
  onSearchTrains,
  onBookTrain,
  onLoadReviewSummary,
  onLoadReviews,
}: TrainsPanelProps) {
  const [trainSortMode, setTrainSortMode] = useState<TrainSortMode>('highSpeedPriority')
  const {
    trainResponses,
    hasSearchedTrains,
    searchDate,
    searchFromStation,
    searchToStation,
    setTrainResponses,
    setHasSearchedTrains,
    setSearchDate,
    setSearchFromStation,
    setSearchToStation,
  } = useTrainSearchState()

  const sortedTrainResponses = useMemo(
    () => sortTrainResponses(trainResponses, searchFromStation, searchToStation, trainSortMode),
    [searchFromStation, searchToStation, trainResponses, trainSortMode],
  )

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <TrainPageHero eyebrow={translate('trains.title')} title={translate('trains.searchModuleTitle')} description={translate('trains.description')} />

      <TrainSearchCard
        isBusy={isBusy}
        searchDate={searchDate}
        searchFromStation={searchFromStation}
        searchToStation={searchToStation}
        translate={translate}
        onSearchDateChange={setSearchDate}
        onSearchFromStationChange={setSearchFromStation}
        onSearchToStationChange={setSearchToStation}
        onSearch={async () => {
          const nextTrains = await onSearchTrains({
            fromStation: searchFromStation,
            toStation: searchToStation,
            date: searchDate,
          })
          setHasSearchedTrains(true)
          setTrainResponses(nextTrains)
        }}
      />

      {hasSearchedTrains ? <TrainFilterBar currentSortMode={trainSortMode} translate={translate} onSortModeChange={setTrainSortMode} /> : null}

      {isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('trains.guest')}</p> : null}

      {hasSearchedTrains ? (
        <TrainResultsSection
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          searchFromStation={searchFromStation}
          searchToStation={searchToStation}
          trainResponses={sortedTrainResponses}
          travelers={travelers}
          translate={translate}
          onRequireLogin={onRequireLogin}
          onBookTrain={onBookTrain}
          onLoadReviewSummary={onLoadReviewSummary}
          onLoadReviews={onLoadReviews}
        />
      ) : (
        <p className="text-sm leading-6 text-slate-500">{formatTrainRecommendation(searchFromStation, searchToStation, translate)}</p>
      )}
    </section>
  )
}

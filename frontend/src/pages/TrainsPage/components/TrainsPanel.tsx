import { useTrainSearchState } from '@/pages/TrainsPage/components/hooks/useTrainSearchState'
import { applyTrainQuickDatePreset, formatTrainPriceInsight, formatTrainRecommendation, trainHotRoutes, trainPopularStations, trainRecentSearches } from '@/app/stores/models/train-booking-model'
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
  const {
    trainResponses,
    hasSearchedTrains,
    tripType,
    searchDate,
    returnDate,
    searchFromStation,
    searchToStation,
    passengerCount,
    seatPreference,
    trainTypePreference,
    selectedQuickDatePreset,
    setTrainResponses,
    setHasSearchedTrains,
    setTripType,
    setSearchDate,
    setReturnDate,
    setSearchFromStation,
    setSearchToStation,
    setPassengerCount,
    setSeatPreference,
    setTrainTypePreference,
    setSelectedQuickDatePreset,
  } = useTrainSearchState()

  return (
    <section className="page-card">
      <TrainPageHero title={translate('trains.title')} description={translate('trains.description')} />

      <TrainSearchCard
        earliestDepartureHint={translate('trains.earliestDepartureValue')}
        hotRoutes={trainHotRoutes}
        isBusy={isBusy}
        lowestPriceHint={formatTrainPriceInsight(trainResponses, translate)}
        passengerCount={passengerCount}
        recentSearches={trainRecentSearches}
        popularStations={trainPopularStations}
        returnDate={returnDate}
        searchDate={searchDate}
        searchFromStation={searchFromStation}
        searchToStation={searchToStation}
        seatPreference={seatPreference}
        selectedQuickDatePreset={selectedQuickDatePreset}
        trainTypePreference={trainTypePreference}
        translate={translate}
        tripType={tripType}
        onTripTypeChange={setTripType}
        onSearchDateChange={setSearchDate}
        onReturnDateChange={setReturnDate}
        onSearchFromStationChange={setSearchFromStation}
        onSearchToStationChange={setSearchToStation}
        onPassengerCountChange={setPassengerCount}
        onSeatPreferenceChange={setSeatPreference}
        onTrainTypePreferenceChange={setTrainTypePreference}
        onSelectQuickDatePreset={preset => {
          setSelectedQuickDatePreset(preset)
          const nextDate = applyTrainQuickDatePreset(preset)
          setSearchDate(nextDate)
          if (tripType === 'roundTrip') {
            setReturnDate(applyTrainQuickDatePreset('nextWeek', new Date(nextDate)))
          }
        }}
        onSelectRoute={route => {
          setSearchFromStation(route.departureLabel)
          setSearchToStation(route.arrivalLabel)
        }}
        onSearch={async () => {
          const nextTrains = await onSearchTrains({
            fromStation: searchFromStation,
            toStation: searchToStation,
            date: searchDate,
            returnDate,
            tripType,
            passengerCount,
            seatPreference,
            trainTypePreference,
          })
          setHasSearchedTrains(true)
          setTrainResponses(nextTrains)
        }}
      />

      <TrainFilterBar translate={translate} />

      {isGuestMode ? <p className="empty-state">{translate('trains.guest')}</p> : null}

      {hasSearchedTrains ? (
        <TrainResultsSection
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          searchFromStation={searchFromStation}
          searchToStation={searchToStation}
          trainResponses={trainResponses}
          travelers={travelers}
          translate={translate}
          onRequireLogin={onRequireLogin}
          onBookTrain={onBookTrain}
          onLoadReviewSummary={onLoadReviewSummary}
          onLoadReviews={onLoadReviews}
        />
      ) : (
        <p className="empty-state">{formatTrainRecommendation(searchFromStation, searchToStation, translate)}</p>
      )}
    </section>
  )
}

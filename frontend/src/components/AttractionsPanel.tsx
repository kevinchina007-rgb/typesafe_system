import { useEffect } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '../app/stores/advertising-store'
import { AdvertisementCardRail } from './advertising/sections/AdvertisementCardRail'
import { travelMvpApiClient } from '../lib/api-client'
import { useAttractionSearchState } from './attractions/hooks/useAttractionSearchState'
import {
  attractionHotSpots,
  attractionRecentSearches,
  formatAttractionInsight,
} from './attractions/attractionBookingModel'
import { AttractionFilterBar } from './attractions/sections/AttractionFilterBar'
import { AttractionPageHero } from './attractions/sections/AttractionPageHero'
import { AttractionResultsSection } from './attractions/sections/AttractionResultsSection'
import { AttractionSearchCard } from './attractions/sections/AttractionSearchCard'
import type { AttractionsPanelProps } from './attractions/attractionBookingModel'

export function AttractionsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onRequireLogin,
  onSearchAttractions,
  onBookAttraction,
  onLoadReviewSummary,
  onLoadReviews,
}: AttractionsPanelProps) {
  const {
    attractionResponses,
    hasSearchedAttractions,
    searchCity,
    keyword,
    useDateDraft,
    travelerCount,
    attractionType,
    sortPreference,
    selectedQuickDatePreset,
    setAttractionResponses,
    setHasSearchedAttractions,
    setSearchCity,
    setKeyword,
    setUseDateDraft,
    setTravelerCount,
    setAttractionType,
    setSortPreference,
    setSelectedQuickDatePreset,
  } = useAttractionSearchState()
  const deliveryAdvertisements = useDeliverableAdvertisements('attractionBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)

  useEffect(() => {
    void loadDeliverableAdvertisements('attractionBooking')
    const reloadDeliverableAdvertisements = () => {
      void loadDeliverableAdvertisements('attractionBooking')
    }

    const handleVisibilityChange = () => {
      if (document.visibilityState === 'visible') {
        reloadDeliverableAdvertisements()
      }
    }

    window.addEventListener('focus', reloadDeliverableAdvertisements)
    document.addEventListener('visibilitychange', handleVisibilityChange)

    return () => {
      window.removeEventListener('focus', reloadDeliverableAdvertisements)
      document.removeEventListener('visibilitychange', handleVisibilityChange)
    }
  }, [loadDeliverableAdvertisements])

  return (
    <section className="page-card">
      <AttractionPageHero title={translate('attractions.title')} description={translate('attractions.description')} />

      <AttractionSearchCard
        attractionType={attractionType}
        hotAttractions={attractionHotSpots}
        insight={formatAttractionInsight(attractionResponses, translate)}
        isBusy={isBusy}
        keyword={keyword}
        recentSearches={attractionRecentSearches}
        searchCity={searchCity}
        selectedQuickDatePreset={selectedQuickDatePreset}
        sortPreference={sortPreference}
        travelerCount={travelerCount}
        translate={translate}
        useDateDraft={useDateDraft}
        onSearchCityChange={setSearchCity}
        onKeywordChange={setKeyword}
        onUseDateChange={setUseDateDraft}
        onTravelerCountChange={setTravelerCount}
        onAttractionTypeChange={setAttractionType}
        onSortPreferenceChange={setSortPreference}
        onSelectQuickDatePreset={(preset, nextDate) => {
          setSelectedQuickDatePreset(preset)
          setUseDateDraft(nextDate)
        }}
        onSelectHotAttraction={value => {
          const [city, ...restParts] = value.split(' ')
          setSearchCity(city)
          setKeyword(restParts.join(' '))
        }}
        onSearch={async () => {
          const nextAttractions = await onSearchAttractions({
            city: searchCity,
            keyword,
            useDate: useDateDraft,
            travelerCount,
            attractionType,
            sortPreference,
          })
          setHasSearchedAttractions(true)
          setAttractionResponses(nextAttractions)
        }}
      />

      <AdvertisementCardRail
        advertisements={deliveryAdvertisements}
        translate={translate}
        onOpenAdvertisement={async advertisement => {
          const nextAttraction = await travelMvpApiClient.getAttraction(advertisement.targetResourceId, {
            useDate: useDateDraft,
          })
          setSearchCity(nextAttraction.city)
          setKeyword(nextAttraction.attractionName)
          setHasSearchedAttractions(true)
          setAttractionResponses([nextAttraction])
          window.scrollTo({ top: 0, behavior: 'smooth' })
        }}
      />

      <AttractionFilterBar translate={translate} />

      {isGuestMode ? <p className="empty-state">{translate('attractions.guest')}</p> : null}

      {hasSearchedAttractions ? (
        <AttractionResultsSection
          attractionResponses={attractionResponses}
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          travelers={travelers}
          translate={translate}
          useDateDraft={useDateDraft}
          onRequireLogin={onRequireLogin}
          onBookAttraction={onBookAttraction}
          onLoadReviewSummary={onLoadReviewSummary}
          onLoadReviews={onLoadReviews}
        />
      ) : null}
    </section>
  )
}

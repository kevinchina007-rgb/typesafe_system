import { useEffect } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { useAttractionSearchState } from '@/pages/AttractionsPage/components/hooks/useAttractionSearchState'
import { attractionHotSpots, attractionRecentSearches, formatAttractionInsight } from '@/app/stores/models/attraction-booking-model'
import { AttractionFilterBar } from '@/pages/AttractionsPage/components/sections/AttractionFilterBar'
import { AttractionPageHero } from '@/pages/AttractionsPage/components/sections/AttractionPageHero'
import { AttractionResultsSection } from '@/pages/AttractionsPage/components/sections/AttractionResultsSection'
import { AttractionSearchCard } from '@/pages/AttractionsPage/components/sections/AttractionSearchCard'
import type { AttractionsPanelProps } from '@/app/stores/models/attraction-booking-model'

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
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
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

      {isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('attractions.guest')}</p> : null}

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

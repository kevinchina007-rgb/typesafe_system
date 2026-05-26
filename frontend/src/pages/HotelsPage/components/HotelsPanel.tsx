import { useEffect, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { useHotelSearchState } from '@/pages/HotelsPage/components/hooks/useHotelSearchState'
import { formatHotelPriceInsight, hotelHotDestinations, hotelRecentSearches } from '@/app/stores/models/hotel-booking-model'
import { HotelFilterBar } from '@/pages/HotelsPage/components/sections/HotelFilterBar'
import { HotelPageHero } from '@/pages/HotelsPage/components/sections/HotelPageHero'
import { HotelResultsSection } from '@/pages/HotelsPage/components/sections/HotelResultsSection'
import { HotelSearchCard } from '@/pages/HotelsPage/components/sections/HotelSearchCard'
import type { HotelsPanelProps } from '@/app/stores/models/hotel-booking-model'

export function HotelsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onRequireLogin,
  onSearchHotels,
  onBookHotel,
  onLoadReviewSummary,
  onLoadReviews,
}: HotelsPanelProps) {
  const {
    hotelResponses,
    hasSearchedHotels,
    searchLocation,
    searchCheckInDate,
    searchCheckOutDate,
    roomCount,
    guestCount,
    hotelPreference,
    nearbyPreference,
    selectedQuickDatePreset,
    setHotelResponses,
    setHasSearchedHotels,
    setSearchLocation,
    setSearchCheckInDate,
    setSearchCheckOutDate,
    setRoomCount,
    setGuestCount,
    setHotelPreference,
    setNearbyPreference,
    setSelectedQuickDatePreset,
  } = useHotelSearchState()
  const deliveryAdvertisements = useDeliverableAdvertisements('hotelBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)
  const [selectedAdvertisement, setSelectedAdvertisement] = useState<AdvertisementResponse | null>(null)

  useEffect(() => {
    void loadDeliverableAdvertisements('hotelBooking')
    const reloadDeliverableAdvertisements = () => {
      void loadDeliverableAdvertisements('hotelBooking')
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
      <HotelPageHero title={translate('hotels.title')} description={translate('hotels.description')} />

      <HotelSearchCard
        averagePriceInsight={formatHotelPriceInsight(hotelResponses, translate)}
        guestCount={guestCount}
        hotelPreference={hotelPreference}
        hotDestinations={hotelHotDestinations}
        isBusy={isBusy}
        nearbyPreference={nearbyPreference}
        recentSearches={hotelRecentSearches}
        roomCount={roomCount}
        searchCheckInDate={searchCheckInDate}
        searchCheckOutDate={searchCheckOutDate}
        searchLocation={searchLocation}
        selectedQuickDatePreset={selectedQuickDatePreset}
        translate={translate}
        onSearchLocationChange={setSearchLocation}
        onSearchCheckInDateChange={setSearchCheckInDate}
        onSearchCheckOutDateChange={setSearchCheckOutDate}
        onRoomCountChange={setRoomCount}
        onGuestCountChange={setGuestCount}
        onHotelPreferenceChange={setHotelPreference}
        onNearbyPreferenceChange={setNearbyPreference}
        onSelectQuickDatePreset={(preset, nextDates) => {
          setSelectedQuickDatePreset(preset)
          setSearchCheckInDate(nextDates.checkInDate)
          setSearchCheckOutDate(nextDates.checkOutDate)
        }}
        onSelectDestination={setSearchLocation}
        onSearch={async () => {
          const nextHotelResponses = await onSearchHotels({
            location: searchLocation,
            checkInDate: searchCheckInDate,
            checkOutDate: searchCheckOutDate,
            roomCount,
            guestCount,
            hotelPreference,
            nearbyPreference,
          })
          setHasSearchedHotels(true)
          setHotelResponses(nextHotelResponses)
        }}
      />

      <AdvertisementCardRail
        advertisements={deliveryAdvertisements}
        translate={translate}
        onOpenAdvertisement={async advertisement => {
          setSelectedAdvertisement(advertisement)
          const nextHotelResponse = await travelMvpApiClient.getHotel(advertisement.targetResourceId, {
            checkInDate: searchCheckInDate,
            checkOutDate: searchCheckOutDate,
          })
          setSearchLocation(nextHotelResponse.location)
          setHasSearchedHotels(true)
          setHotelResponses([nextHotelResponse])
          window.scrollTo({ top: 0, behavior: 'smooth' })
        }}
      />

      {selectedAdvertisement ? (
        <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
          <div className="flex flex-wrap items-start justify-between gap-4">
            <div>
              <p className="text-sm font-bold text-slate-500">{translate('advertising.selectedEyebrow')}</p>
              <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{selectedAdvertisement.title}</h3>
            </div>
          </div>
          <div className="grid gap-2 text-sm text-slate-600">
            <p>{selectedAdvertisement.subtitle}</p>
            <p>{selectedAdvertisement.description}</p>
            <span className="text-sm font-medium text-slate-500">{`${translate('advertising.targetResourceLabel')}: ${selectedAdvertisement.resourceSummaryTitle}`}</span>
          </div>
        </section>
      ) : null}

      <HotelFilterBar translate={translate} />

      {isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}

      {hasSearchedHotels ? (
        <HotelResultsSection
          currentLanguage={currentLanguage}
          hotelResponses={hotelResponses}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          searchCheckInDate={searchCheckInDate}
          searchCheckOutDate={searchCheckOutDate}
          travelers={travelers}
          translate={translate}
          onRequireLogin={onRequireLogin}
          onBookHotel={onBookHotel}
          onLoadReviewSummary={onLoadReviewSummary}
          onLoadReviews={onLoadReviews}
        />
      ) : null}
    </section>
  )
}

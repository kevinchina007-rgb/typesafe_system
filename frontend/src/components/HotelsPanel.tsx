import { useEffect } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '../app/stores/advertising-store'
import { AdvertisementCardRail } from './advertising/sections/AdvertisementCardRail'
import { travelMvpApiClient } from '../lib/api-client'
import { useHotelSearchState } from './hotels/hooks/useHotelSearchState'
import {
  formatHotelPriceInsight,
  hotelHotDestinations,
  hotelRecentSearches,
} from './hotels/hotelBookingModel'
import { HotelFilterBar } from './hotels/sections/HotelFilterBar'
import { HotelPageHero } from './hotels/sections/HotelPageHero'
import { HotelResultsSection } from './hotels/sections/HotelResultsSection'
import { HotelSearchCard } from './hotels/sections/HotelSearchCard'
import type { HotelsPanelProps } from './hotels/hotelBookingModel'

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
    <section className="page-card">
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

      <HotelFilterBar translate={translate} />

      {isGuestMode ? <p className="empty-state">{translate('hotels.guest')}</p> : null}

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

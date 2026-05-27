import { useEffect, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { useHotelSearchState } from '@/pages/HotelsPage/components/hooks/useHotelSearchState'
import { hotelHotDestinations } from '@/app/stores/models/hotel-booking-model'
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
    setHotelResponses,
    setHasSearchedHotels,
    setSearchLocation,
    setSearchCheckInDate,
    setSearchCheckOutDate,
    setRoomCount,
    setGuestCount,
    setHotelPreference,
    setNearbyPreference,
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
        hotDestinations={hotelHotDestinations}
        isBusy={isBusy}
        searchCheckInDate={searchCheckInDate}
        searchCheckOutDate={searchCheckOutDate}
        searchLocation={searchLocation}
        translate={translate}
        onSearchLocationChange={setSearchLocation}
        onSearchCheckInDateChange={setSearchCheckInDate}
        onSearchCheckOutDateChange={setSearchCheckOutDate}
        onSelectDestination={setSearchLocation}
        onSearch={async () => {
          const nextHotelResponses = await onSearchHotels({
            location: searchLocation,
            checkInDate: searchCheckInDate,
            checkOutDate: searchCheckOutDate,
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

      {hasSearchedHotels ? (
        <HotelFilterBar
          roomCount={roomCount}
          guestCount={guestCount}
          hotelPreference={hotelPreference}
          nearbyPreference={nearbyPreference}
          isBusy={isBusy}
          translate={translate}
          onRoomCountChange={setRoomCount}
          onGuestCountChange={setGuestCount}
          onHotelPreferenceChange={setHotelPreference}
          onNearbyPreferenceChange={setNearbyPreference}
        />
      ) : null}

      {isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}

      {hasSearchedHotels ? (
        <HotelResultsSection
          currentLanguage={currentLanguage}
          hotelResponses={hotelResponses}
          isBusy={isBusy}
          isGuestMode={isGuestMode}
          defaultRoomCount={roomCount}
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

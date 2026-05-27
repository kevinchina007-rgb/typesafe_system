import { useEffect, useMemo, useRef, useState } from 'react'

import { useAdvertisingStore, useDeliverableAdvertisements } from '@/app/stores/advertising-store'
import { addHotelDays, hotelHotDestinations } from '@/app/stores/models/hotel-booking-model'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import type { HotelsPanelProps } from '@/app/stores/models/hotel-booking-model'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { useHotelSearchState } from '@/pages/HotelsPage/components/hooks/useHotelSearchState'
import { HotelDatePriceStrip } from '@/pages/HotelsPage/components/sections/HotelDatePriceStrip'
import { HotelFilterBar } from '@/pages/HotelsPage/components/sections/HotelFilterBar'
import { HotelPageHero } from '@/pages/HotelsPage/components/sections/HotelPageHero'
import { HotelResultsSection } from '@/pages/HotelsPage/components/sections/HotelResultsSection'
import { HotelSearchCard } from '@/pages/HotelsPage/components/sections/HotelSearchCard'

type SearchNotice = {
  kind: 'error' | 'warning'
  message: string
}

function getLowestRoomPrice(hotelResponses: Array<{ roomTypes: Array<{ basePrice: string }> }>): number | null {
  const prices = hotelResponses.flatMap(hotel => hotel.roomTypes.map(roomType => Number(roomType.basePrice)).filter(Number.isFinite))
  if (prices.length === 0) {
    return null
  }

  return Math.min(...prices)
}

export function HotelsPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onRequireLogin,
  onValidationError,
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
    hotelPreference,
    nearbyPreference,
    setHotelPlannerResponses,
    setHasSearchedHotels,
    setSearchLocation,
    setSearchCheckInDate,
    setSearchCheckOutDate,
    setHotelPreference,
    setNearbyPreference,
  } = useHotelSearchState()
  const deliveryAdvertisements = useDeliverableAdvertisements('hotelBooking')
  const loadDeliverableAdvertisements = useAdvertisingStore(state => state.loadDeliverableAdvertisements)
  const [selectedAdvertisement, setSelectedAdvertisement] = useState<AdvertisementResponse | null>(null)
  const [searchNotice, setSearchNotice] = useState<SearchNotice | null>(null)
  const [searchRevision, setSearchRevision] = useState(0)
  const [dateWindowStart, setDateWindowStart] = useState(() => addHotelDays(searchCheckInDate, -3))
  const resultsSectionRef = useRef<HTMLDivElement | null>(null)
  const noticeSectionRef = useRef<HTMLDivElement | null>(null)

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

  useEffect(() => {
    setDateWindowStart(addHotelDays(searchCheckInDate, -3))
  }, [searchCheckInDate])

  useEffect(() => {
    if (searchRevision <= 0) {
      return
    }

    resultsSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }, [searchRevision])

  useEffect(() => {
    if (!searchNotice) {
      return
    }

    noticeSectionRef.current?.scrollIntoView({ behavior: 'smooth', block: 'center' })
  }, [searchNotice])

  const featuredAdvertisement = deliveryAdvertisements[0] ?? null
  const hotelLowestNightlyPrice = useMemo(() => getLowestRoomPrice(hotelResponses), [hotelResponses])

  async function executeHotelSearch(nextLocation: string, nextCheckInDate: string, nextCheckOutDate: string) {
    const normalizedLocation = nextLocation.trim()
    const normalizedCheckInDate = nextCheckInDate.trim()
    const normalizedCheckOutDate = nextCheckOutDate.trim()

    if (!normalizedLocation) {
      const message = translate('hotels.location') + ' ' + translate('error.requiredField')
      setSearchNotice({ kind: 'error', message })
      onValidationError(message)
      return
    }

    if (!normalizedCheckInDate || !normalizedCheckOutDate) {
      const message = translate('error.friendly.default')
      setSearchNotice({ kind: 'error', message })
      onValidationError(message)
      return
    }

    if (normalizedCheckOutDate <= normalizedCheckInDate) {
      const message = translate('error.hotelStayDate')
      setSearchNotice({ kind: 'error', message })
      onValidationError(message)
      return
    }

    try {
      setSearchLocation(normalizedLocation)
      setSearchCheckInDate(normalizedCheckInDate)
      setSearchCheckOutDate(normalizedCheckOutDate)
      setSelectedAdvertisement(null)

      const nextHotelPlannerResponses = await onSearchHotels({
        location: normalizedLocation,
        checkInDate: normalizedCheckInDate,
        checkOutDate: normalizedCheckOutDate,
      })

      setSearchNotice(
        nextHotelPlannerResponses.length > 0
          ? null
          : {
              kind: 'warning',
              message: translate('hotels.empty'),
            },
      )
      setHasSearchedHotels(true)
      setHotelPlannerResponses(nextHotelPlannerResponses)
      setDateWindowStart(addHotelDays(normalizedCheckInDate, -3))
      setSearchRevision(revision => revision + 1)
    } catch (error) {
      setHasSearchedHotels(false)
      setHotelPlannerResponses([])
      const message = error instanceof Error ? error.message : translate('error.friendly.default')
      setSearchNotice({ kind: 'error', message })
      onValidationError(message)
    }
  }

  async function openAdvertisement(advertisement: AdvertisementResponse) {
    setSelectedAdvertisement(advertisement)
    const nextHotelPlannerResponse = await travelMvpApiClient.getHotelDetailsPlanner(advertisement.targetResourceId, {
      checkInDate: searchCheckInDate,
      checkOutDate: searchCheckOutDate,
    })
    setSearchLocation(nextHotelPlannerResponse.location)
    setHasSearchedHotels(true)
    setHotelPlannerResponses([nextHotelPlannerResponse])
    setSearchNotice(null)
    setDateWindowStart(addHotelDays(searchCheckInDate, -3))
    setSearchRevision(revision => revision + 1)
    window.scrollTo({ top: 0, behavior: 'smooth' })
  }

  async function handleDateSelect(nextCheckInDate: string) {
    const nextCheckOutDate = addHotelDays(nextCheckInDate, 1)
    await executeHotelSearch(searchLocation, nextCheckInDate, nextCheckOutDate)
  }

  return (
    <section className="grid gap-5 border-y border-sky-100 bg-gradient-to-b from-slate-50 via-white to-sky-50 p-6 text-slate-950 shadow-sm shadow-sky-100/40">
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
          await executeHotelSearch(searchLocation, searchCheckInDate, searchCheckOutDate)
        }}
      />

      {searchNotice ? (
        <div
          ref={noticeSectionRef}
          className={`px-4 py-3 text-sm leading-6 ${
            searchNotice.kind === 'error'
              ? 'border border-rose-200 bg-rose-50 text-rose-700'
              : 'border border-amber-200 bg-amber-50 text-amber-800'
          }`}
        >
          {searchNotice.message}
        </div>
      ) : null}

      <section className="grid gap-4 border border-indigo-100 bg-gradient-to-r from-white via-indigo-50 to-cyan-50 p-6 text-slate-950 shadow-lg shadow-indigo-100/50">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-black uppercase tracking-[0.18em] text-indigo-600">{translate('advertising.deliveryEyebrow')}</p>
            <h3 className="m-0 text-3xl font-black leading-tight text-slate-950">{translate('advertising.deliveryTitle')}</h3>
          </div>
        </div>

        {featuredAdvertisement ? (
          <div className="grid gap-4 border border-sky-100 bg-white p-4 shadow-sm shadow-sky-100/40 md:grid-cols-[18rem_1fr]">
            <button
              type="button"
              className="grid gap-3 text-left"
              onClick={() => void openAdvertisement(featuredAdvertisement)}
            >
              <BackendAssetImage
                assetUrl={featuredAdvertisement.imageUrl}
                alt={featuredAdvertisement.title}
                className="aspect-[16/10] w-full object-cover"
                fallbackContent={featuredAdvertisement.title}
              />
              <strong className="text-2xl font-black text-slate-950">{featuredAdvertisement.title}</strong>
            </button>

            <div className="grid gap-2">
              <span className="inline-flex w-fit bg-indigo-600 px-2 py-1 text-xs font-bold text-white">
                {translate('advertising.deliveryEyebrow')}
              </span>
              <p className="text-base font-semibold text-slate-700">{featuredAdvertisement.subtitle}</p>
              <p className="text-sm leading-6 text-slate-600">{featuredAdvertisement.description}</p>
              <span className="text-sm font-bold text-slate-500">{featuredAdvertisement.resourceSummaryTitle}</span>
              <button
                type="button"
                className="mt-2 inline-flex min-h-11 w-fit items-center justify-center border border-transparent bg-gradient-to-r from-sky-500 to-cyan-500 px-4 py-2 text-sm font-black text-white transition hover:from-sky-600 hover:to-cyan-600"
                onClick={() => void openAdvertisement(featuredAdvertisement)}
              >
                {featuredAdvertisement.ctaLabel}
              </button>
            </div>
          </div>
        ) : (
          <div className="border border-dashed border-slate-200 bg-slate-50 p-6 text-sm leading-6 text-slate-500">
            {translate('advertising.slotEmptyDescription')}
          </div>
        )}

        {selectedAdvertisement ? (
          <div className="grid gap-2 border border-sky-100 bg-white p-4 text-sm text-slate-600 shadow-sm shadow-sky-100/40">
            <p className="text-sm font-black uppercase tracking-[0.16em] text-sky-600">{translate('advertising.selectedEyebrow')}</p>
            <p>{selectedAdvertisement.title}</p>
            <p>{selectedAdvertisement.subtitle}</p>
            <p>{selectedAdvertisement.description}</p>
          </div>
        ) : null}
      </section>

      {hasSearchedHotels ? (
        <div ref={resultsSectionRef} className="grid gap-4">
          <HotelDatePriceStrip
            dateWindowStart={dateWindowStart}
            selectedDate={searchCheckInDate}
            lowestPrice={hotelLowestNightlyPrice}
            isBusy={isBusy}
            onPrevious={() => setDateWindowStart(date => addHotelDays(date, -1))}
            onNext={() => setDateWindowStart(date => addHotelDays(date, 1))}
            onDateSelect={date => void handleDateSelect(date)}
          />

          <HotelFilterBar
            hotelPreference={hotelPreference}
            nearbyPreference={nearbyPreference}
            isBusy={isBusy}
            translate={translate}
            onHotelPreferenceChange={setHotelPreference}
            onNearbyPreferenceChange={setNearbyPreference}
          />

          {isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}

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
        </div>
      ) : null}

      {!hasSearchedHotels && isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}
    </section>
  )
}

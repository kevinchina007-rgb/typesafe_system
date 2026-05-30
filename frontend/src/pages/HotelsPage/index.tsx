import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { hotelHotDestinations } from './functions'
import { useHotelsPageController } from './hooks'
import type { HotelsPageProps } from './objects'
import {
  HotelAdvertisingSection,
  HotelDatePriceStrip,
  HotelFilterBar,
  HotelPageHero,
  HotelResultsSection,
  HotelSearchCard,
  HotelSearchNotice,
} from './components'

export function HotelsPage(props: HotelsPageProps) {
  const controller = useHotelsPageController(props)
  const { currentLanguage, translate } = props

  return (
    <>
      <section className="grid gap-5 border-y border-sky-100 bg-gradient-to-b from-slate-50 via-white to-sky-50 p-6 text-slate-950 shadow-sm shadow-sky-100/40">
        <HotelPageHero title={translate('hotels.title')} description={translate('hotels.description')} />

        <HotelSearchCard
          hotDestinations={hotelHotDestinations}
          isBusy={controller.isBusy}
          searchCheckInDate={controller.searchCheckInDate}
          searchCheckOutDate={controller.searchCheckOutDate}
          searchLocation={controller.searchLocation}
          translate={translate}
          onSearchLocationChange={controller.setSearchLocation}
          onSearchCheckInDateChange={controller.setSearchCheckInDate}
          onSearchCheckOutDateChange={controller.setSearchCheckOutDate}
          onSelectDestination={controller.setSearchLocation}
          onSearch={() => {
            void controller.executeHotelSearch(controller.searchLocation, controller.searchCheckInDate, controller.searchCheckOutDate)
          }}
        />

        <HotelSearchNotice ref={controller.noticeSectionRef} notice={controller.searchNotice} />

        <HotelAdvertisingSection
          featuredAdvertisement={controller.featuredAdvertisement}
          selectedAdvertisement={controller.selectedAdvertisement}
          translate={translate}
          onOpenAdvertisement={controller.openAdvertisement}
        />

        {controller.hasSearchedHotels ? (
          <div ref={controller.resultsSectionRef} className="grid gap-4">
            <HotelDatePriceStrip
              dateWindowStart={controller.dateWindowStart}
              selectedDate={controller.searchCheckInDate}
              lowestPrice={controller.hotelLowestNightlyPrice}
              isBusy={controller.isBusy}
              onPrevious={controller.onPreviousDateWindow}
              onNext={controller.onNextDateWindow}
              onDateSelect={date => void controller.handleDateSelect(date)}
            />

            <HotelFilterBar
              hotelPreference={controller.hotelPreference}
              nearbyPreference={controller.nearbyPreference}
              isBusy={controller.isBusy}
              translate={translate}
              onHotelPreferenceChange={controller.setHotelPreference}
              onNearbyPreferenceChange={controller.setNearbyPreference}
            />

            {controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}

            <HotelResultsSection
              currentLanguage={currentLanguage}
              hotelResponses={controller.hotelResponses}
              isBusy={controller.isBusy}
              isGuestMode={controller.isGuestMode}
              defaultRoomCount={controller.roomCount}
              searchCheckInDate={controller.searchCheckInDate}
              searchCheckOutDate={controller.searchCheckOutDate}
              travelers={controller.travelers}
              translate={translate}
              onRequireLogin={controller.onRequireLogin}
              onBookHotel={controller.bookHotel}
              onLoadReviewSummary={controller.loadReviewSummary}
              onLoadReviews={controller.loadReviewsByResource}
            />
          </div>
        ) : null}

        {!controller.hasSearchedHotels && controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}
      </section>

      <AuthRequiredDialog
        isOpen={controller.isAuthDialogOpen}
        title={translate('authRequired.bookingTitle')}
        description={translate('authRequired.bookingDescription')}
        translate={translate}
        onClose={controller.onAuthDialogClose}
        onConfirm={controller.onAuthDialogConfirm}
      />
    </>
  )
}

import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { TravelerSelectionPanel } from '@/pages/shared/travelers/TravelerSelectionPanel'
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
  const displayHotelResponses =
    controller.isTourGroupTargetMode && controller.targetHotelResponses.length > 0
      ? controller.targetHotelResponses
      : controller.hotelResponses
  const displayHasSearchedHotels = controller.isTourGroupTargetMode ? displayHotelResponses.length > 0 : controller.hasSearchedHotels

  return (
    <>
      <section className="grid gap-5 border-y border-sky-100 bg-gradient-to-b from-slate-50 via-white to-sky-50 p-6 text-slate-950 shadow-sm shadow-sky-100/40">
        <HotelPageHero title={translate('hotels.title')} description={translate('hotels.description')} />

        {!controller.isTourGroupTargetMode ? (
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
        ) : (
          <section className="grid gap-2 border border-sky-100 bg-sky-50 px-6 py-4 text-slate-950">
            <p className="text-sm font-bold text-sky-700">团内定向预订</p>
            <p className="text-base text-slate-700">已定位到对应酒店房型，直接在下方完成预订。</p>
          </section>
        )}

        {displayHasSearchedHotels ? (
          <TravelerSelectionPanel
            title="选择出行人"
            hint="这里勾选的出行人会直接带到支付页"
            travelers={controller.travelers}
            selectedTravelerIds={controller.selectedTravelerIds}
            onToggleTravelerSelection={controller.toggleTravelerSelection}
            renderTravelerLabel={traveler => traveler.fullName}
            emptySelectionMessage="请至少选择一位出行人"
          />
        ) : null}

        <HotelSearchNotice ref={controller.noticeSectionRef} notice={controller.searchNotice} />

        {!controller.isTourGroupTargetMode ? (
          <HotelAdvertisingSection
            featuredAdvertisement={controller.featuredAdvertisement}
            selectedAdvertisement={controller.selectedAdvertisement}
            translate={translate}
            onOpenAdvertisement={controller.openAdvertisement}
          />
        ) : null}

        {displayHasSearchedHotels ? (
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
              hotelResponses={displayHotelResponses}
              isBusy={controller.isBusy}
              isGuestMode={controller.isGuestMode}
              defaultRoomCount={controller.roomCount}
              searchCheckInDate={controller.searchCheckInDate}
              searchCheckOutDate={controller.searchCheckOutDate}
              travelers={controller.travelers}
              selectedTravelerIds={controller.selectedTravelerIds}
              translate={translate}
              onToggleTravelerSelection={controller.toggleTravelerSelection}
              onRequireLogin={controller.onRequireLogin}
              onBookHotel={controller.bookHotel}
              onLoadReviewSummary={controller.loadReviewSummary}
              onLoadReviews={controller.loadReviewsByResource}
            />
          </div>
        ) : null}

        {!displayHasSearchedHotels && controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('hotels.guest')}</p> : null}
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

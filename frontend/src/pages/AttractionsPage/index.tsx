import { useMemo } from 'react'

import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { DateWindowStrip } from '@/pages/shared/date/DateWindowStrip'
import { TravelerSelectionPanel } from '@/pages/shared/travelers/TravelerSelectionPanel'
import { formatAttractionInsight, renderAttractionTravelerOptionLabel } from '@/app/stores/models/attraction-booking-model'
import type { AttractionsPageProps } from './objects'
import { ATTRACTION_HOT_SPOTS, ATTRACTION_RECENT_SEARCHES } from './objects'
import { AttractionFilterBar, AttractionPageHero, AttractionResultsSection, AttractionSearchCard } from './components'
import { useAttractionsPageController } from './hooks'

export function AttractionsPage({
  currentLanguage,
  signedInUser,
  translate,
  onNavigate,
  onShowNotice,
}: AttractionsPageProps) {
  const controller = useAttractionsPageController({
    currentLanguage,
    signedInUser,
    translate,
    onNavigate,
    onShowNotice,
  })
  const attractionAdvertisement = useMemo(() => controller.deliveryAdvertisements[0] ?? null, [controller.deliveryAdvertisements])

  return (
    <>
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <AttractionPageHero title={translate('attractions.title')} description={translate('attractions.description')} />

        <AttractionSearchCard
          hotAttractions={ATTRACTION_HOT_SPOTS}
          insight={formatAttractionInsight(controller.attractionResponses, translate)}
          isBusy={controller.isBusy}
          keyword={controller.keyword}
          recentSearches={ATTRACTION_RECENT_SEARCHES}
          searchCity={controller.searchCity}
          selectedQuickDatePreset={controller.selectedQuickDatePreset}
          translate={translate}
          useDateDraft={controller.useDateDraft}
          onSearchCityChange={controller.setSearchCity}
          onKeywordChange={controller.setKeyword}
          onUseDateChange={controller.setUseDateDraft}
          onSelectQuickDatePreset={controller.handleSelectQuickDatePreset}
          onSelectHotAttraction={controller.handleSelectHotAttraction}
          onSearch={controller.handleSearchAttractions}
        />

        {controller.hasSearchedAttractions ? (
          <TravelerSelectionPanel
            title="选择出行人"
            hint="这里勾选的出行人会直接带到支付页"
            travelers={controller.travelers}
            selectedTravelerIds={controller.selectedTravelerIds}
            onToggleTravelerSelection={controller.toggleTravelerSelection}
            renderTravelerLabel={traveler => renderAttractionTravelerOptionLabel(traveler, controller.useDateDraft)}
            emptySelectionMessage="请至少选择一位出行人"
          />
        ) : null}

        {controller.hasSearchedAttractions ? (
          <DateWindowStrip
            dateWindowStart={controller.dateWindowStart}
            selectedDate={controller.useDateDraft}
            isBusy={controller.isBusy}
            onPrevious={controller.onPreviousDateWindow}
            onNext={controller.onNextDateWindow}
            onDateSelect={controller.handleDateSelect}
          />
        ) : null}

        {attractionAdvertisement ? (
          <section className="grid gap-4 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
            <p className="text-sm font-bold text-slate-500">{translate('advertising.deliveryEyebrow')}</p>
            <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.deliveryTitle')}</h3>
            <AdvertisementCardRail
              advertisements={[attractionAdvertisement]}
              translate={translate}
              onOpenAdvertisement={controller.handleOpenAdvertisement}
            />
          </section>
        ) : null}

        <AttractionFilterBar
          hasSearchedAttractions={controller.hasSearchedAttractions}
          sortPreference={controller.sortPreference}
          translate={translate}
          onSortPreferenceChange={controller.setSortPreference}
        />

        {controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('attractions.guest')}</p> : null}

        {controller.hasSearchedAttractions ? (
          <AttractionResultsSection
            attractionResponses={controller.attractionResponses}
            currentLanguage={currentLanguage}
            isBusy={controller.isBusy}
            isGuestMode={controller.isGuestMode}
            travelers={controller.travelers}
            selectedTravelerIds={controller.selectedTravelerIds}
            translate={translate}
            useDateDraft={controller.useDateDraft}
            onToggleTravelerSelection={controller.toggleTravelerSelection}
            onRequireLogin={controller.handleOpenAuthDialog}
            onBookAttraction={controller.handleBookAttraction}
            onLoadReviewSummary={controller.handleLoadReviewSummary}
            onLoadReviews={controller.handleLoadReviews}
          />
        ) : null}
      </section>

      <AuthRequiredDialog
        isOpen={controller.isAuthDialogOpen}
        title={translate('authRequired.bookingTitle')}
        description={translate('authRequired.bookingDescription')}
        translate={translate}
        onClose={controller.handleCloseAuthDialog}
        onConfirm={controller.handleConfirmAuthDialog}
      />
    </>
  )
}

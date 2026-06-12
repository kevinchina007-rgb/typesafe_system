import { useMemo } from 'react'

import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { DateWindowStrip } from '@/pages/shared/date/DateWindowStrip'
import { TravelerSelectionPanel } from '@/pages/shared/travelers/TravelerSelectionPanel'
import { formatAttractionInsight, renderAttractionTravelerOptionLabel } from '@/app/stores/models/attraction-booking-model'
import type { AttractionsPageProps } from './objects'
import { ATTRACTION_HOT_SPOTS, ATTRACTION_RECENT_SEARCHES } from './objects'
import { AttractionPageHero, AttractionResultsSection, AttractionSearchCard } from './components'
import { useAttractionsPageController } from './hooks'

// AttractionsPage 页面入口，只负责把控制器状态分发给子组件。
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
  const displayAttractionResponses =
    controller.isTourGroupTargetMode && controller.targetAttractionResponses.length > 0
      ? controller.targetAttractionResponses
      : controller.attractionResponses
  const displayHasSearchedAttractions = controller.isTourGroupTargetMode
    ? displayAttractionResponses.length > 0
    : controller.hasSearchedAttractions

  return (
    <>
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <AttractionPageHero title={translate('attractions.title')} description={translate('attractions.description')} />

        {!controller.isTourGroupTargetMode ? (
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
        ) : (
          <section className="grid gap-2 border border-sky-100 bg-sky-50 px-6 py-4 text-slate-950">
            <p className="text-sm font-bold text-sky-700">团内定向预订</p>
            <p className="text-base text-slate-700">已定位到对应景点票，直接在下方完成预订。</p>
          </section>
        )}

        {displayHasSearchedAttractions ? (
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

        {displayHasSearchedAttractions ? (
          <DateWindowStrip
            dateWindowStart={controller.dateWindowStart}
            selectedDate={controller.useDateDraft}
            isBusy={controller.isBusy}
            onPrevious={controller.onPreviousDateWindow}
            onNext={controller.onNextDateWindow}
            onDateSelect={controller.handleDateSelect}
          />
        ) : null}

        {!controller.isTourGroupTargetMode && attractionAdvertisement ? (
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

        {controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('attractions.guest')}</p> : null}

        {displayHasSearchedAttractions ? (
          <AttractionResultsSection
            attractionResponses={displayAttractionResponses}
            currentLanguage={currentLanguage}
            isBusy={controller.isBusy}
            isGuestMode={controller.isGuestMode}
            travelers={controller.travelers}
            selectedTravelerIds={controller.selectedTravelerIds}
            focusedSessionId={controller.focusedSessionId}
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

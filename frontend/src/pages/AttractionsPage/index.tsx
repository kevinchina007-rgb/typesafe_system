import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { AdvertisementCardRail } from '@/pages/shared/advertising/sections/AdvertisementCardRail'
import { formatAttractionInsight } from '@/app/stores/models/attraction-booking-model'
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

  return (
    <>
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        {/* 页面标题区 */}
        <AttractionPageHero title={translate('attractions.title')} description={translate('attractions.description')} />

        {/* 搜索区 */}
        <AttractionSearchCard
          attractionType={controller.attractionType}
          hotAttractions={ATTRACTION_HOT_SPOTS}
          insight={formatAttractionInsight(controller.attractionResponses, translate)}
          isBusy={controller.isBusy}
          keyword={controller.keyword}
          recentSearches={ATTRACTION_RECENT_SEARCHES}
          searchCity={controller.searchCity}
          selectedQuickDatePreset={controller.selectedQuickDatePreset}
          sortPreference={controller.sortPreference}
          travelerCount={controller.travelerCount}
          translate={translate}
          useDateDraft={controller.useDateDraft}
          onSearchCityChange={controller.setSearchCity}
          onKeywordChange={controller.setKeyword}
          onUseDateChange={controller.setUseDateDraft}
          onTravelerCountChange={controller.setTravelerCount}
          onAttractionTypeChange={controller.setAttractionType}
          onSortPreferenceChange={controller.setSortPreference}
          onSelectQuickDatePreset={controller.handleSelectQuickDatePreset}
          onSelectHotAttraction={controller.handleSelectHotAttraction}
          onSearch={controller.handleSearchAttractions}
        />

        {/* 广告投放区 */}
        <AdvertisementCardRail
          advertisements={controller.deliveryAdvertisements}
          translate={translate}
          onOpenAdvertisement={controller.handleOpenAdvertisement}
        />

        {/* 筛选区 */}
        <AttractionFilterBar translate={translate} />

        {/* 访客提示区 */}
        {controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('attractions.guest')}</p> : null}

        {/* 结果区 */}
        {controller.hasSearchedAttractions ? (
          <AttractionResultsSection
            attractionResponses={controller.attractionResponses}
            currentLanguage={currentLanguage}
            isBusy={controller.isBusy}
            isGuestMode={controller.isGuestMode}
            travelers={controller.travelers}
            translate={translate}
            useDateDraft={controller.useDateDraft}
            onRequireLogin={controller.handleOpenAuthDialog}
            onBookAttraction={controller.handleBookAttraction}
            onLoadReviewSummary={controller.handleLoadReviewSummary}
            onLoadReviews={controller.handleLoadReviews}
          />
        ) : null}
      </section>

      {/* 登录提示弹窗 */}
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

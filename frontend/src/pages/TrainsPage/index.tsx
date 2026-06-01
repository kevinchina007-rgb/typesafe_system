import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
import { DateWindowStrip } from '@/pages/shared/date/DateWindowStrip'
import { TravelerSelectionPanel } from '@/pages/shared/travelers/TravelerSelectionPanel'
import { useTrainsPageController } from './hooks'
import type { TrainsPageProps } from './objects'
import { TrainFilterBar, TrainPageHero, TrainResultsSection, TrainSearchCard } from './components'

export function TrainsPage(props: TrainsPageProps) {
  const controller = useTrainsPageController(props)
  const { currentLanguage, translate } = props

  return (
    <>
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <TrainPageHero eyebrow={translate('trains.title')} title={translate('trains.searchModuleTitle')} description={translate('trains.description')} />

        <TrainSearchCard
          isBusy={controller.isBusy}
          searchDate={controller.searchDate}
          searchFromStation={controller.searchFromStation}
          searchToStation={controller.searchToStation}
          translate={translate}
          onSearchDateChange={controller.setSearchDate}
          onSearchFromStationChange={controller.setSearchFromStation}
          onSearchToStationChange={controller.setSearchToStation}
          onSearch={() => controller.executeTrainSearch()}
        />

        {controller.hasSearchedTrains ? (
          <TravelerSelectionPanel
            title="选择出行人"
            hint="这里勾选的出行人会直接带到支付页"
            travelers={controller.travelers}
            selectedTravelerIds={controller.selectedTravelerIds}
            onToggleTravelerSelection={controller.toggleTravelerSelection}
            renderTravelerLabel={traveler => `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`}
            emptySelectionMessage="请至少选择一位出行人"
          />
        ) : null}

        {controller.hasSearchedTrains ? (
          <>
            <DateWindowStrip
              dateWindowStart={controller.dateWindowStart}
              selectedDate={controller.searchDate}
              isBusy={controller.isBusy}
              onPrevious={controller.onPreviousDateWindow}
              onNext={controller.onNextDateWindow}
              onDateSelect={controller.handleDateSelect}
            />
            <TrainFilterBar currentSortMode={controller.trainSortMode} translate={translate} onSortModeChange={controller.setTrainSortMode} />
          </>
        ) : null}

        {controller.isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('trains.guest')}</p> : null}

        {controller.hasSearchedTrains ? (
          <TrainResultsSection
            currentLanguage={currentLanguage}
            isBusy={controller.isBusy}
            isGuestMode={controller.isGuestMode}
            searchFromStation={controller.searchFromStation}
            searchToStation={controller.searchToStation}
            trainResponses={controller.trainResponses}
            travelers={controller.travelers}
            selectedTravelerIds={controller.selectedTravelerIds}
            translate={translate}
            onToggleTravelerSelection={controller.toggleTravelerSelection}
            onRequireLogin={controller.onRequireLogin}
            onBookTrain={controller.onBookTrain}
            onLoadReviewSummary={controller.loadReviewSummary}
            onLoadReviews={controller.loadReviewsByResource}
          />
        ) : (
          <p className="text-sm leading-6 text-slate-500">{controller.searchRecommendation}</p>
        )}
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

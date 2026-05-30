import { AuthRequiredDialog } from '@/pages/shared/auth/AuthRequiredDialog'
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

        {controller.hasSearchedTrains ? <TrainFilterBar currentSortMode={controller.trainSortMode} translate={translate} onSortModeChange={controller.setTrainSortMode} /> : null}

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
            translate={translate}
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

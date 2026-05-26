import type { AppViewKey } from '@/lib/mvp-types/index'
import { SearchPanel, AppCard, PrimaryButton, SecondaryButton, SectionHeader, StatCard, ActionBar, EmptyState } from '@/app/ui/UIComponents'

type SmartTripPlannerPageProps = {
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export function SmartTripPlannerPage({ translate, onSelectView }: SmartTripPlannerPageProps) {
  return (
    <div className="grid gap-5">
      <AppCard>
        <SectionHeader
          eyebrow={translate('nav.section.workspace')}
          title={translate('smartPlanner.title')}
          subtitle={translate('smartPlanner.description')}
          actions={
            <ActionBar>
              <PrimaryButton type="button" onClick={() => onSelectView('flights')}>
                {translate('dashboard.searchFlights')}
              </PrimaryButton>
              <SecondaryButton type="button" onClick={() => onSelectView('orders')}>
                {translate('dashboard.viewOrders')}
              </SecondaryButton>
            </ActionBar>
          }
        />

        <div className="grid gap-4 md:grid-cols-3">
          <StatCard label={translate('smartPlanner.todo.signal')} value="01" detail={translate('smartPlanner.todo.signalDescription')} />
          <StatCard label={translate('smartPlanner.todo.bundle')} value="02" detail={translate('smartPlanner.todo.bundleDescription')} />
          <StatCard label={translate('smartPlanner.todo.timeline')} value="03" detail={translate('smartPlanner.todo.timelineDescription')} />
        </div>
      </AppCard>

      <SearchPanel
        label={translate('search.global.label')}
        actions={
          <>
            <SecondaryButton type="button" onClick={() => onSelectView('hotels')}>
              {translate('nav.hotels')}
            </SecondaryButton>
            <SecondaryButton type="button" onClick={() => onSelectView('trains')}>
              {translate('nav.trains')}
            </SecondaryButton>
            <SecondaryButton type="button" onClick={() => onSelectView('attractions')}>
              {translate('nav.attractions')}
            </SecondaryButton>
          </>
        }
      >
        <EmptyState
          title={translate('smartPlanner.placeholderTitle')}
          description={translate('smartPlanner.placeholderDescription')}
          action={
            <PrimaryButton type="button" onClick={() => onSelectView('orders')}>
              {translate('dashboard.openTripHub')}
            </PrimaryButton>
          }
        />
      </SearchPanel>
    </div>
  )
}

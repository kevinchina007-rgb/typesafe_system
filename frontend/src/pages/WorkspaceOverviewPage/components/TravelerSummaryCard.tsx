import { DashboardCard } from '@/pages/WorkspaceOverviewPage/components/DashboardCard'
import type { TravelerSummaryCardProps } from '@/pages/WorkspaceOverviewPage/components/types'

export function TravelerSummaryCard({ travelerCount, defaultTravelerName, translate, onSelectView }: TravelerSummaryCardProps) {
  return (
    <DashboardCard
      eyebrow={translate('nav.section.travelManagement')}
      title={translate('dashboard.travelers.title')}
      description={translate('dashboard.travelers.description')}
      action={
        <button type="button" className="secondary-button" onClick={() => onSelectView('travelers')}>
          {translate('dashboard.manageTravelers')}
        </button>
      }
    >
      <div className="detail-grid">
        <div>
          <span className="detail-label">{translate('dashboard.travelers.count')}</span>
          <strong>{travelerCount}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('dashboard.travelers.default')}</span>
          <strong>{defaultTravelerName ?? translate('dashboard.travelers.noDefault')}</strong>
        </div>
      </div>
    </DashboardCard>
  )
}


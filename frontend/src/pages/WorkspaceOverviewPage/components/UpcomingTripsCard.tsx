import { DashboardCard } from '@/pages/WorkspaceOverviewPage/components/DashboardCard'
import type { UpcomingTripsCardProps } from '@/pages/WorkspaceOverviewPage/components/types'

export function UpcomingTripsCard({ trips, translate, onSelectView }: UpcomingTripsCardProps) {
  return (
    <DashboardCard
      eyebrow={translate('nav.section.workspace')}
      title={translate('dashboard.upcomingTrips.title')}
      description={translate('dashboard.upcomingTrips.description')}
      action={
        <button type="button" className="secondary-button" onClick={() => onSelectView('orders')}>
          {translate('dashboard.openTripHub')}
        </button>
      }
    >
      {trips.length > 0 ? (
        <ul className="entity-list">
          {trips.map(trip => (
            <li key={trip.id}>
              <div>
                <strong>{trip.title}</strong>
                <p>{trip.subtitle}</p>
              </div>
              <span>{trip.startAtLabel}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="empty-state">{translate('dashboard.upcomingTrips.empty')}</p>
      )}
    </DashboardCard>
  )
}


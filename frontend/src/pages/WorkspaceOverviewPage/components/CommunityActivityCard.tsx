import { DashboardCard } from '@/pages/WorkspaceOverviewPage/components/DashboardCard'
import type { CommunityActivityCardProps } from '@/pages/WorkspaceOverviewPage/components/types'

export function CommunityActivityCard({ activities, translate, onSelectView }: CommunityActivityCardProps) {
  return (
    <DashboardCard
      eyebrow={translate('nav.section.community')}
      title={translate('dashboard.community.title')}
      description={translate('dashboard.community.description')}
      action={
        <div className="action-row">
          <button type="button" className="secondary-button" onClick={() => onSelectView('blog')}>
            {translate('nav.blog')}
          </button>
          <button type="button" className="secondary-button" onClick={() => onSelectView('reviews')}>
            {translate('nav.reviews')}
          </button>
        </div>
      }
    >
      {activities.length > 0 ? (
        <ul className="entity-list">
          {activities.map(activity => (
            <li key={activity.id}>
              <div>
                <strong>{activity.title}</strong>
                <p>{activity.meta}</p>
              </div>
              <span className="tag-chip">{activity.kind}</span>
            </li>
          ))}
        </ul>
      ) : (
        <p className="empty-state">{translate('dashboard.community.empty')}</p>
      )}
    </DashboardCard>
  )
}


import { DashboardCard } from '@/pages/WorkspaceOverviewPage/components/DashboardCard'
import type { AccountSummaryCardProps } from '@/pages/WorkspaceOverviewPage/components/types'

export function AccountSummaryCard({ signedInUser, translate, onSelectView }: AccountSummaryCardProps) {
  const isReady = signedInUser !== null

  return (
    <DashboardCard
      eyebrow={translate('nav.section.profile')}
      title={translate('dashboard.account.title')}
      description={translate('dashboard.account.description')}
      action={
        <button type="button" className="secondary-button" onClick={() => onSelectView('account')}>
          {translate(isReady ? 'dashboard.openAccount' : 'dashboard.completeAccount')}
        </button>
      }
    >
      {signedInUser ? (
        <div className="detail-grid">
          <div>
            <span className="detail-label">{translate('account.nickname')}</span>
            <strong>{signedInUser.nickname}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.email')}</span>
            <strong>{signedInUser.email}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.membership')}</span>
            <strong>{signedInUser.membershipLevel}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.points')}</span>
            <strong>{signedInUser.points}</strong>
          </div>
        </div>
      ) : (
        <p className="empty-state">{translate('dashboard.account.empty')}</p>
      )}
    </DashboardCard>
  )
}


import { DashboardCard } from '@/pages/WorkspaceOverviewPage/components/DashboardCard'
import type { RecommendedNextStepCardProps } from '@/pages/WorkspaceOverviewPage/components/types'

export function RecommendedNextStepCard({ isSessionReady, recommendation, translate, onSelectView }: RecommendedNextStepCardProps) {
  return (
    <DashboardCard
      eyebrow={translate('dashboard.nextStep.eyebrow')}
      title={translate('dashboard.nextStep.title')}
      description={recommendation.description}
      action={isSessionReady && recommendation.viewKey !== 'blog' ? (
        <button type="button" onClick={() => onSelectView(recommendation.viewKey)}>
          {recommendation.ctaLabel}
        </button>
      ) : undefined}
    >
      <div className="recommended-next-step-card">
        <strong>{recommendation.title}</strong>
      </div>
    </DashboardCard>
  )
}


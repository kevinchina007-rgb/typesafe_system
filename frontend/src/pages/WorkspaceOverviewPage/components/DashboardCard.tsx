import type { ReactNode } from 'react'

import { AppCard, SectionHeader } from '@/app/ui/UIComponents'

type DashboardCardProps = {
  eyebrow: string
  title: string
  description?: string
  action?: ReactNode
  children: ReactNode
}

export function DashboardCard({ eyebrow, title, description, action, children }: DashboardCardProps) {
  return (
    <AppCard className="dashboard-card workspace-overview-dashboard-card">
      <div className="workspace-overview-dashboard-card-body">
        <SectionHeader eyebrow={eyebrow} title={title} subtitle={description} actions={action} />
        {children}
      </div>
    </AppCard>
  )
}

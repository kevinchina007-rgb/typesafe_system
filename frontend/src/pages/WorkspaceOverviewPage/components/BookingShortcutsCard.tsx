import { DashboardCard } from '@/pages/WorkspaceOverviewPage/components/DashboardCard'
import type { BookingShortcutsCardProps } from '@/pages/WorkspaceOverviewPage/components/types'

export function BookingShortcutsCard({ shortcuts, translate, onSelectView }: BookingShortcutsCardProps) {
  return (
    <DashboardCard
      eyebrow={translate('nav.section.booking')}
      title={translate('dashboard.shortcuts.title')}
      description={translate('dashboard.shortcuts.description')}
    >
      <div className="dashboard-shortcuts-grid">
        {shortcuts.map(shortcut => (
          <button
            key={shortcut.viewKey}
            type="button"
            className="dashboard-shortcut-button secondary-button"
            onClick={() => onSelectView(shortcut.viewKey)}
          >
            <span>{shortcut.title}</span>
            <small>{shortcut.description}</small>
          </button>
        ))}
      </div>
    </DashboardCard>
  )
}


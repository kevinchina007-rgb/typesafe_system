import type { AppViewKey } from '@/lib/mvp-types/index'
import { ActionBar, AppCard, PrimaryButton, SectionHeader } from '@/app/ui/UIComponents'

type AppTopToolbarProps = {
  eyebrow: string
  title: string
  description: string
  statusText: string
  primaryAction?: {
    label: string
    viewKey: AppViewKey
  }
  onSelectView: (viewKey: AppViewKey) => void
}

export function AppTopToolbar({
  eyebrow,
  title,
  description: _description,
  statusText: _statusText,
  primaryAction,
  onSelectView,
}: AppTopToolbarProps) {
  return (
    <AppCard className="app-top-toolbar">
      <SectionHeader
        eyebrow={eyebrow}
        title={title}
      />

      {primaryAction ? (
        <ActionBar>
          <PrimaryButton type="button" onClick={() => onSelectView(primaryAction.viewKey)}>
            {primaryAction.label}
          </PrimaryButton>
        </ActionBar>
      ) : null}
    </AppCard>
  )
}

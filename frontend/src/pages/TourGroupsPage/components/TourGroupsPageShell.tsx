import { TourGroupsPanel } from './TourGroupsPanel'
import type { TourGroupsPanelCommonProps } from '../objects'

type TourGroupsPageShellProps = {
  controller: TourGroupsPanelCommonProps
}

export function TourGroupsPageShell({ controller }: TourGroupsPageShellProps) {
  return (
    <section className="grid gap-4 px-6 pb-8 pt-6">
      <TourGroupsPanel {...controller} />
    </section>
  )
}

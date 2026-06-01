import { TourGroupsPanel } from './TourGroupsPanel'
import type { TourGroupsPanelCommonProps } from '../objects'

type TourGroupsPageShellProps = {
  controller: TourGroupsPanelCommonProps
}

export function TourGroupsPageShell({ controller }: TourGroupsPageShellProps) {
  return (
    <section className="grid justify-start px-6 pb-8 pt-6">
      <div className="grid w-full max-w-[112rem] justify-start gap-4">
        <TourGroupsPanel {...controller} />
      </div>
    </section>
  )
}

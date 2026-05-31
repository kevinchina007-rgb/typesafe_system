import { TravelerPanel } from './TravelerPanel'
import type { TravelersPageController } from '../objects'

type TravelersPageShellProps = {
  controller: TravelersPageController
}

export function TravelersPageShell({ controller }: TravelersPageShellProps) {
  return (
    <section className="grid gap-5 bg-slate-50 px-6 pb-8 pt-6 text-slate-950">
      <TravelerPanel
        currentLanguage={controller.currentLanguage}
        isBusy={controller.isBusy}
        isGuestMode={controller.isGuestMode}
        travelers={controller.travelers}
        translate={controller.translate}
        onCreateTraveler={controller.onCreateTraveler}
        onUpdateTraveler={controller.onUpdateTraveler}
        onSetDefaultTraveler={controller.onSetDefaultTraveler}
        onDeleteTraveler={controller.onDeleteTraveler}
        onReloadTravelers={controller.onReloadTravelers}
      />
    </section>
  )
}

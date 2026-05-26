import { ManagerPanelAuth } from '@/pages/ManagerPage/components/managers/manager-panel-auth'
import { ManagerPanelWorkspace } from '@/pages/ManagerPage/components/managers/manager-panel-workspace'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'

export function ManagerPanel({
  managerSession,
  isBusy,
  translate,
  ...rest
}: ManagerPanelProps) {
  return (
    <section className="bg-white text-slate-950">
      {managerSession ? (
        <ManagerPanelWorkspace
          {...rest}
          isBusy={isBusy}
          managerSession={managerSession}
          translate={translate}
        />
      ) : (
        <ManagerPanelAuth
          isBusy={isBusy}
          translate={translate}
          onRegisterAirlineManager={rest.onRegisterAirlineManager}
          onRegisterHotelManager={rest.onRegisterHotelManager}
          onLoginManager={rest.onLoginManager}
          onValidationError={rest.onValidationError}
        />
      )}
    </section>
  )
}

import { ManagerPanelAuth } from '@/pages/ManagerPage/components/managers/manager-panel-auth'
import { ManagerPanelWorkspace } from '@/pages/ManagerPage/components/managers/manager-panel-workspace'
import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'

export function ManagerPanel({
  managerSession,
  isBusy,
  translate,
  onLogoutManager,
  ...rest
}: ManagerPanelProps) {
  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.manager')}</p>
          <h2>{translate('manager.title')}</h2>
        </div>
        {managerSession ? (
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onLogoutManager}>
            {translate('manager.logout')}
          </button>
        ) : null}
      </div>

      <p className="hero-copy">{translate('manager.description')}</p>

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

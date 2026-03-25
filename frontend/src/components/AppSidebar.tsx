import type { AppLanguage, AppViewKey, HealthResponse, UserResponse } from '../lib/mvp-types'

type AppSidebarProps = {
  currentLanguage: AppLanguage
  currentViewKey: AppViewKey
  health: HealthResponse | null
  signedInUser: UserResponse | null
  onChangeLanguage: (language: AppLanguage) => void
  onSelectView: (viewKey: AppViewKey) => void
  translate: (translationKey: string) => string
}

const sidebarViews: AppViewKey[] = ['explore', 'account', 'travelers', 'bookings']

export function AppSidebar({
  currentLanguage,
  currentViewKey,
  health,
  signedInUser,
  onChangeLanguage,
  onSelectView,
  translate,
}: AppSidebarProps) {
  return (
    <aside className="app-sidebar">
      <div className="sidebar-topbar">
        <div>
          <p className="eyebrow-label">Scala Travel MVP</p>
          <h1>{translate('app.title')}</h1>
        </div>
        <label className="language-switcher">
          <span>{translate('language.label')}</span>
          <select
            value={currentLanguage}
            onChange={event => onChangeLanguage(event.target.value as AppLanguage)}
          >
            <option value="en">English</option>
            <option value="zh">中文</option>
          </select>
        </label>
      </div>

      <p className="sidebar-copy">{translate('app.subtitle')}</p>

      <div className="status-panel sidebar-status-panel">
        <span className={`health-pill ${health?.status === 'ok' ? 'is-healthy' : ''}`}>
          {health ? `${translate('status.online')} · ${health.backendPort}` : translate('status.offline')}
        </span>
        <p>{signedInUser ? `${signedInUser.nickname} · ${signedInUser.email}` : translate('guest.badge')}</p>
      </div>

      <nav className="sidebar-nav">
        {sidebarViews.map(viewKey => (
          <button
            type="button"
            key={viewKey}
            className={`sidebar-nav-button ${currentViewKey === viewKey ? 'is-active' : ''}`}
            onClick={() => onSelectView(viewKey)}
          >
            {translate(`nav.${viewKey}`)}
          </button>
        ))}
      </nav>
    </aside>
  )
}

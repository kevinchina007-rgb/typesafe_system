import type { AppLanguage, AppViewKey, CurrentManagerSessionResponse, UserResponse } from '../../lib/mvp-types'
import type { TopNavItem, TopNavKey } from '../../app/navigation'
import { BackendAssetImage } from '../BackendAssetImage'
import { TopNavButton } from './TopNavButton'

type ThemeMode = 'dark' | 'light'

type TopNavBarProps = {
  currentLanguage: AppLanguage
  currentTopNav: TopNavKey
  themeMode: ThemeMode
  signedInManager: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  items: TopNavItem[]
  onChangeLanguage: (language: AppLanguage) => void
  onChangeTheme: (themeMode: ThemeMode) => void
  onSelectTopNav: (topNav: TopNavKey, defaultViewKey: AppViewKey) => void
  translate: (translationKey: string) => string
}

function getInitials(label: string) {
  return label.slice(0, 2).toUpperCase()
}

export function TopNavBar({
  currentLanguage,
  currentTopNav,
  themeMode,
  signedInManager,
  signedInUser,
  items,
  onChangeLanguage,
  onChangeTheme,
  onSelectTopNav,
  translate,
}: TopNavBarProps) {
  const identityLabel = signedInUser?.nickname ?? signedInManager?.displayName ?? translate('guest.badge')
  const avatarUrl = signedInUser?.avatarUrl ?? null

  return (
    <header className="top-nav-shell app-card">
      <div className="top-nav-brand">
        <div>
          <p className="eyebrow-label">{translate('app.eyebrow')}</p>
          <h1 className="top-nav-brand-title">{translate('app.title')}</h1>
        </div>
      </div>

      <nav className="top-nav-links" aria-label={translate('topnav.aria')}>
        {items.map(item => (
          <TopNavButton
            key={item.key}
            badgeCount={item.badgeCount}
            icon={item.icon}
            isActive={currentTopNav === item.key}
            label={translate(item.titleKey)}
            targetViewKey={item.defaultViewKey}
            onSelect={viewKey => onSelectTopNav(item.key, viewKey)}
          />
        ))}
      </nav>

      <div className="top-nav-meta">
        <div className="top-nav-toggle-group" role="group" aria-label={translate('theme.label')}>
          <button
            type="button"
            className={`top-nav-utility-button ${themeMode === 'dark' ? 'is-active' : ''}`}
            onClick={() => onChangeTheme('dark')}
          >
            {translate('theme.dark')}
          </button>
          <button
            type="button"
            className={`top-nav-utility-button ${themeMode === 'light' ? 'is-active' : ''}`}
            onClick={() => onChangeTheme('light')}
          >
            {translate('theme.light')}
          </button>
        </div>

        <div className="top-nav-user">
          {avatarUrl ? (
            <BackendAssetImage
              className="top-nav-avatar-image"
              assetUrl={avatarUrl}
              alt={identityLabel}
              fallbackContent={getInitials(identityLabel)}
            />
          ) : (
            <span className="top-nav-avatar-fallback">{getInitials(identityLabel)}</span>
          )}
          <span>{identityLabel}</span>
        </div>

        <div className="top-nav-toggle-group" role="group" aria-label={translate('language.label')}>
          <button
            type="button"
            className={`top-nav-utility-button ${currentLanguage === 'zh' ? 'is-active' : ''}`}
            onClick={() => onChangeLanguage('zh')}
          >
            {translate('language.zh')}
          </button>
          <button
            type="button"
            className={`top-nav-utility-button ${currentLanguage === 'en' ? 'is-active' : ''}`}
            onClick={() => onChangeLanguage('en')}
          >
            {translate('language.en')}
          </button>
        </div>
      </div>
    </header>
  )
}

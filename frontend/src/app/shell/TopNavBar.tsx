import type { AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import type { NavItem, TopNavItem, TopNavKey } from '@/app/navigation'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { TopNavButton } from '@/app/shell/TopNavButton'

type TopNavBarProps = {
  currentTopNav: TopNavKey
  signedInManager: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  items: TopNavItem[]
  submenuItemsByTopNav: Record<string, NavItem[]>
  currentViewKey: AppViewKey
  onSelectTopNav: (topNav: TopNavKey, defaultViewKey: AppViewKey) => void
  onSelectView: (viewKey: AppViewKey) => void
  translate: (translationKey: string) => string
  isOverlay?: boolean
}

function getInitials(label: string) {
  return label.slice(0, 2).toUpperCase()
}

export function TopNavBar({
  currentTopNav,
  signedInManager,
  signedInUser,
  items,
  submenuItemsByTopNav,
  currentViewKey,
  onSelectTopNav,
  onSelectView,
  translate,
  isOverlay = false,
}: TopNavBarProps) {
  const identityLabel = signedInUser?.nickname ?? signedInManager?.displayName ?? translate('guest.badge')
  const avatarUrl = signedInUser?.avatarUrl ?? null

  return (
    <header className={isOverlay
      ? 'absolute inset-x-0 top-0 z-20 flex h-28 items-center justify-between px-8 text-white md:px-14'
      : 'z-20 flex h-28 items-center justify-between border-b border-slate-950 bg-white px-6 text-slate-950 shadow-none md:px-14'
    }>
      <div className="grid min-w-0">
        <img className="h-18 w-auto max-w-[24rem] object-contain object-left md:h-20 md:max-w-[26rem]" src="/images/fly-pig-logo.png" alt="fly pig" />
      </div>

      <nav
        className="flex items-center gap-3"
        aria-label={translate('topnav.aria')}
      >
        {items.map(item => (
          <div key={item.key} className="group relative">
            <TopNavButton
              badgeCount={item.badgeCount}
              icon={item.icon}
              isActive={currentTopNav === item.key}
              label={translate(item.titleKey)}
              targetViewKey={item.defaultViewKey}
              onSelect={viewKey => onSelectTopNav(item.key, viewKey)}
              isOverlay={isOverlay}
            />
            {(submenuItemsByTopNav[item.key]?.length ?? 0) > 1 ? (
              <>
                <div className="absolute left-1/2 top-full z-30 hidden h-3 min-w-48 -translate-x-1/2 group-hover:block" />
                <div className={isOverlay
                  ? 'absolute left-1/2 top-[calc(100%+0.65rem)] z-30 hidden min-w-48 -translate-x-1/2 bg-white/95 py-4 shadow-xl backdrop-blur-xl group-hover:grid'
                  : 'absolute left-1/2 top-[calc(100%+0.65rem)] z-30 hidden min-w-48 -translate-x-1/2 bg-black py-4 shadow-xl group-hover:grid'
                }>
                  {submenuItemsByTopNav[item.key].map(submenuItem => (
                    <button
                      key={submenuItem.viewKey}
                      type="button"
                      className={isOverlay
                        ? `w-full justify-center !border-0 ![background:transparent] px-4 py-3 !text-slate-900 !shadow-none ![transform:none] hover:![background:rgba(15,23,42,0.06)] hover:!text-slate-500 hover:![box-shadow:none] hover:![transform:none] ${
                            currentViewKey === submenuItem.viewKey ? 'font-medium' : ''
                          }`
                        : `w-full justify-center !border-0 ![background:transparent] px-4 py-3 !text-white !shadow-none ![transform:none] hover:![background:rgba(255,255,255,0.12)] hover:!text-white hover:![box-shadow:none] hover:![transform:none] ${
                            currentViewKey === submenuItem.viewKey ? 'font-semibold' : ''
                          }`
                      }
                      onClick={() => onSelectView(submenuItem.viewKey)}
                    >
                      {translate(submenuItem.titleKey)}
                    </button>
                  ))}
                </div>
              </>
            ) : null}
          </div>
        ))}
      </nav>

      <div className={isOverlay ? 'flex items-center' : 'flex items-center'}>
        <div className={isOverlay
          ? 'inline-flex items-center gap-3 rounded-none border border-white/25 bg-white/10 px-3 py-2 text-white'
          : 'inline-flex items-center gap-3 border border-slate-200 bg-white px-3 py-2 text-slate-500'
        }>
          {avatarUrl ? (
            <BackendAssetImage
              className="inline-flex h-10 w-10 items-center justify-center rounded-full object-cover"
              assetUrl={avatarUrl}
              alt={identityLabel}
              fallbackContent={getInitials(identityLabel)}
            />
          ) : (
            <span className="inline-flex h-10 w-10 items-center justify-center rounded-full bg-indigo-100 text-slate-700">
              {getInitials(identityLabel)}
            </span>
          )}
          <span>{identityLabel}</span>
        </div>
      </div>
    </header>
  )
}

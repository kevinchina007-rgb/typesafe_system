import type { TopNavLinksProps } from '@/app/shell/TopNavBar.types'

import { TopNavButton } from '@/app/shell/TopNavButton'

export function TopNavLinks({
  currentTopNav,
  currentViewKey,
  items,
  submenuItemsByTopNav,
  hoveredTopNav,
  setHoveredTopNav,
  onSelectTopNav,
  onSelectView,
  translate,
  isOverlay = false,
}: TopNavLinksProps) {
  return (
    <nav className="flex items-center gap-3" aria-label={translate('topnav.aria')}>
      {items.map(item => (
        <div
          key={item.key}
          className="relative"
          onMouseEnter={() => setHoveredTopNav(item.key)}
          onMouseLeave={() => setHoveredTopNav(current => (current === item.key ? null : current))}
        >
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
              <div className={hoveredTopNav === item.key ? 'absolute left-1/2 top-full z-50 h-3 min-w-48 -translate-x-1/2' : 'hidden'} />
              <div
                className={
                  hoveredTopNav === item.key
                    ? isOverlay
                      ? 'absolute left-1/2 top-[calc(100%+0.65rem)] z-50 min-w-48 -translate-x-1/2 bg-white/95 py-4 shadow-xl backdrop-blur-xl'
                      : 'absolute left-1/2 top-[calc(100%+0.65rem)] z-50 min-w-48 -translate-x-1/2 bg-black py-4 shadow-xl'
                    : 'hidden'
                }
              >
                {submenuItemsByTopNav[item.key].map(submenuItem => (
                  <button
                    key={submenuItem.viewKey}
                    type="button"
                    className={
                      isOverlay
                        ? `w-full justify-center !border-0 ![background:transparent] px-4 py-3 !text-slate-900 !shadow-none ![transform:none] hover:![background:rgba(15,23,42,0.06)] ${currentViewKey === submenuItem.viewKey ? 'font-medium' : ''}`
                        : `w-full justify-center !border-0 ![background:transparent] px-4 py-3 !text-white !shadow-none ![transform:none] hover:![background:rgba(255,255,255,0.12)] ${currentViewKey === submenuItem.viewKey ? 'font-semibold' : ''}`
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
  )
}

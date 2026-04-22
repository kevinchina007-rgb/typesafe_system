import type { NavItem, TopNavKey } from '../../app/navigation'
import { useNavigationUiStore } from '../../app/stores/navigation-ui-store'
import type { AppViewKey } from '../../lib/mvp-types'
import { Icon } from '../icons/Icons'

type SidebarProps = {
  currentTopNav: TopNavKey
  currentViewKey: AppViewKey
  items: NavItem[]
  onSelectView: (viewKey: AppViewKey) => void
  translate: (translationKey: string) => string
}

export function Sidebar({ currentTopNav, currentViewKey, items, onSelectView, translate }: SidebarProps) {
  const isCollapsed = useNavigationUiStore(state => state.isSidebarCollapsed)
  const toggleSidebarCollapsed = useNavigationUiStore(state => state.toggleSidebarCollapsed)

  return (
    <aside className={`context-sidebar app-card ${isCollapsed ? 'is-collapsed' : ''}`}>
      <div className="context-sidebar-header">
        <div>
          <p className="eyebrow-label">{translate(`topnav.${currentTopNav}`)}</p>
        </div>
        <button
          type="button"
          className="secondary-button context-sidebar-toggle"
          onClick={toggleSidebarCollapsed}
        >
          {translate(isCollapsed ? 'sidebar.expand' : 'sidebar.collapse')}
        </button>
      </div>

      {!isCollapsed ? (
        <nav className="sidebar-nav">
          {items.map(item => (
            <button
              type="button"
              key={item.viewKey}
              className={`sidebar-nav-button ${currentViewKey === item.viewKey ? 'is-active' : ''}`}
              onClick={() => onSelectView(item.viewKey)}
            >
              <span className="sidebar-nav-icon">
                <Icon icon={item.icon} size={18} />
              </span>
              <span className="sidebar-nav-labels">
                <strong>{translate(item.titleKey)}</strong>
              </span>
              {item.badgeCount ? <span className="sidebar-nav-badge">{item.badgeCount}</span> : null}
            </button>
          ))}
        </nav>
      ) : null}
    </aside>
  )
}

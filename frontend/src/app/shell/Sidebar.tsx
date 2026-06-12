// 本文件定义侧边栏，负责模块切换、导航分组和当前模块高亮。

import type { NavItem, TopNavKey } from '@/app/navigation'
import { useNavigationUiStore } from '@/app/stores/navigation-ui-store'
import type { AppViewKey } from '@/lib/mvp-types/index'
import { Icon } from '@/app/icons/Icons'

type SidebarProps = {
  currentTopNav: TopNavKey
  currentViewKey: AppViewKey
  items: NavItem[]
  onSelectView: (viewKey: AppViewKey) => void
  translate: (translationKey: string) => string
}

// 侧边栏负责当前一级导航下的二级菜单展示和折叠控制。
export function Sidebar({ currentTopNav, currentViewKey, items, onSelectView, translate }: SidebarProps) {
  // 从导航 UI 状态里读取侧边栏是否折叠。
  const isCollapsed = useNavigationUiStore(state => state.isSidebarCollapsed)
  // 切换侧边栏折叠状态，供按钮直接调用。
  const toggleSidebarCollapsed = useNavigationUiStore(state => state.toggleSidebarCollapsed)

  return (
    <aside className={`grid gap-4 border border-slate-200 bg-white p-4 text-slate-950 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 ${isCollapsed ? 'hidden' : ''}`}>
      <div className="flex items-center justify-between gap-3">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate(`topnav.${currentTopNav}`)}</p>
        </div>
        <button
          type="button"
          className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          onClick={toggleSidebarCollapsed}
        >
          {translate(isCollapsed ? 'sidebar.expand' : 'sidebar.collapse')}
        </button>
      </div>

      {!isCollapsed ? (
        <nav className="grid gap-2">
          {items.map(item => (
            <button
              type="button"
              key={item.viewKey}
              className={`grid min-h-12 grid-cols-[auto_1fr_auto] items-center gap-3 border border-transparent bg-white px-3 py-2 text-left text-slate-700 transition hover:border-black hover:bg-black hover:text-white ${currentViewKey === item.viewKey ? 'border-black bg-black text-white' : ''}`}
              onClick={() => onSelectView(item.viewKey)}
            >
              <span className="grid place-items-center">
                <Icon icon={item.icon} size={18} />
              </span>
              <span className="grid gap-1">
                <strong>{translate(item.titleKey)}</strong>
              </span>
              {item.badgeCount ? <span className="inline-flex min-h-6 min-w-6 items-center justify-center bg-slate-950 px-2 text-xs font-bold text-white">{item.badgeCount}</span> : null}
            </button>
          ))}
        </nav>
      ) : null}
    </aside>
  )
}

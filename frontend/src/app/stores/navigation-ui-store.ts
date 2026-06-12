// 本文件封装状态管理逻辑。

import { create } from 'zustand'
import { persist } from 'zustand/middleware'

type NavigationUiStoreState = {
  isSidebarCollapsed: boolean
}

type NavigationUiStoreActions = {
  setSidebarCollapsed: (isSidebarCollapsed: boolean) => void
  toggleSidebarCollapsed: () => void
}

type NavigationUiStore = NavigationUiStoreState & NavigationUiStoreActions

const navigationUiStoreDefaultState: NavigationUiStoreState = {
  isSidebarCollapsed: false,
}

// 导航 UI 状态仓库，负责保存侧边栏是否收起。
export const useNavigationUiStore = create<NavigationUiStore>()(
  persist(
    set => ({
      ...navigationUiStoreDefaultState,
      setSidebarCollapsed: isSidebarCollapsed => set({ isSidebarCollapsed }),
      toggleSidebarCollapsed: () => set(state => ({ isSidebarCollapsed: !state.isSidebarCollapsed })),
    }),
    {
      name: 'travel-workbench.navigation-ui',
      partialize: state => ({
        isSidebarCollapsed: state.isSidebarCollapsed,
      }),
    }
  )
)

// 读取导航 UI 状态快照，供非 React 场景读取。
export function getNavigationUiSnap(): NavigationUiStoreState {
  const { isSidebarCollapsed } = useNavigationUiStore.getState()
  return { isSidebarCollapsed }
}

// 直接设置侧边栏是否收起。
export function setSidebarCollapsed(isSidebarCollapsed: boolean) {
  useNavigationUiStore.getState().setSidebarCollapsed(isSidebarCollapsed)
}

// 切换侧边栏收起状态。
export function toggleSidebarCollapsed() {
  useNavigationUiStore.getState().toggleSidebarCollapsed()
}

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

export function getNavigationUiSnap(): NavigationUiStoreState {
  const { isSidebarCollapsed } = useNavigationUiStore.getState()
  return { isSidebarCollapsed }
}

export function setSidebarCollapsed(isSidebarCollapsed: boolean) {
  useNavigationUiStore.getState().setSidebarCollapsed(isSidebarCollapsed)
}

export function toggleSidebarCollapsed() {
  useNavigationUiStore.getState().toggleSidebarCollapsed()
}

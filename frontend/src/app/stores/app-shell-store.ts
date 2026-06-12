// 本文件封装状态管理逻辑。

import { create } from 'zustand'
import { persist } from 'zustand/middleware'

import type { AppLanguage, AppNotice, AppViewKey } from '@/lib/mvp-types/index'

const appViewKeys: AppViewKey[] = [
  'overview',
  'smartPlanner',
  'orders',
  'flightOrders',
  'hotelOrders',
  'trainOrders',
  'attractionOrders',
  'blog',
  'reviews',
  'customerFeedback',
  'explore',
  'account',
  'travelers',
  'flights',
  'hotels',
  'trains',
  'attractions',
  'tourGroups',
  'tourGroupPlanBuilder',
  'bookings',
  'manager',
  'siteAdminLogin',
  'managerWorkspace',
  'managerCreateFlight',
  'managerFlightManagement',
  'managerFeedback',
  'managerProfile',
  'managerAdvertising',
  'siteAdminBlogAudit',
  'siteAdminAdvertisingReview',
  'siteAdminHotelAdvertisingReview',
  'siteAdminTrainAdvertisingReview',
  'siteAdminAttractionAdvertisingReview',
  'siteAdminFeedback',
  'trainAdmin',
  'attractionAdmin',
]

function isAppViewKey(value: string | null): value is AppViewKey {
  return value !== null && appViewKeys.includes(value as AppViewKey)
}

function getInitialViewFromUrl(): AppViewKey | null {
  if (typeof window === 'undefined') {
    return null
  }

  const viewParam = new URLSearchParams(window.location.search).get('view')
  return isAppViewKey(viewParam) ? viewParam : null
}

function syncUrlView(currentViewKey: AppViewKey) {
  if (typeof window === 'undefined') {
    return
  }

  const nextUrl = new URL(window.location.href)
  nextUrl.searchParams.set('view', currentViewKey)
  window.history.replaceState(window.history.state, '', nextUrl)
}

type AppShellStoreState = {
  currentLanguage: AppLanguage
  currentViewKey: AppViewKey
  currentNotice: AppNotice | null
}

type AppShellStoreActions = {
  setCurrentViewKey: (viewKey: AppViewKey) => void
  setCurrentNotice: (notice: AppNotice | null) => void
  clearCurrentNotice: () => void
}

type AppShellStore = AppShellStoreState & AppShellStoreActions

const appShellStoreDefaultState: AppShellStoreState = {
  currentLanguage: 'zh',
  currentViewKey: getInitialViewFromUrl() ?? 'overview',
  currentNotice: null,
}

export const useAppShellStore = create<AppShellStore>()(
  persist(
    set => ({
      ...appShellStoreDefaultState,
      setCurrentViewKey: currentViewKey => {
        syncUrlView(currentViewKey)
        set({ currentViewKey })
      },
      setCurrentNotice: currentNotice => set({ currentNotice }),
      clearCurrentNotice: () => set({ currentNotice: null }),
    }),
    {
      name: 'travel-workbench.app-shell',
      partialize: state => ({
        currentViewKey: state.currentViewKey,
      }),
      merge: (persistedState, currentState) => {
        const urlViewKey = getInitialViewFromUrl()
        return {
          ...currentState,
          ...(persistedState as Partial<AppShellStoreState>),
          currentLanguage: 'zh',
          ...(urlViewKey ? { currentViewKey: urlViewKey } : {}),
        }
      },
    }
  )
)

export function getAppShellSnap(): AppShellStoreState {
  const { currentLanguage, currentViewKey, currentNotice } = useAppShellStore.getState()
  return { currentLanguage, currentViewKey, currentNotice }
}

export function setAppView(currentViewKey: AppViewKey) {
  useAppShellStore.getState().setCurrentViewKey(currentViewKey)
}

export function setAppNotice(currentNotice: AppNotice | null) {
  useAppShellStore.getState().setCurrentNotice(currentNotice)
}

export function clearAppNotice() {
  useAppShellStore.getState().clearCurrentNotice()
}

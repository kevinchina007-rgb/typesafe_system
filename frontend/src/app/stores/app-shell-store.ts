import { create } from 'zustand'
import { persist } from 'zustand/middleware'

import type { AppLanguage, AppNotice, AppViewKey } from '@/lib/mvp-types/index'

export type ThemeMode = 'dark' | 'light'

type AppShellStoreState = {
  currentLanguage: AppLanguage
  currentViewKey: AppViewKey
  themeMode: ThemeMode
  currentNotice: AppNotice | null
}

type AppShellStoreActions = {
  setCurrentLanguage: (language: AppLanguage) => void
  setCurrentViewKey: (viewKey: AppViewKey) => void
  setThemeMode: (themeMode: ThemeMode) => void
  setCurrentNotice: (notice: AppNotice | null) => void
  clearCurrentNotice: () => void
}

type AppShellStore = AppShellStoreState & AppShellStoreActions

const appShellStoreDefaultState: AppShellStoreState = {
  currentLanguage: 'en',
  currentViewKey: 'overview',
  themeMode: 'dark',
  currentNotice: null,
}

export const useAppShellStore = create<AppShellStore>()(
  persist(
    set => ({
      ...appShellStoreDefaultState,
      setCurrentLanguage: currentLanguage => set({ currentLanguage }),
      setCurrentViewKey: currentViewKey => set({ currentViewKey }),
      setThemeMode: themeMode => set({ themeMode }),
      setCurrentNotice: currentNotice => set({ currentNotice }),
      clearCurrentNotice: () => set({ currentNotice: null }),
    }),
    {
      name: 'travel-workbench.app-shell',
      partialize: state => ({
        currentLanguage: state.currentLanguage,
        currentViewKey: state.currentViewKey,
        themeMode: state.themeMode,
      }),
    }
  )
)

export function getAppShellSnap(): AppShellStoreState {
  const { currentLanguage, currentViewKey, themeMode, currentNotice } = useAppShellStore.getState()
  return { currentLanguage, currentViewKey, themeMode, currentNotice }
}

export function setAppLanguage(currentLanguage: AppLanguage) {
  useAppShellStore.getState().setCurrentLanguage(currentLanguage)
}

export function setAppView(currentViewKey: AppViewKey) {
  useAppShellStore.getState().setCurrentViewKey(currentViewKey)
}

export function setAppTheme(themeMode: ThemeMode) {
  useAppShellStore.getState().setThemeMode(themeMode)
}

export function setAppNotice(currentNotice: AppNotice | null) {
  useAppShellStore.getState().setCurrentNotice(currentNotice)
}

export function clearAppNotice() {
  useAppShellStore.getState().clearCurrentNotice()
}

import { useCallback, useEffect, useRef, useState } from 'react'

import { getActiveTopNav, getSidebarItemsForTopNav, getVisibleTopNavItems, shouldShowSidebar } from '../app/navigation'
import { AppShell } from '../components/shell/AppShell'
import { ToastNotice } from '../components/ToastNotice'
import { travelMvpApiClient } from '../lib/api-client'
import { isUnauthorizedApiError } from '../lib/api-transport'
import { createTranslator } from '../lib/i18n'
import type { AppLanguage, AppNotice, AppViewKey, CurrentManagerSessionResponse, HealthResponse, UserResponse } from '../lib/mvp-types'
import { getInitialBackendHealth } from '../lib/runtime-config'
import { AccountPage } from './AccountPage'
import { AttractionsPage } from './AttractionsPage'
import { BlogPage } from './BlogPage'
import { BookingsPage } from './BookingsPage'
import { FlightsPage } from './FlightsPage'
import { HotelsPage } from './HotelsPage'
import { ManagerPage } from './ManagerPage'
import { ReviewsPage } from './ReviewsPage'
import { SmartTripPlannerPage } from './SmartTripPlannerPage'
import { TourGroupsPage } from './TourGroupsPage'
import { TrainsPage } from './TrainsPage'
import { TravelersPage } from './TravelersPage'
import { WorkspaceOverviewPage } from './WorkspaceOverviewPage'

type ThemeMode = 'dark' | 'light'

const languageStorageKey = 'travel-workbench.language'
const viewStorageKey = 'travel-workbench.view'
const themeStorageKey = 'travel-workbench.theme'

function normalizeViewKey(viewKey: AppViewKey): AppViewKey {
  if (viewKey === 'bookings') {
    return 'orders'
  }

  if (viewKey === 'explore') {
    return 'smartPlanner'
  }

  if (viewKey === 'trainAdmin' || viewKey === 'attractionAdmin') {
    return 'manager'
  }

  return viewKey
}

export function MvpApp() {
  const [currentLanguage, setCurrentLanguage] = useState<AppLanguage>(() => {
    if (typeof window === 'undefined') {
      return 'en'
    }

    const savedLanguage = window.localStorage.getItem(languageStorageKey)
    return savedLanguage === 'zh' ? 'zh' : 'en'
  })
  const [currentViewKey, setCurrentViewKey] = useState<AppViewKey>(() => {
    if (typeof window === 'undefined') {
      return 'overview'
    }

    const savedView = window.localStorage.getItem(viewStorageKey)
    return (savedView as AppViewKey) ?? 'overview'
  })
  const [themeMode, setThemeMode] = useState<ThemeMode>(() => {
    if (typeof window === 'undefined') {
      return 'dark'
    }

    return window.localStorage.getItem(themeStorageKey) === 'light' ? 'light' : 'dark'
  })
  const [accountEntryMode] = useState<'register' | 'login'>('login')
  const [, setBackendHealthResponse] = useState<HealthResponse | null>(() => getInitialBackendHealth())
  const [hasResolvedPrincipalState, setHasResolvedPrincipalState] = useState(false)
  const [signedInUserResponse, setSignedInUserResponse] = useState<UserResponse | null>(null)
  const [signedInManagerSessionResponse, setSignedInManagerSessionResponse] = useState<CurrentManagerSessionResponse | null>(null)
  const [currentNotice, setCurrentNotice] = useState<AppNotice | null>(null)
  const backendFailureCountRef = useRef(0)

  const translate = createTranslator(currentLanguage)
  const isGuestMode = signedInUserResponse === null && signedInManagerSessionResponse === null
  const isManagerOnlyMode = signedInUserResponse === null && signedInManagerSessionResponse !== null

  const normalizedViewKey = normalizeViewKey(currentViewKey)
  const currentTopNav = getActiveTopNav(normalizedViewKey)
  const topNavItems = getVisibleTopNavItems({
    signedInManager: signedInManagerSessionResponse,
    signedInUser: signedInUserResponse,
  })
  const sidebarItems = getSidebarItemsForTopNav({
    topNav: currentTopNav,
    signedInManager: signedInManagerSessionResponse,
    signedInUser: signedInUserResponse,
  })
  const showSidebar = shouldShowSidebar(currentTopNav, sidebarItems)

  const reloadBackendHealth = useCallback(async () => {
    try {
      const healthResponse = await travelMvpApiClient.getHealth()
      backendFailureCountRef.current = 0
      setBackendHealthResponse(healthResponse)
      return true
    } catch {
      backendFailureCountRef.current += 1
      if (backendFailureCountRef.current >= 3) {
        setBackendHealthResponse(null)
      }
      return false
    }
  }, [])

  const reloadPrincipalState = useCallback(async () => {
    const [currentUserSessionResult, currentManagerSessionResult] = await Promise.allSettled([
      travelMvpApiClient.getCurrentUserSession(),
      travelMvpApiClient.getCurrentManagerSession(),
    ])

    let didRecoverPrincipalState = true

    if (currentUserSessionResult.status === 'fulfilled') {
      setSignedInUserResponse(currentUserSessionResult.value.user)
    } else if (isUnauthorizedApiError(currentUserSessionResult.reason)) {
      setSignedInUserResponse(null)
    } else {
      didRecoverPrincipalState = false
    }

    if (currentManagerSessionResult.status === 'fulfilled') {
      setSignedInManagerSessionResponse(currentManagerSessionResult.value)
    } else if (isUnauthorizedApiError(currentManagerSessionResult.reason)) {
      setSignedInManagerSessionResponse(null)
    } else {
      didRecoverPrincipalState = false
    }

    if (didRecoverPrincipalState) {
      setHasResolvedPrincipalState(true)
    }

    return didRecoverPrincipalState
  }, [])

  useEffect(() => {
    let isDisposed = false
    let retryTimeout: ReturnType<typeof setTimeout> | null = null

    const bootstrap = async () => {
      const [didLoadHealth, didLoadPrincipalState] = await Promise.all([
        reloadBackendHealth(),
        reloadPrincipalState(),
      ])

      if ((!didLoadHealth || !didLoadPrincipalState) && !isDisposed) {
        retryTimeout = setTimeout(() => {
          void bootstrap()
        }, 2000)
      }
    }

    void bootstrap()

    const refreshInterval = setInterval(() => {
      void reloadBackendHealth()
      void reloadPrincipalState()
    }, 15000)

    return () => {
      isDisposed = true
      clearInterval(refreshInterval)
      if (retryTimeout) {
        clearTimeout(retryTimeout)
      }
    }
  }, [reloadBackendHealth, reloadPrincipalState])

  useEffect(() => {
    if (typeof window === 'undefined') {
      return
    }

    window.localStorage.setItem(languageStorageKey, currentLanguage)
  }, [currentLanguage])

  useEffect(() => {
    if (typeof window === 'undefined') {
      return
    }

    window.localStorage.setItem(viewStorageKey, normalizedViewKey)
  }, [normalizedViewKey])

  useEffect(() => {
    if (typeof window === 'undefined') {
      return
    }

    window.localStorage.setItem(themeStorageKey, themeMode)
    document.documentElement.dataset.theme = themeMode
  }, [themeMode])

  useEffect(() => {
    if (normalizedViewKey !== currentViewKey) {
      setCurrentViewKey(normalizedViewKey)
    }
  }, [currentViewKey, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (isGuestMode && normalizedViewKey === 'reviews') {
      setCurrentViewKey('blog')
    }
  }, [hasResolvedPrincipalState, isGuestMode, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (isManagerOnlyMode && normalizedViewKey !== 'overview' && normalizedViewKey !== 'blog' && normalizedViewKey !== 'account' && normalizedViewKey !== 'manager') {
      setCurrentViewKey('manager')
    }
  }, [hasResolvedPrincipalState, isManagerOnlyMode, normalizedViewKey])

  function showNotice(kind: AppNotice['kind'], title: string, description: string, technicalMessage?: string) {
    setCurrentNotice({
      id: Date.now(),
      kind,
      title,
      description,
      technicalMessage,
    })
  }

  function renderCurrentPage() {
    if (normalizedViewKey === 'overview') {
      return (
        <WorkspaceOverviewPage
          isSessionReady={hasResolvedPrincipalState}
          signedInUser={signedInUserResponse}
          translate={translate}
          onSelectView={setCurrentViewKey}
        />
      )
    }

    if (normalizedViewKey === 'smartPlanner') {
      return <SmartTripPlannerPage translate={translate} onSelectView={setCurrentViewKey} />
    }

    if (normalizedViewKey === 'blog') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <BlogPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'reviews') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <ReviewsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'account') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <AccountPage
          currentLanguage={currentLanguage}
          signedInManager={signedInManagerSessionResponse}
          signedInUser={signedInUserResponse}
          requestedEntryMode={accountEntryMode}
          translate={translate}
          onSignedInManagerChange={setSignedInManagerSessionResponse}
          onSignedInUserChange={setSignedInUserResponse}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'travelers') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <TravelersPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onSignedInUserChange={setSignedInUserResponse}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'flights') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <FlightsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'hotels') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <HotelsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'trains') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <TrainsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'attractions') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <AttractionsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'tourGroups') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <TourGroupsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'orders') {
      // Existing module mount preserved inside the new Travel Workbench shell.
      return (
        <BookingsPage
          currentLanguage={currentLanguage}
          isSessionReady={hasResolvedPrincipalState}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'manager') {
      return (
        <ManagerPage
          currentLanguage={currentLanguage}
          currentManagerSession={signedInManagerSessionResponse}
          translate={translate}
          onManagerSessionChange={setSignedInManagerSessionResponse}
          onNavigate={setCurrentViewKey}
          onShowNotice={showNotice}
        />
      )
    }

    return null
  }

  if (!hasResolvedPrincipalState) {
    return (
      <main className="layout-shell">
        <section className="content-shell">
          <section className="app-card empty-state-panel">
            <strong>{currentLanguage === 'zh' ? '正在恢复页面状态' : 'Restoring workspace state'}</strong>
          </section>
        </section>
      </main>
    )
  }

  return (
    <>
      <AppShell
        topNav={{
          currentLanguage,
          currentTopNav,
          themeMode,
          items: topNavItems,
          signedInManager: signedInManagerSessionResponse,
          signedInUser: signedInUserResponse,
          onChangeLanguage: setCurrentLanguage,
          onChangeTheme: setThemeMode,
          onSelectTopNav: (_topNav, defaultViewKey) => setCurrentViewKey(defaultViewKey),
          translate,
        }}
        sidebar={
          showSidebar
            ? {
                currentTopNav,
                currentViewKey: normalizedViewKey,
                items: sidebarItems,
                onSelectView: setCurrentViewKey,
                translate,
              }
            : undefined
        }
      >
        {renderCurrentPage()}
      </AppShell>

      <ToastNotice notice={currentNotice} onDismiss={() => setCurrentNotice(null)} />
    </>
  )
}

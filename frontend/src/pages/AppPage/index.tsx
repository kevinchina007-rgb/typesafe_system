import { useCallback, useEffect, useRef, useState } from 'react'

import { getActiveTopNav, getSidebarItemsForTopNav, getVisibleTopNavItems, shouldShowSidebar } from '@/app/navigation'
import { clearAppNotice, setAppLanguage, setAppNotice, setAppTheme, setAppView, useAppShellStore } from '@/app/stores/app-shell-store'
import { setCurrentManagerSession, setManagerStateResolved, useManagerStore } from '@/app/stores/manager-store'
import { setCurrentUserSession, setUserStateResolved, useUserStore } from '@/app/stores/user-store'
import { clearAllThreads, useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { AppShell } from '@/app/shell/AppShell'
import { ToastNotice } from '@/pages/shared/base/ToastNotice'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { isUnauthorizedApiError } from '@/microservices/common/api/ApiTransport'
import { createTranslator } from '@/lib/i18n/index'
import type { AppNotice, AppViewKey, HealthResponse } from '@/lib/mvp-types/index'
import { getInitialBackendHealth } from '@/lib/config/runtime-config'
import { AccountPage } from '@/pages/AccountPage'
import { AttractionsPage } from '@/pages/AttractionsPage'
import { BlogPage } from '@/pages/BlogPage'
import { BookingsPage } from '@/pages/BookingsPage'
import { CustomerFeedbackPage } from '@/pages/CustomerFeedbackPage'
import { FlightsPage } from '@/pages/FlightsPage'
import { HotelsPage } from '@/pages/HotelsPage'
import { ManagerPage } from '@/pages/ManagerPage'
import { ReviewsPage } from '@/pages/ReviewsPage'
import { SmartTripPlannerPage } from '@/pages/SmartTripPlannerPage'
import { TourGroupsPage } from '@/pages/TourGroupsPage'
import { TrainsPage } from '@/pages/TrainsPage'
import { TravelersPage } from '@/pages/TravelersPage'
import { WorkspaceOverviewPage } from '@/pages/WorkspaceOverviewPage'

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
  const currentLanguage = useAppShellStore(state => state.currentLanguage)
  const currentViewKey = useAppShellStore(state => state.currentViewKey)
  const themeMode = useAppShellStore(state => state.themeMode)
  const currentNotice = useAppShellStore(state => state.currentNotice)

  const hasResolvedUserState = useUserStore(state => state.hasResolvedUserState)
  const signedInUserResponse = useUserStore(state => state.signedInUser)
  const hasResolvedManagerState = useManagerStore(state => state.hasResolvedManagerState)
  const signedInManagerSessionResponse = useManagerStore(state => state.signedInManagerSession)
  const userFeedbackThreads = useFeedbackChatStore(state => state.userThreads)
  const managerFeedbackThreads = useFeedbackChatStore(state => state.managerThreads)
  const loadUserFeedbackThreads = useFeedbackChatStore(state => state.loadUserThreads)
  const loadManagerFeedbackThreads = useFeedbackChatStore(state => state.loadManagerThreads)

  const backendFailureCountRef = useRef(0)

  const translate = createTranslator(currentLanguage)
  const isGuestMode = signedInUserResponse === null && signedInManagerSessionResponse === null
  const isManagerOnlyMode = signedInUserResponse === null && signedInManagerSessionResponse !== null
  const isUserOnlyMode = signedInUserResponse !== null && signedInManagerSessionResponse === null
  const hasResolvedPrincipalState = hasResolvedUserState && hasResolvedManagerState

  const normalizedViewKey = normalizeViewKey(currentViewKey)
  const currentTopNav = getActiveTopNav(normalizedViewKey)
  const routeBadgeCounts = {
    customerFeedback: userFeedbackThreads.reduce((total, thread) => total + thread.unreadByUser, 0),
    managerFeedback: managerFeedbackThreads.reduce((total, thread) => total + thread.unreadByManager, 0),
  }
  const topNavItems = getVisibleTopNavItems({
    badgeCounts: routeBadgeCounts,
    signedInManager: signedInManagerSessionResponse,
    signedInUser: signedInUserResponse,
  })
  const sidebarItems = getSidebarItemsForTopNav({
    badgeCounts: routeBadgeCounts,
    topNav: currentTopNav,
    signedInManager: signedInManagerSessionResponse,
    signedInUser: signedInUserResponse,
  })
  const showSidebar = shouldShowSidebar(sidebarItems)

  const [, setBackendHealthResponse] = useState<HealthResponse | null>(() => getInitialBackendHealth())

  const withTimeout = useCallback(
    <T,>(promise: Promise<T>, timeoutMessage: string, timeoutMs = 2500): Promise<T> =>
      new Promise<T>((resolve, reject) => {
        const timeoutId = setTimeout(() => {
          reject(new Error(timeoutMessage))
        }, timeoutMs)

        promise
          .then(value => {
            clearTimeout(timeoutId)
            resolve(value)
          })
          .catch(error => {
            clearTimeout(timeoutId)
            reject(error)
          })
      }),
    [],
  )

  const reloadBackendHealth = useCallback(async () => {
    try {
      const healthResponse = await withTimeout(
        travelMvpApiClient.getHealth(),
        'backend_health_timeout',
      )
      backendFailureCountRef.current = 0
      void healthResponse
      return true
    } catch {
      backendFailureCountRef.current += 1
      if (backendFailureCountRef.current >= 3) {
        setBackendHealthResponse(null)
      }
      return false
    }
  }, [setBackendHealthResponse, withTimeout])

  const reloadPrincipalState = useCallback(async () => {
    const [currentUserSessionResult, currentManagerSessionResult] = await Promise.allSettled([
      withTimeout(travelMvpApiClient.getCurrentUserSession(), 'user_session_timeout'),
      withTimeout(travelMvpApiClient.getCurrentManagerSession(), 'manager_session_timeout'),
    ])

    if (currentUserSessionResult.status === 'fulfilled') {
      setCurrentUserSession(currentUserSessionResult.value.user)
    } else if (isUnauthorizedApiError(currentUserSessionResult.reason)) {
      setCurrentUserSession(null)
    } else {
      setCurrentUserSession(null)
    }

    if (currentManagerSessionResult.status === 'fulfilled') {
      setCurrentManagerSession(currentManagerSessionResult.value)
    } else if (isUnauthorizedApiError(currentManagerSessionResult.reason)) {
      setCurrentManagerSession(null)
    } else {
      setCurrentManagerSession(null)
    }

    setUserStateResolved(true)
    setManagerStateResolved(true)
    return true
  }, [withTimeout])

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
    document.documentElement.dataset.theme = themeMode
  }, [themeMode])

  useEffect(() => {
    if (normalizedViewKey !== currentViewKey) {
      setAppView(normalizedViewKey)
    }
  }, [currentViewKey, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (
      isGuestMode &&
      (normalizedViewKey === 'orders' ||
        normalizedViewKey === 'travelers' ||
        normalizedViewKey === 'tourGroups' ||
        normalizedViewKey === 'customerFeedback')
    ) {
      setAppView('blog')
      return
    }

    if (
      isGuestMode &&
      (normalizedViewKey === 'managerWorkspace' ||
        normalizedViewKey === 'managerFeedback' ||
        normalizedViewKey === 'managerAdvertising' ||
        normalizedViewKey === 'siteAdminBlogAudit' ||
        normalizedViewKey === 'siteAdminAdvertisingReview')
    ) {
      setAppView('manager')
    }
  }, [hasResolvedPrincipalState, isGuestMode, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (normalizedViewKey === 'reviews') {
      setAppView(signedInUserResponse ? 'customerFeedback' : 'blog')
    }
  }, [hasResolvedPrincipalState, normalizedViewKey, signedInUserResponse])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (isManagerOnlyMode && normalizedViewKey !== 'manager') {
      if (signedInManagerSessionResponse?.managerType === 'SiteAdmin') {
        if (
          normalizedViewKey !== 'siteAdminBlogAudit' &&
          normalizedViewKey !== 'siteAdminAdvertisingReview'
        ) {
          setAppView('siteAdminBlogAudit')
        }
      } else if (
        normalizedViewKey !== 'managerWorkspace' &&
        normalizedViewKey !== 'managerFeedback' &&
        !(normalizedViewKey === 'managerAdvertising' &&
          (signedInManagerSessionResponse?.managerType === 'Hotel' || signedInManagerSessionResponse?.managerType === 'Attraction'))
      ) {
        setAppView('managerWorkspace')
      }
    }
  }, [hasResolvedPrincipalState, isManagerOnlyMode, normalizedViewKey, signedInManagerSessionResponse])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (
      isUserOnlyMode &&
      (normalizedViewKey === 'manager' ||
        normalizedViewKey === 'managerWorkspace' ||
        normalizedViewKey === 'managerFeedback' ||
        normalizedViewKey === 'managerAdvertising' ||
        normalizedViewKey === 'siteAdminBlogAudit' ||
        normalizedViewKey === 'siteAdminAdvertisingReview')
    ) {
      setAppView('account')
    }
  }, [hasResolvedPrincipalState, isUserOnlyMode, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) {
      return
    }

    if (signedInUserResponse) {
      void loadUserFeedbackThreads()
      return
    }

    if (signedInManagerSessionResponse) {
      void loadManagerFeedbackThreads()
      return
    }

    clearAllThreads()
  }, [
    hasResolvedPrincipalState,
    loadManagerFeedbackThreads,
    loadUserFeedbackThreads,
    signedInManagerSessionResponse?.managerId,
    signedInManagerSessionResponse?.managerType,
    signedInUserResponse?.userId,
  ])

  function showNotice(kind: AppNotice['kind'], title: string, description: string, technicalMessage?: string) {
    setAppNotice({
      id: Date.now(),
      kind,
      title,
      description,
      technicalMessage,
    })
  }

  function renderCurrentPage() {
    if (normalizedViewKey === 'smartPlanner') {
      return <SmartTripPlannerPage translate={translate} onSelectView={setAppView} />
    }

    if (normalizedViewKey === 'overview') {
      return (
        <WorkspaceOverviewPage
          isSessionReady={hasResolvedPrincipalState}
          signedInUser={signedInUserResponse}
          translate={translate}
          onSelectView={setAppView}
        />
      )
    }

    if (normalizedViewKey === 'blog') {
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
      return (
        <AccountPage
          currentLanguage={currentLanguage}
          signedInManager={signedInManagerSessionResponse}
          signedInUser={signedInUserResponse}
          requestedEntryMode="login"
          translate={translate}
          onSignedInManagerChange={setCurrentManagerSession}
          onSignedInUserChange={setCurrentUserSession}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'customerFeedback') {
      return (
        <CustomerFeedbackPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'travelers') {
      return (
        <TravelersPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onSignedInUserChange={setCurrentUserSession}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'flights') {
      return (
        <FlightsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'hotels') {
      return (
        <HotelsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'trains') {
      return (
        <TrainsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'attractions') {
      return (
        <AttractionsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'tourGroups') {
      return (
        <TourGroupsPage
          currentLanguage={currentLanguage}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'orders') {
      return (
        <BookingsPage
          currentLanguage={currentLanguage}
          isSessionReady={hasResolvedPrincipalState}
          signedInUser={signedInUserResponse}
          translate={translate}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (normalizedViewKey === 'manager') {
      return (
        <ManagerPage
          currentLanguage={currentLanguage}
          currentViewKey={normalizedViewKey}
          currentManagerSession={signedInManagerSessionResponse}
          signedInUser={signedInUserResponse}
          translate={translate}
          onManagerSessionChange={setCurrentManagerSession}
          onSignedInUserChange={setCurrentUserSession}
          onNavigate={setAppView}
          onShowNotice={showNotice}
        />
      )
    }

    if (
      normalizedViewKey === 'managerWorkspace' ||
      normalizedViewKey === 'managerFeedback' ||
      normalizedViewKey === 'managerAdvertising' ||
      normalizedViewKey === 'siteAdminBlogAudit' ||
      normalizedViewKey === 'siteAdminAdvertisingReview'
    ) {
      return (
        <ManagerPage
          currentLanguage={currentLanguage}
          currentViewKey={normalizedViewKey}
          currentManagerSession={signedInManagerSessionResponse}
          signedInUser={signedInUserResponse}
          translate={translate}
          onManagerSessionChange={setCurrentManagerSession}
          onSignedInUserChange={setCurrentUserSession}
          onNavigate={setAppView}
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
            <strong>{currentLanguage === 'zh' ? '正在恢复工作台状态' : 'Restoring workspace state'}</strong>
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
          onChangeLanguage: setAppLanguage,
          onChangeTheme: setAppTheme,
          onSelectTopNav: (_topNav, defaultViewKey) => setAppView(defaultViewKey),
          translate,
        }}
        sidebar={
          showSidebar
            ? {
                currentTopNav,
                currentViewKey: normalizedViewKey,
                items: sidebarItems,
                onSelectView: setAppView,
                translate,
              }
            : undefined
        }
      >
        {renderCurrentPage()}
      </AppShell>

      <ToastNotice notice={currentNotice} onDismiss={clearAppNotice} />
    </>
  )
}





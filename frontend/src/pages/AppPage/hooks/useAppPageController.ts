import { useCallback, useEffect, useRef, useState } from 'react'

import { clearAllThreads, useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { setAppNotice, setAppView, useAppShellStore } from '@/app/stores/app-shell-store'
import { setCurrentManagerSession, setManagerStateResolved, useManagerStore } from '@/app/stores/manager-store'
import { setCurrentUserSession, setUserStateResolved, useUserStore } from '@/app/stores/user-store'
import { getActiveTopNav, getSidebarItemsForTopNav, getVisibleTopNavItems } from '@/app/navigation'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { isUnauthorizedApiError } from '@/microservices/common/api/ApiTransport'
import type { AppNotice, HealthResponse } from '@/lib/mvp-types/index'
import { getInitialBackendHealth } from '@/lib/config/runtime-config'
import { createTranslator } from '@/lib/i18n/index'
import { normalizeViewKey } from '../functions'

export function useAppPageController() {
  const currentLanguage = useAppShellStore(state => state.currentLanguage)
  const currentViewKey = useAppShellStore(state => state.currentViewKey)
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
  const [backendHealthResponse, setBackendHealthResponse] = useState<HealthResponse | null>(() => getInitialBackendHealth())

  const translate = createTranslator()
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
  const submenuItemsByTopNav = Object.fromEntries(
    topNavItems.map(item => [
      item.key,
      getSidebarItemsForTopNav({
        badgeCounts: routeBadgeCounts,
        topNav: item.key,
        signedInManager: signedInManagerSessionResponse,
        signedInUser: signedInUserResponse,
      }),
    ]),
  )

  const withTimeout = useCallback(
    <T,>(promise: Promise<T>, timeoutMessage: string, timeoutMs = 2500): Promise<T> =>
      new Promise<T>((resolve, reject) => {
        const timeoutId = setTimeout(() => reject(new Error(timeoutMessage)), timeoutMs)
        promise.then(value => {
          clearTimeout(timeoutId)
          resolve(value)
        }).catch(error => {
          clearTimeout(timeoutId)
          reject(error)
        })
      }),
    [],
  )

  const reloadBackendHealth = useCallback(async () => {
    try {
      await withTimeout(travelMvpApiClient.getHealth(), 'backend_health_timeout')
      backendFailureCountRef.current = 0
      return true
    } catch {
      backendFailureCountRef.current += 1
      if (backendFailureCountRef.current >= 3) {
        setBackendHealthResponse(null)
      }
      return false
    }
  }, [withTimeout])

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
      if (retryTimeout) clearTimeout(retryTimeout)
    }
  }, [reloadBackendHealth, reloadPrincipalState])

  useEffect(() => {
    if (normalizedViewKey !== currentViewKey) setAppView(normalizedViewKey)
  }, [currentViewKey, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) return
    if (isGuestMode && (normalizedViewKey === 'travelers' || normalizedViewKey === 'tourGroups' || normalizedViewKey === 'tourGroupPlanBuilder' || normalizedViewKey === 'customerFeedback')) {
      setAppView('blog')
      return
    }
    if (
      isGuestMode &&
      (normalizedViewKey === 'managerWorkspace' ||
        normalizedViewKey === 'managerCreateFlight' ||
        normalizedViewKey === 'managerFlightManagement' ||
        normalizedViewKey === 'managerFeedback' ||
        normalizedViewKey === 'managerProfile' ||
        normalizedViewKey === 'managerAdvertising' ||
        normalizedViewKey === 'siteAdminBlogAudit' ||
        normalizedViewKey === 'siteAdminAdvertisingReview' ||
        normalizedViewKey === 'siteAdminHotelAdvertisingReview' ||
        normalizedViewKey === 'siteAdminTrainAdvertisingReview' ||
        normalizedViewKey === 'siteAdminAttractionAdvertisingReview' ||
        normalizedViewKey === 'siteAdminFeedback')
    ) {
      setAppView('manager')
    }
  }, [hasResolvedPrincipalState, isGuestMode, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) return
    if (normalizedViewKey === 'reviews') {
      setAppView(signedInUserResponse ? 'customerFeedback' : 'blog')
    }
  }, [hasResolvedPrincipalState, normalizedViewKey, signedInUserResponse])

  useEffect(() => {
    if (!hasResolvedPrincipalState) return
    if (isManagerOnlyMode && normalizedViewKey !== 'manager') {
      if (signedInManagerSessionResponse?.managerType === 'SiteAdmin') {
        if (
          normalizedViewKey !== 'siteAdminBlogAudit' &&
          normalizedViewKey !== 'siteAdminAdvertisingReview' &&
          normalizedViewKey !== 'siteAdminHotelAdvertisingReview' &&
          normalizedViewKey !== 'siteAdminTrainAdvertisingReview' &&
          normalizedViewKey !== 'siteAdminAttractionAdvertisingReview' &&
          normalizedViewKey !== 'siteAdminFeedback'
        ) {
          setAppView('siteAdminBlogAudit')
        }
      } else if (signedInManagerSessionResponse?.managerType === 'Airline') {
        if (normalizedViewKey !== 'managerCreateFlight' && normalizedViewKey !== 'managerFlightManagement' && normalizedViewKey !== 'managerFeedback' && normalizedViewKey !== 'managerAdvertising') {
          setAppView('managerFlightManagement')
        }
      } else if (
        normalizedViewKey !== 'managerWorkspace' &&
        normalizedViewKey !== 'managerFeedback' &&
        normalizedViewKey !== 'managerAdvertising'
      ) {
        setAppView('managerWorkspace')
      }
    }
  }, [hasResolvedPrincipalState, isManagerOnlyMode, normalizedViewKey, signedInManagerSessionResponse])

  useEffect(() => {
    if (!hasResolvedPrincipalState) return
    if (
      isUserOnlyMode &&
      (normalizedViewKey === 'manager' ||
        normalizedViewKey === 'account' ||
        normalizedViewKey === 'managerWorkspace' ||
        normalizedViewKey === 'managerCreateFlight' ||
        normalizedViewKey === 'managerFlightManagement' ||
        normalizedViewKey === 'managerFeedback' ||
        normalizedViewKey === 'managerProfile' ||
        normalizedViewKey === 'managerAdvertising' ||
        normalizedViewKey === 'siteAdminBlogAudit' ||
        normalizedViewKey === 'siteAdminAdvertisingReview' ||
        normalizedViewKey === 'siteAdminHotelAdvertisingReview' ||
        normalizedViewKey === 'siteAdminTrainAdvertisingReview' ||
        normalizedViewKey === 'siteAdminAttractionAdvertisingReview' ||
        normalizedViewKey === 'siteAdminFeedback')
    ) {
      setAppView('overview')
    }
  }, [hasResolvedPrincipalState, isUserOnlyMode, normalizedViewKey])

  useEffect(() => {
    if (!hasResolvedPrincipalState) return
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
    setAppNotice({ id: Date.now(), kind, title, description, technicalMessage })
  }

  async function runHeaderAccountAction(action: () => Promise<void>, successMessage?: string) {
    try {
      await action()
      if (successMessage) {
        showNotice('success', translate('notice.actionSuccess'), successMessage)
      }
    } catch (error) {
      showNotice('error', translate('error.friendly.default'), error instanceof Error ? error.message : String(error))
    }
  }

  return {
    currentLanguage,
    currentViewKey,
    currentNotice,
    signedInUserResponse,
    signedInManagerSessionResponse,
    hasResolvedPrincipalState,
    normalizedViewKey,
    currentTopNav,
    topNavItems,
    submenuItemsByTopNav,
    translate,
    showNotice,
    runHeaderAccountAction,
    backendHealthResponse,
    setCurrentUserSession,
    setCurrentManagerSession,
    setAppView,
  }
}

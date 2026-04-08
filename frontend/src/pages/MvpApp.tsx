import { useCallback, useEffect, useRef, useState } from 'react'

import { AppSidebar } from '../components/AppSidebar'
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
import { ExplorePage } from './ExplorePage'
import { FlightsPage } from './FlightsPage'
import { HotelsPage } from './HotelsPage'
import { ManagerPage } from './ManagerPage'
import { ReviewsPage } from './ReviewsPage'
import { TourGroupsPage } from './TourGroupsPage'
import { TrainsPage } from './TrainsPage'
import { TravelersPage } from './TravelersPage'

export function MvpApp() {
  // MvpApp 现在只保留真正的 shell 级状态：
  // 当前语言、当前页面、当前 principal、全局 notice、后端健康状态。
  const [currentLanguage, setCurrentLanguage] = useState<AppLanguage>('en')
  const [currentViewKey, setCurrentViewKey] = useState<AppViewKey>('blog')
  const [accountEntryMode, setAccountEntryMode] = useState<'register' | 'login'>('login')
  const [backendHealthResponse, setBackendHealthResponse] = useState<HealthResponse | null>(() => getInitialBackendHealth())
  const [signedInUserResponse, setSignedInUserResponse] = useState<UserResponse | null>(null)
  const [signedInManagerSessionResponse, setSignedInManagerSessionResponse] = useState<CurrentManagerSessionResponse | null>(null)
  const [currentNotice, setCurrentNotice] = useState<AppNotice | null>(null)
  const backendFailureCountRef = useRef(0)

  const translate = createTranslator(currentLanguage)
  const isGuestMode = signedInUserResponse === null && signedInManagerSessionResponse === null
  const isManagerOnlyMode = signedInUserResponse === null && signedInManagerSessionResponse !== null

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
    // user / manager 通过两条独立 session 链恢复。
    // 只有服务端明确返回未授权时才清空当前 principal；
    // 临时网络失败或后端短暂抖动不应把已登录用户打回游客态。
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

    return didRecoverPrincipalState
  }, [])

  useEffect(() => {
    // 启动时恢复后端健康和当前 principal，
    // 之后用固定轮询保持 UI 与服务端 session 同步。
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
    if (currentViewKey === 'trainAdmin' || currentViewKey === 'attractionAdmin') {
      setCurrentViewKey('manager')
    }
  }, [currentViewKey])

  useEffect(() => {
    if (isGuestMode && currentViewKey !== 'blog' && currentViewKey !== 'account') {
      setCurrentViewKey('blog')
    }
  }, [currentViewKey, isGuestMode])

  useEffect(() => {
    if (isManagerOnlyMode && currentViewKey !== 'blog' && currentViewKey !== 'account' && currentViewKey !== 'manager') {
      setCurrentViewKey('manager')
    }
  }, [currentViewKey, isManagerOnlyMode])

  function showNotice(kind: AppNotice['kind'], title: string, description: string, technicalMessage?: string) {
    setCurrentNotice({
      id: Date.now(),
      kind,
      title,
      description,
      technicalMessage,
    })
  }

  return (
    <main className="layout-shell">
      <AppSidebar
        currentLanguage={currentLanguage}
        currentViewKey={currentViewKey}
        health={backendHealthResponse}
        signedInManager={signedInManagerSessionResponse}
        signedInUser={signedInUserResponse}
        onChangeLanguage={setCurrentLanguage}
        onSelectView={setCurrentViewKey}
        onOpenAccountEntryMode={nextAccountEntryMode => {
          setAccountEntryMode(nextAccountEntryMode)
          setCurrentViewKey('account')
        }}
        translate={translate}
      />

      <section className="content-shell">
        {/* 这里是应用级视图分发层。每个 page 自己再管理局部数据与交互。 */}
        {currentViewKey === 'blog' ? (
          <BlogPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'reviews' ? (
          <ReviewsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'explore' ? <ExplorePage translate={translate} onOpenView={setCurrentViewKey} /> : null}

        {currentViewKey === 'account' ? (
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
        ) : null}

        {currentViewKey === 'travelers' ? (
          <TravelersPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onSignedInUserChange={setSignedInUserResponse}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'flights' ? (
          <FlightsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onNavigate={setCurrentViewKey}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'hotels' ? (
          <HotelsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onNavigate={setCurrentViewKey}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'trains' ? (
          <TrainsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onNavigate={setCurrentViewKey}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'attractions' ? (
          <AttractionsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onNavigate={setCurrentViewKey}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'tourGroups' ? (
          <TourGroupsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onNavigate={setCurrentViewKey}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'bookings' ? (
          <BookingsPage
            currentLanguage={currentLanguage}
            signedInUser={signedInUserResponse}
            translate={translate}
            onShowNotice={showNotice}
          />
        ) : null}

        {currentViewKey === 'manager' ? (
          <ManagerPage
            currentLanguage={currentLanguage}
            currentManagerSession={signedInManagerSessionResponse}
            translate={translate}
            onManagerSessionChange={setSignedInManagerSessionResponse}
            onNavigate={setCurrentViewKey}
            onShowNotice={showNotice}
          />
        ) : null}
      </section>

      <ToastNotice notice={currentNotice} onDismiss={() => setCurrentNotice(null)} />
    </main>
  )
}

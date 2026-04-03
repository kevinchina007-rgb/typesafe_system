import { useCallback, useEffect, useRef, useState } from 'react'

import { AppSidebar } from '../components/AppSidebar'
import { ToastNotice } from '../components/ToastNotice'
import { travelMvpApiClient } from '../lib/api-client'
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
    try {
      const [currentUserSession, currentManagerSession] = await Promise.all([
        travelMvpApiClient.getCurrentUserSession().catch(() => null),
        travelMvpApiClient.getCurrentManagerSession().catch(() => null),
      ])

      setSignedInUserResponse(currentUserSession?.user ?? null)
      setSignedInManagerSessionResponse(currentManagerSession)
      return true
    } catch {
      setSignedInUserResponse(null)
      setSignedInManagerSessionResponse(null)
      return false
    }
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

        {currentViewKey === 'explore' ? <ExplorePage translate={translate} /> : null}

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
            onShowNotice={showNotice}
          />
        ) : null}
      </section>

      <ToastNotice notice={currentNotice} onDismiss={() => setCurrentNotice(null)} />
    </main>
  )
}

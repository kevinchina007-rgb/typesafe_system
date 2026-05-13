import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { ManagerCenterSectionKey } from '@/pages/ManagerPage/sections/ManagerCenterSections'
﻿import { useEffect, useMemo, useState } from 'react'

import { usePageActions } from '@/pages/shared/usePageActions'
import { AdvertisementSubmissionWorkspace } from '@/pages/ManagerPage/components/advertising/AdvertisementSubmissionWorkspace'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, AttractionAdminSessionResponse, CurrentManagerSessionResponse, FlightResponse, HotelResponse, ManagerRefundTaskResponse, ManagerSessionResponse, ManagerTaskResponse, TrainAdminSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import { SiteAdminPanel, SupplierFeedbackSection } from '@/pages/ManagerPage/sections/ManagerCenterSections'
import { AttractionManagerPanelSection, SupplierManagerPanelSection, TrainManagerPanelSection } from '@/pages/ManagerPage/sections/ManagerPagePanels'
import { toLegacyManagerSession, toManagerTypeKey } from '@/pages/ManagerPage/models/managerPageSession'

type ManagerPageProps = {
  currentLanguage: AppLanguage
  currentViewKey: AppViewKey
  currentManagerSession: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onManagerSessionChange: (managerSession: CurrentManagerSessionResponse | null) => void
  onSignedInUserChange: (user: UserResponse | null) => void
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

type LoginManagerType = 'airline' | 'hotel' | 'train' | 'attraction' | 'siteAdmin'

type ManagerAuthCardProps = {
  title: string
  registerTitle: string
  loginTitle: string
  registerFields: Array<{ label: string; name: string; type?: string; placeholder?: string }>
  loginManagerType: LoginManagerType
  isBusy: boolean
  onValidationError: (message: string) => void
  onRegister: (payload: Record<string, string>) => Promise<void>
  onLogin: (payload: { managerType: LoginManagerType; email: string; password: string }) => Promise<void>
  onBack: () => void
  translate: (translationKey: string) => string
}

type ManagerEntryCardProps = {
  title: string
  onSelect: () => void
}

function ManagerEntryCard({ title, onSelect }: ManagerEntryCardProps) {
  return (
    <button type="button" className="manager-entry-card panel-card" onClick={onSelect}>
      <div className="manager-auth-card-head">
        <h3 className="manager-auth-card-title">{title}</h3>
      </div>
    </button>
  )
}

function ManagerAuthCard({
  title,
  registerTitle,
  loginTitle,
  registerFields,
  loginManagerType,
  isBusy,
  onValidationError,
  onRegister,
  onLogin,
  onBack,
  translate,
}: ManagerAuthCardProps) {
  return (
    <article className="manager-auth-card panel-card">
      <div className="manager-auth-card-head">
        <p className="eyebrow-label">{translate('nav.managerCenter')}</p>
        <h3 className="manager-auth-card-title">{title}</h3>
      </div>

      <div className="manager-entry-card-actions">
        <button type="button" className="secondary-button" onClick={onBack}>
          {translate('manager.backToCategories')}
        </button>
      </div>

      <form
        className="stack-form"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const password = String(formData.get('password') ?? '')
          const confirmPassword = String(formData.get('confirmPassword') ?? '')
          if (password !== confirmPassword) {
            onValidationError(translate('error.passwordMismatch'))
            return
          }
          if (password.length < 10) {
            onValidationError(translate('error.passwordTooShort'))
            return
          }

          const payload = Object.fromEntries(formData.entries()) as Record<string, string>
          await onRegister(payload)
          event.currentTarget.reset()
        }}
      >
        <h4 className="manager-auth-form-title">{registerTitle}</h4>
        {registerFields.map(field => (
          <label key={field.name}>
            {field.label}
            <input
              name={field.name}
              type={field.type ?? 'text'}
              placeholder={field.placeholder ?? field.label}
              required
              disabled={isBusy}
            />
          </label>
        ))}
        <label>
          {translate('account.password')}
          <input name="password" type="password" placeholder={translate('account.password')} required disabled={isBusy} />
        </label>
        <label>
          {translate('account.confirmPassword')}
          <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required disabled={isBusy} />
        </label>
        <button type="submit" disabled={isBusy}>
          {translate('manager.createAccount')}
        </button>
      </form>

      <form
        className="stack-form"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          await onLogin({
            managerType: loginManagerType,
            email: String(formData.get('email') ?? '').trim(),
            password: String(formData.get('password') ?? ''),
          })
        }}
      >
        <h4 className="manager-auth-form-title">{loginTitle}</h4>
        <label>
          {translate('manager.email')}
          <input name="email" type="email" placeholder={translate('manager.email')} required disabled={isBusy} />
        </label>
        <label>
          {translate('account.password')}
          <input name="password" type="password" placeholder={translate('account.password')} required disabled={isBusy} />
        </label>
        <button type="submit" className="secondary-button" disabled={isBusy}>
          {translate('manager.login')}
        </button>
      </form>
    </article>
  )
}

function getManagerEntryTitle(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') {
    return translate('manager.type.airline')
  }
  if (managerType === 'hotel') {
    return translate('manager.type.hotel')
  }
  if (managerType === 'train') {
    return translate('manager.type.train')
  }
  if (managerType === 'siteAdmin') {
    return translate('manager.type.siteAdmin')
  }
  return translate('manager.type.attraction')
}

function getManagerRegisterTitle(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') {
    return translate('manager.registerAirline')
  }
  if (managerType === 'hotel') {
    return translate('manager.registerHotel')
  }
  if (managerType === 'train') {
    return translate('trainAdmin.registerManager')
  }
  if (managerType === 'siteAdmin') {
    return translate('manager.siteAdmin.register')
  }
  return translate('attractionAdmin.registerTitle')
}

function getManagerLoginTitle(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') {
    return translate('manager.loginAirline')
  }
  if (managerType === 'hotel') {
    return translate('manager.loginHotel')
  }
  if (managerType === 'train') {
    return translate('trainAdmin.login')
  }
  if (managerType === 'siteAdmin') {
    return translate('manager.siteAdmin.login')
  }
  return translate('attractionAdmin.loginTitle')
}

function getManagerRegisterFields(managerType: LoginManagerType, translate: (translationKey: string) => string) {
  if (managerType === 'airline') {
    return [
      { label: translate('manager.displayName'), name: 'displayName' },
      { label: translate('manager.email'), name: 'email', type: 'email' },
      { label: translate('manager.airlineName'), name: 'airlineName' },
      { label: translate('manager.airlineCode'), name: 'airlineCode', placeholder: 'MU' },
    ]
  }

  if (managerType === 'hotel') {
    return [
      { label: translate('manager.displayName'), name: 'displayName' },
      { label: translate('manager.email'), name: 'email', type: 'email' },
      { label: translate('manager.hotelName'), name: 'hotelName' },
      { label: translate('manager.hotelLocation'), name: 'location' },
    ]
  }

  if (managerType === 'train') {
    return [
      { label: translate('trainAdmin.operatorCode'), name: 'operatorCode', placeholder: 'CRH' },
      { label: translate('trainAdmin.displayName'), name: 'displayName' },
      { label: translate('trainAdmin.email'), name: 'email', type: 'email' },
    ]
  }

  if (managerType === 'siteAdmin') {
    return [
      { label: translate('manager.displayName'), name: 'displayName' },
      { label: translate('manager.email'), name: 'email', type: 'email' },
    ]
  }

  return [
    {
      label: translate('attractionAdmin.displayName'),
      name: 'displayName',
      placeholder: translate('attractionAdmin.displayNamePlaceholder'),
    },
    { label: translate('attractionAdmin.email'), name: 'email', type: 'email' },
  ]
}

function toActiveSection(currentViewKey: AppViewKey): ManagerCenterSectionKey {
  if (currentViewKey === 'managerFeedback') {
    return 'feedback'
  }
  if (currentViewKey === 'managerAdvertising') {
    return 'advertising'
  }
  if (currentViewKey === 'siteAdminBlogAudit') {
    return 'blogAudit'
  }
  if (currentViewKey === 'siteAdminAdvertisingReview') {
    return 'advertisingReview'
  }
  return 'workspace'
}

export function ManagerPage({
  currentLanguage,
  currentViewKey,
  currentManagerSession,
  signedInUser,
  translate,
  onManagerSessionChange,
  onSignedInUserChange,
  onNavigate,
  onShowNotice,
}: ManagerPageProps) {
  const activeManagerType = currentManagerSession ? toManagerTypeKey(currentManagerSession.managerType) : null
  const activeSection = toActiveSection(currentViewKey)
  const [selectedEntryType, setSelectedEntryType] = useState<LoginManagerType | null>(null)
  const [currentSupplierManagerSession, setCurrentSupplierManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [managedFlightResponses, setManagedFlightResponses] = useState<FlightResponse[]>([])
  const [managedHotelResponses, setManagedHotelResponses] = useState<HotelResponse[]>([])
  const [currentTrainAdminSession, setCurrentTrainAdminSession] = useState<TrainAdminSessionResponse | null>(null)
  const [currentAttractionAdminSession, setCurrentAttractionAdminSession] = useState<AttractionAdminSessionResponse | null>(null)
  const [managerTaskResponses, setManagerTaskResponses] = useState<ManagerTaskResponse[]>([])
  const [managerRefundTaskResponses, setManagerRefundTaskResponses] = useState<ManagerRefundTaskResponse[]>([])
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  async function reloadManagerTasks(
    filters: {
      status: 'pending' | 'all' | 'confirmed' | 'rejected'
      resourceType: 'all' | 'flight' | 'hotel' | 'train' | 'attraction'
    } = { status: 'pending', resourceType: 'all' },
    session?: ManagerSessionResponse,
  ) {
    const effectiveSession = session ?? currentSupplierManagerSession
    if (!effectiveSession) {
      return
    }

    const taskListResponse = await travelMvpApiClient.listManagerTasks({
      managerId: effectiveSession.managerId,
      managerType: effectiveSession.managerType.toLowerCase(),
      status: filters.status,
      resourceType: filters.resourceType === 'all' ? undefined : filters.resourceType,
    })
    setManagerTaskResponses(taskListResponse.tasks)
  }

  async function reloadManagerRefundTasks(session?: ManagerSessionResponse) {
    const effectiveSession = session ?? currentSupplierManagerSession
    if (!effectiveSession) {
      return
    }

    const refundTaskListResponse = await travelMvpApiClient.listManagerRefundTasks({
      managerId: effectiveSession.managerId,
      managerType: effectiveSession.managerType.toLowerCase(),
    })
    setManagerRefundTaskResponses(refundTaskListResponse.tasks)
  }

  async function reloadManagedFlights(managerId: string) {
    const flightListResponse = await travelMvpApiClient.listManagerFlights(managerId)
    setManagedFlightResponses(flightListResponse.flights)
  }

  async function reloadManagedHotels(managerId: string) {
    const hotelListResponse = await travelMvpApiClient.listManagedHotels(managerId)
    setManagedHotelResponses(hotelListResponse.hotels)
  }

  async function reloadManagedTrains(managerId: string, baseSession?: CurrentManagerSessionResponse | null) {
    const trainListResponse = await travelMvpApiClient.listManagedTrains(managerId)
    const sourceSession = baseSession ?? currentManagerSession
    if (!sourceSession) {
      return
    }

    setCurrentTrainAdminSession({
      managerId: sourceSession.managerId,
      operatorCode: sourceSession.scopeId,
      email: sourceSession.email,
      displayName: sourceSession.displayName,
      status: sourceSession.status,
      managedTrains: trainListResponse.trains,
    })
  }

  async function reloadManagedAttractions(managerId: string, baseSession?: CurrentManagerSessionResponse | null) {
    const attractionListResponse = await travelMvpApiClient.listManagedAttractions(managerId)
    const sourceSession = baseSession ?? currentManagerSession
    if (!sourceSession) {
      return
    }

    setCurrentAttractionAdminSession({
      managerId: sourceSession.managerId,
      email: sourceSession.email,
      displayName: sourceSession.displayName,
      status: sourceSession.status,
      managedAttractions: attractionListResponse.attractions,
    })
  }

  useEffect(() => {
    void (async () => {
      if (!currentManagerSession) {
        setCurrentSupplierManagerSession(null)
        setCurrentTrainAdminSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightResponses([])
        setManagedHotelResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        return
      }

      const managerType = toManagerTypeKey(currentManagerSession.managerType)

      if (managerType === 'airline' || managerType === 'hotel' || managerType === 'attraction') {
        const legacySession = toLegacyManagerSession(currentManagerSession)
        setCurrentSupplierManagerSession(legacySession)
        setCurrentTrainAdminSession(null)
        if (managerType !== 'attraction') {
          setCurrentAttractionAdminSession(null)
        }

        await Promise.all([
          reloadManagerTasks({ status: 'pending', resourceType: 'all' }, legacySession),
          reloadManagerRefundTasks(legacySession),
          managerType === 'airline'
            ? reloadManagedFlights(currentManagerSession.managerId)
            : Promise.resolve(setManagedFlightResponses([])),
          managerType === 'hotel'
            ? reloadManagedHotels(currentManagerSession.managerId)
            : Promise.resolve(setManagedHotelResponses([])),
          managerType === 'attraction'
            ? reloadManagedAttractions(currentManagerSession.managerId, currentManagerSession)
            : Promise.resolve(),
        ])
      } else if (managerType === 'train') {
        setCurrentSupplierManagerSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightResponses([])
        setManagedHotelResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        await reloadManagedTrains(currentManagerSession.managerId, currentManagerSession)
      } else if (managerType === 'siteAdmin') {
        setCurrentSupplierManagerSession(null)
        setCurrentTrainAdminSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightResponses([])
        setManagedHotelResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
      }
    })()
  }, [currentManagerSession])

  async function logoutManager() {
    await travelMvpApiClient.logoutManagerAuth()
    onManagerSessionChange(null)
    setSelectedEntryType(null)
    setCurrentSupplierManagerSession(null)
    setCurrentTrainAdminSession(null)
    setCurrentAttractionAdminSession(null)
    setManagedFlightResponses([])
    setManagedHotelResponses([])
    setManagerTaskResponses([])
    setManagerRefundTaskResponses([])
    onNavigate('manager')
  }

  async function ensureUserLoggedOut() {
    if (!signedInUser) {
      return
    }
    await travelMvpApiClient.logoutUser()
    onSignedInUserChange(null)
  }

  async function loginSelectedManager(managerType: LoginManagerType, email: string, password: string) {
    const session = await travelMvpApiClient.loginManagerAuth({
      managerType,
      email,
      password,
    })
    onManagerSessionChange(session)
  }

  async function registerSelectedManager(managerType: LoginManagerType, payload: Record<string, string>) {
    if (managerType === 'airline') {
      await travelMvpApiClient.registerAirlineManager({
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        airlineName: payload.airlineName.trim(),
        airlineCode: payload.airlineCode.trim(),
        password: payload.password,
      })
      return
    }

    if (managerType === 'hotel') {
      await travelMvpApiClient.registerHotelManager({
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        hotelName: payload.hotelName.trim(),
        location: payload.location.trim(),
        password: payload.password,
      })
      return
    }

    if (managerType === 'train') {
      await travelMvpApiClient.registerRailwayManager({
        operatorCode: payload.operatorCode.trim(),
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        password: payload.password,
      })
      return
    }

    if (managerType === 'siteAdmin') {
      await travelMvpApiClient.registerSiteAdmin({
        email: payload.email.trim(),
        displayName: payload.displayName.trim(),
        password: payload.password,
      })
      return
    }

    await travelMvpApiClient.registerAttractionManager({
      email: payload.email.trim(),
      displayName: payload.displayName.trim(),
      password: payload.password,
    })
  }

  const hotelAdvertisementOptions = useMemo(() => {
    if (activeManagerType !== 'hotel') {
      return []
    }

    if (managedHotelResponses.length > 0) {
      return managedHotelResponses.map(hotel => ({
        value: hotel.hotelId,
        label: `${hotel.hotelName} 路 ${hotel.location}`,
      }))
    }

    if (!currentManagerSession) {
      return []
    }

    return [
      {
        value: currentManagerSession.scopeId,
        label: currentManagerSession.displayName,
      },
    ]
  }, [activeManagerType, currentManagerSession?.scopeId, currentManagerSession?.displayName, managedHotelResponses])

  const attractionAdvertisementOptions = useMemo(
    () =>
      (currentAttractionAdminSession?.managedAttractions ?? []).map(attraction => ({
        value: attraction.attractionId,
        label: attraction.attractionName,
      })),
    [currentAttractionAdminSession?.managedAttractions],
  )

  const isSiteAdmin = activeManagerType === 'siteAdmin'
  const canSubmitAdvertisements = activeManagerType === 'hotel' || activeManagerType === 'attraction'
  const shouldShowWorkspace = !isSiteAdmin && activeSection === 'workspace'
  const shouldShowFeedback = !isSiteAdmin && activeSection === 'feedback'
  const shouldShowAdvertising = !isSiteAdmin && activeSection === 'advertising' && canSubmitAdvertisements
  const shouldShowSiteAdminPanel = isSiteAdmin && (activeSection === 'blogAudit' || activeSection === 'advertisingReview')

  if (!currentManagerSession) {
    if (selectedEntryType) {
      const entryTitle = getManagerEntryTitle(selectedEntryType, translate)

      return (
        <section className="page-card manager-center-entry-page">
          <div className="manager-center-entry-copy">
            <p className="eyebrow-label">{translate('nav.managerCenter')}</p>
            <h2>{entryTitle}</h2>
            <p className="hero-copy">{translate('manager.centerDescription')}</p>
          </div>

          <div className="manager-center-auth-shell">
            <ManagerAuthCard
              title={entryTitle}
              registerTitle={getManagerRegisterTitle(selectedEntryType, translate)}
              loginTitle={getManagerLoginTitle(selectedEntryType, translate)}
              registerFields={getManagerRegisterFields(selectedEntryType, translate)}
              loginManagerType={selectedEntryType}
              isBusy={isBusy}
              onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
              onRegister={async payload => {
                await runPageAction(async () => {
                  await ensureUserLoggedOut()
                  await registerSelectedManager(selectedEntryType, payload)
                  await loginSelectedManager(selectedEntryType, payload.email.trim(), payload.password)
                  setSelectedEntryType(null)
                }, translate('manager.createAccount'), translate('notice.actionSuccess'))
              }}
              onLogin={async payload => {
                await runPageAction(async () => {
                  await ensureUserLoggedOut()
                  await loginSelectedManager(payload.managerType, payload.email, payload.password)
                  setSelectedEntryType(null)
                }, translate('manager.login'), translate('notice.actionSuccess'))
              }}
              onBack={() => setSelectedEntryType(null)}
              translate={translate}
            />
          </div>
        </section>
      )
    }

    return (
      <section className="page-card manager-center-entry-page">
        <div className="manager-center-entry-copy">
          <p className="eyebrow-label">{translate('nav.managerCenter')}</p>
          <h2>{translate('manager.title')}</h2>
          <p className="hero-copy">{translate('manager.centerDescription')}</p>
        </div>

        <div className="manager-center-entry-grid">
          <ManagerEntryCard title={translate('manager.type.airline')} onSelect={() => setSelectedEntryType('airline')} />
          <ManagerEntryCard title={translate('manager.type.hotel')} onSelect={() => setSelectedEntryType('hotel')} />
          <ManagerEntryCard title={translate('manager.type.train')} onSelect={() => setSelectedEntryType('train')} />
          <ManagerEntryCard title={translate('manager.type.attraction')} onSelect={() => setSelectedEntryType('attraction')} />
          <ManagerEntryCard title={translate('manager.type.siteAdmin')} onSelect={() => setSelectedEntryType('siteAdmin')} />
        </div>
      </section>
    )
  }

  return (
    <>
      <section className="page-card manager-center-entry-page">
        <div className="manager-center-entry-copy">
          <p className="eyebrow-label">{translate('nav.managerCenter')}</p>
          <h2>{getManagerEntryTitle(activeManagerType ?? 'airline', translate)}</h2>
          <p className="hero-copy">{translate('manager.centerDescription')}</p>
        </div>
        <div className="manager-entry-card-actions">
          <button
            type="button"
            className="secondary-button"
            onClick={() => {
              void runPageAction(async () => {
                await logoutManager()
              }, translate('manager.logout'), translate('notice.logoutSuccess'))
            }}
          >
            {translate('manager.logout')}
          </button>
        </div>
      </section>

      <SupplierManagerPanelSection
        isVisible={shouldShowWorkspace && (activeManagerType === 'airline' || activeManagerType === 'hotel')}
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        managerSession={currentSupplierManagerSession}
        managedFlights={managedFlightResponses}
        managedHotels={managedHotelResponses}
        managerTasks={managerTaskResponses}
        managerRefundTasks={managerRefundTaskResponses}
        translate={translate}
        onRegisterAirlineManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await travelMvpApiClient.registerAirlineManager(payload)
            await loginSelectedManager('airline', payload.email, payload.password)
          }, translate('manager.createAccount'), translate('notice.actionSuccess'))
        }}
        onRegisterHotelManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await travelMvpApiClient.registerHotelManager(payload)
            await loginSelectedManager('hotel', payload.email, payload.password)
          }, translate('manager.createAccount'), translate('notice.actionSuccess'))
        }}
        onCreateManagerRoomType={async payload => {
          await runPageAction(async () => {
            await travelMvpApiClient.createManagerRoomType(payload)
            await reloadManagedHotels(payload.managerId)
          }, translate('manager.createRoomType'), translate('notice.actionSuccess'))
        }}
        onLoginManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await loginSelectedManager(payload.managerType as LoginManagerType, payload.email, payload.password)
          }, translate('manager.login'), translate('notice.actionSuccess'))
        }}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onReloadTasks={async filters => {
          await runPageAction(async () => {
            await reloadManagerTasks(filters)
            if (currentSupplierManagerSession?.managerType === 'Airline') {
              await reloadManagedFlights(currentSupplierManagerSession.managerId)
            } else if (currentSupplierManagerSession?.managerType === 'Hotel') {
              await reloadManagedHotels(currentSupplierManagerSession.managerId)
            }
          }, translate('manager.refresh'), translate('notice.actionSuccess'))
        }}
        onReloadRefundTasks={async () => {
          await runPageAction(async () => {
            await reloadManagerRefundTasks()
          }, translate('manager.refundTasks'), translate('notice.actionSuccess'))
        }}
        onCreateManagerFlight={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createManagerFlight({
              managerId: currentSupplierManagerSession.managerId,
              ...payload,
            })
            await reloadManagedFlights(currentSupplierManagerSession.managerId)
          }, translate('manager.createFlight'), translate('notice.actionSuccess'))
        }}
        onConfirmTask={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.confirmManagerBookingItem(payload.orderItemId, {
              managerId: currentSupplierManagerSession.managerId,
              managerType: currentSupplierManagerSession.managerType.toLowerCase(),
              note: payload.note.trim() || null,
            })
            await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
          }, translate('manager.confirm'), translate('notice.actionSuccess'))
        }}
        onRejectTask={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.rejectManagerBookingItem(payload.orderItemId, {
              managerId: currentSupplierManagerSession.managerId,
              managerType: currentSupplierManagerSession.managerType.toLowerCase(),
              reason: payload.reason,
            })
            await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
          }, translate('manager.reject'), translate('notice.actionSuccess'))
        }}
        onBatchConfirmTasks={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.batchConfirmManagerBookingItems({
              managerId: currentSupplierManagerSession.managerId,
              managerType: currentSupplierManagerSession.managerType.toLowerCase(),
              orderItemIds: payload.orderItemIds,
              note: payload.note.trim() || null,
            })
            await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
          }, translate('manager.batchConfirm'), translate('notice.actionSuccess'))
        }}
        onBatchRejectTasks={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.batchRejectManagerBookingItems({
              managerId: currentSupplierManagerSession.managerId,
              managerType: currentSupplierManagerSession.managerType.toLowerCase(),
              orderItemIds: payload.orderItemIds,
              reason: payload.reason,
            })
            await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
          }, translate('manager.batchReject'), translate('notice.actionSuccess'))
        }}
        onApproveRefundTask={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.approveRefund(
              payload.orderId,
              currentSupplierManagerSession.managerId,
              currentSupplierManagerSession.managerType.toLowerCase(),
            )
            await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
          }, translate('manager.approveRefund'), translate('notice.actionSuccess'))
        }}
        onRejectRefundTask={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.rejectRefund(
              payload.orderId,
              currentSupplierManagerSession.managerId,
              currentSupplierManagerSession.managerType.toLowerCase(),
            )
            await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
          }, translate('manager.rejectRefund'), translate('notice.actionSuccess'))
        }}
        onLogoutManager={() => {
          void runPageAction(async () => {
            await logoutManager()
          }, translate('manager.logout'), translate('notice.logoutSuccess'))
        }}
      />

      <TrainManagerPanelSection
        isVisible={shouldShowWorkspace && activeManagerType === 'train'}
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        trainAdminSession={currentTrainAdminSession}
        translate={translate}
        onRegisterRailwayManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await travelMvpApiClient.registerRailwayManager(payload)
            await loginSelectedManager('train', payload.email, payload.password)
          }, translate('trainAdmin.createAccount'), translate('notice.actionSuccess'))
        }}
        onLoginRailwayManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await loginSelectedManager('train', payload.email, payload.password)
          }, translate('trainAdmin.login'), translate('notice.actionSuccess'))
        }}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onReloadManagedTrains={async () => {
          if (!currentManagerSession) {
            return
          }
          await runPageAction(async () => {
            await reloadManagedTrains(currentManagerSession.managerId)
          }, translate('trainAdmin.refresh'), translate('notice.actionSuccess'))
        }}
        onCreateTrainJourney={async payload => {
          if (!currentTrainAdminSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createTrainJourney({
              managerId: currentTrainAdminSession.managerId,
              ...payload,
            })
            await reloadManagedTrains(currentTrainAdminSession.managerId)
          }, translate('trainAdmin.createTrain'), translate('notice.actionSuccess'))
        }}
        onLogoutRailwayManager={() => {
          void runPageAction(async () => {
            await logoutManager()
          }, translate('manager.logout'), translate('notice.logoutSuccess'))
        }}
      />

      <AttractionManagerPanelSection
        isVisible={shouldShowWorkspace && activeManagerType === 'attraction'}
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        attractionAdminSession={currentAttractionAdminSession}
        translate={translate}
        onRegisterAttractionManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await travelMvpApiClient.registerAttractionManager(payload)
            await loginSelectedManager('attraction', payload.email, payload.password)
          }, translate('attractionAdmin.createAccount'), translate('notice.actionSuccess'))
        }}
        onLoginAttractionManager={async payload => {
          await runPageAction(async () => {
            await ensureUserLoggedOut()
            await loginSelectedManager('attraction', payload.email, payload.password)
          }, translate('attractionAdmin.login'), translate('notice.actionSuccess'))
        }}
        onValidationError={message => onShowNotice('error', translate('error.friendly.default'), message)}
        onReloadManagedAttractions={async () => {
          if (!currentManagerSession) {
            return
          }
          await runPageAction(async () => {
            await reloadManagedAttractions(currentManagerSession.managerId)
          }, translate('attractionAdmin.refresh'), translate('notice.actionSuccess'))
        }}
        onCreateAttraction={async payload => {
          if (!currentAttractionAdminSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createAttraction({
              managerId: currentAttractionAdminSession.managerId,
              ...payload,
            })
            await reloadManagedAttractions(currentAttractionAdminSession.managerId)
          }, translate('attractionAdmin.createAttraction'), translate('notice.actionSuccess'))
        }}
        onCreateTicketType={async payload => {
          if (!currentAttractionAdminSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createAttractionTicketType({
              managerId: currentAttractionAdminSession.managerId,
              ...payload,
            })
            await reloadManagedAttractions(currentAttractionAdminSession.managerId)
          }, translate('attractionAdmin.createTicketType'), translate('notice.actionSuccess'))
        }}
        onCreateSession={async payload => {
          if (!currentAttractionAdminSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createAttractionTicketSession({
              managerId: currentAttractionAdminSession.managerId,
              ...payload,
            })
            await reloadManagedAttractions(currentAttractionAdminSession.managerId)
          }, translate('attractionAdmin.createSession'), translate('notice.actionSuccess'))
        }}
        onCreateRule={async payload => {
          if (!currentAttractionAdminSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.createAttractionTicketRule({
              managerId: currentAttractionAdminSession.managerId,
              ...payload,
            })
            await reloadManagedAttractions(currentAttractionAdminSession.managerId)
          }, translate('attractionAdmin.createRule'), translate('notice.actionSuccess'))
        }}
        onLogoutAttractionManager={() => {
          void runPageAction(async () => {
            await logoutManager()
          }, translate('manager.logout'), translate('notice.logoutSuccess'))
        }}
      />

      {shouldShowFeedback ? (
        <SupplierFeedbackSection
          title={translate('manager.feedback.title')}
          currentManagerSession={currentManagerSession}
          managedFlightResponses={managedFlightResponses}
          managerTaskResponses={managerTaskResponses}
          managerRefundTaskResponses={managerRefundTaskResponses}
          currentTrainAdminSession={currentTrainAdminSession}
          currentAttractionAdminSession={currentAttractionAdminSession}
          translate={translate}
        />
      ) : null}

      {shouldShowAdvertising ? (
        <AdvertisementSubmissionWorkspace
          defaultPlacement={activeManagerType === 'hotel' ? 'HotelBookingPage' : 'AttractionBookingPage'}
          defaultTargetResourceType={activeManagerType === 'hotel' ? 'Hotel' : 'Attraction'}
          resourceOptions={activeManagerType === 'hotel' ? hotelAdvertisementOptions : attractionAdvertisementOptions}
          translate={translate}
          onShowNotice={(kind, title, description) => onShowNotice(kind, title, description)}
          onOpenResource={resourceId => {
            void resourceId
            onNavigate(activeManagerType === 'hotel' ? 'hotels' : 'attractions')
          }}
        />
      ) : null}


      {shouldShowSiteAdminPanel ? (
        <SiteAdminPanel
          currentManagerSession={currentManagerSession}
          section={activeSection === 'advertisingReview' ? 'advertisingReview' : 'blogAudit'}
          translate={translate}
        />
      ) : null}
    </>
  )
}

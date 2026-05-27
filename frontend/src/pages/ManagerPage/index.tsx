import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
import type { ManagerCenterSectionKey } from '@/pages/ManagerPage/sections/ManagerCenterSections'
import { useEffect, useMemo, useRef, useState } from 'react'

import { usePageActions } from '@/pages/shared/usePageActions'
import { AdvertisementSubmissionWorkspace } from '@/pages/ManagerPage/components/advertising/AdvertisementSubmissionWorkspace'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, AttractionAdminSessionResponse, CurrentManagerSessionResponse, FlightPlannerResponse, HotelPlannerResponse, ManagerFlightOrderResponse, ManagerRefundTaskResponse, ManagerSessionResponse, ManagerTaskResponse, TrainAdminSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import { getPasswordValidationMessage } from '@/pages/shared/auth/passwordValidation'
import { SiteAdminPanel, SupplierFeedbackSection } from '@/pages/ManagerPage/sections/ManagerCenterSections'
import { AttractionManagerPanelSection, SupplierManagerPanelSection, TrainManagerPanelSection } from '@/pages/ManagerPage/sections/ManagerPagePanels'
import { HotelProfileSection } from '@/pages/ManagerPage/components/managers/manager-panel-workspace'
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
  initialAuthMode: ManagerAuthMode
  registerFields: Array<{ label: string; name: string; type?: string }>
  loginManagerType: LoginManagerType
  isBusy: boolean
  onValidationError: (message: string) => void
  onRegister: (payload: Record<string, string>) => Promise<void>
  onLogin: (payload: { managerType: LoginManagerType; email: string; password: string }) => Promise<void>
  onBack: () => void
  translate: (translationKey: string) => string
}

type ManagerAuthMode = 'register' | 'login'
type BusinessManagerType = Exclude<LoginManagerType, 'siteAdmin'>

type ManagerEntryCardProps = {
  title: string
  shortTitle: string
  accentClassName: string
  imageSrc: string
  imageAlt: string
  onSelect: (authMode: ManagerAuthMode) => void
}

const managerPageShellClassName = 'grid gap-6 px-6 py-8 text-slate-950'
const managerHeaderClassName = 'grid gap-2'
const managerEyebrowClassName = 'text-sm font-bold text-slate-500'
const managerTitleClassName = 'm-0 text-3xl font-bold leading-tight text-slate-950'
const managerCopyClassName = 'm-0 max-w-3xl text-base leading-7 text-slate-600'
const managerEntryGridClassName = 'grid gap-10 md:grid-cols-2 xl:grid-cols-4'
const managerAuthCardClassName = 'mx-auto grid w-full gap-5 self-start border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/70 md:w-1/2 md:max-w-3xl'
const managerActionsClassName = 'flex flex-wrap items-center gap-3'
const managerFormGridClassName = 'grid gap-4'
const managerFormTitleClassName = 'm-0 text-xl font-bold text-slate-950'
const managerLabelClassName = 'grid gap-2 text-sm font-medium text-slate-700'
const managerInputClassName =
  'min-h-11 border border-slate-300 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black focus:ring-2 focus:ring-slate-200'
const managerSecondaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'
const managerAuthPrimaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55'

const managerBusinessEntries: Array<{
  managerType: BusinessManagerType
  titleKey: string
  shortTitle: string
  accentClassName: string
  imageSrc: string
}> = [
  {
    managerType: 'airline',
    titleKey: 'manager.type.airline',
    shortTitle: 'AIR',
    accentClassName: 'bg-sky-500',
    imageSrc: '/images/manager-entry/airline.jpg',
  },
  {
    managerType: 'hotel',
    titleKey: 'manager.type.hotel',
    shortTitle: 'HOTEL',
    accentClassName: 'bg-cyan-500',
    imageSrc: '/images/home-hero-candidates/01_大海_葡萄牙Praia da Marinha_海与岩壁在这里相爱.jpg',
  },
  {
    managerType: 'train',
    titleKey: 'manager.type.train',
    shortTitle: 'TRAIN',
    accentClassName: 'bg-indigo-500',
    imageSrc: '/images/manager-entry/train.jpg',
  },
  {
    managerType: 'attraction',
    titleKey: 'manager.type.attraction',
    shortTitle: 'VIEW',
    accentClassName: 'bg-emerald-500',
    imageSrc: '/images/home-hero-candidates/04_大山_瑞士Oeschinensee_湖光把山色轻轻收藏.jpg',
  },
]

function ManagerEntryCard({ title, shortTitle, accentClassName, imageSrc, imageAlt, onSelect }: ManagerEntryCardProps) {
  const [isFlipped, setIsFlipped] = useState(false)

  return (
    <article className="grid justify-items-center gap-6">
      <div className="grid justify-items-center gap-8">
        <div
          className="h-40 w-40 [perspective:1200px]"
          onMouseEnter={() => setIsFlipped(true)}
          onMouseLeave={() => setIsFlipped(false)}
          onFocus={() => setIsFlipped(true)}
          onBlur={() => setIsFlipped(false)}
        >
          <div
            className="relative h-full w-full transition-transform duration-500 [transform-style:preserve-3d]"
            style={{ transform: isFlipped ? 'rotate(45deg) rotateY(180deg)' : 'rotate(45deg)' }}
          >
            <div className={`absolute inset-0 grid place-items-center shadow-xl shadow-slate-300/60 [backface-visibility:hidden] ${accentClassName}`}>
              <div className="-rotate-45 grid justify-items-center gap-2 text-center text-white">
                <span className="text-2xl font-bold tracking-wide">{shortTitle}</span>
                <span className="max-w-28 text-base font-bold leading-tight">{title}</span>
              </div>
            </div>

            <div className={`absolute inset-0 grid place-items-center overflow-hidden text-white shadow-xl shadow-slate-300/80 [backface-visibility:hidden] [transform:rotateY(180deg)] ${accentClassName}`}>
              <div className="-rotate-45 grid h-full w-full grid-cols-[1fr_auto_1fr] items-center px-3">
                <button
                  type="button"
                  className="grid h-full w-full place-items-center border-0 bg-transparent p-0 font-['SimSun','宋体',serif] text-lg font-bold text-white shadow-none transition hover:text-slate-950 focus:text-slate-950"
                  onClick={() => onSelect('register')}
                >
                  注册
                </button>
                <img
                  src="/images/manager-entry/fly-bara-entry.png"
                  alt=""
                  aria-hidden="true"
                  className="h-11 w-11 object-contain"
                />
                <button
                  type="button"
                  className="grid h-full w-full place-items-center border-0 bg-transparent p-0 font-['SimSun','宋体',serif] text-lg font-bold text-white shadow-none transition hover:text-slate-950 focus:text-slate-950"
                  onClick={() => onSelect('login')}
                >
                  登录
                </button>
              </div>
            </div>
          </div>
        </div>
        <h3 className="m-0 text-center text-2xl font-bold leading-tight text-slate-700">{title}</h3>
      </div>

      <img
        src={imageSrc}
        alt={imageAlt}
        className="h-52 w-full border border-slate-200 object-cover shadow-sm shadow-slate-200/70"
      />
    </article>
  )
}

function ManagerAuthCard({
  title,
  registerTitle,
  loginTitle,
  initialAuthMode,
  registerFields,
  loginManagerType,
  isBusy,
  onValidationError,
  onRegister,
  onLogin,
  onBack,
  translate,
}: ManagerAuthCardProps) {
  const [authMode, setAuthMode] = useState<ManagerAuthMode>(initialAuthMode)
  const registerFormRef = useRef<HTMLFormElement>(null)
  const loginFormRef = useRef<HTMLFormElement>(null)

  function resetManagerAuthForms() {
    registerFormRef.current?.reset()
    loginFormRef.current?.reset()
  }

  useEffect(() => {
    setAuthMode(initialAuthMode)
    resetManagerAuthForms()
  }, [initialAuthMode, loginManagerType])

  return (
    <article className={managerAuthCardClassName}>
      <div className={managerHeaderClassName}>
        <p className={managerEyebrowClassName}>{translate('nav.managerCenter')}</p>
        <h3 className={managerTitleClassName}>{title}</h3>
      </div>

      <div className={managerActionsClassName}>
        <button type="button" className={managerSecondaryButtonClassName} onClick={onBack}>
          {translate('manager.backToCategories')}
        </button>
        <button
          type="button"
          className={authMode === 'register' ? managerAuthPrimaryButtonClassName : managerSecondaryButtonClassName}
          onClick={() => {
            setAuthMode('register')
            resetManagerAuthForms()
          }}
        >
          {translate('manager.createAccount')}
        </button>
        <button
          type="button"
          className={authMode === 'login' ? managerAuthPrimaryButtonClassName : managerSecondaryButtonClassName}
          onClick={() => {
            setAuthMode('login')
            resetManagerAuthForms()
          }}
        >
          {translate('manager.login')}
        </button>
      </div>

      {authMode === 'register' ? (
        <form
        ref={registerFormRef}
        className={managerFormGridClassName}
        autoComplete="off"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const password = String(formData.get('password') ?? '')
          const confirmPassword = String(formData.get('confirmPassword') ?? '')
          if (password !== confirmPassword) {
            onValidationError(translate('error.passwordMismatch'))
            return
          }
          const email = String(formData.get('email') ?? '').trim()
          const passwordValidationMessage = getPasswordValidationMessage(password, email)
          if (passwordValidationMessage) {
            onValidationError(passwordValidationMessage)
            return
          }

          const payload = Object.fromEntries(formData.entries()) as Record<string, string>
          await onRegister(payload)
          resetManagerAuthForms()
          event.currentTarget.reset()
        }}
      >
        <h4 className={managerFormTitleClassName}>{registerTitle}</h4>
        {registerFields.map(field => (
          <label key={field.name} className={managerLabelClassName}>
            {field.label}
            <input
              className={managerInputClassName}
              name={field.name}
              type={field.type ?? 'text'}
              autoComplete="off"
              required
              disabled={isBusy}
            />
          </label>
        ))}
        <label className={managerLabelClassName}>
          {translate('account.password')}
          <input className={managerInputClassName} name="password" type="password" autoComplete="new-password" required disabled={isBusy} />
        </label>
        <label className={managerLabelClassName}>
          {translate('account.confirmPassword')}
          <input className={managerInputClassName} name="confirmPassword" type="password" autoComplete="new-password" required disabled={isBusy} />
        </label>
        <button type="submit" className={managerAuthPrimaryButtonClassName} disabled={isBusy}>
          {translate('manager.createAccount')}
        </button>
        </form>
      ) : null}

      {authMode === 'login' ? (
        <form
        ref={loginFormRef}
        className={managerFormGridClassName}
        autoComplete="off"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          await onLogin({
            managerType: loginManagerType,
            email: String(formData.get('email') ?? '').trim(),
            password: String(formData.get('password') ?? ''),
          })
          resetManagerAuthForms()
        }}
      >
        <h4 className={managerFormTitleClassName}>{loginTitle}</h4>
        <label className={managerLabelClassName}>
          {translate('manager.email')}
          <input className={managerInputClassName} name="email" type="email" autoComplete="off" required disabled={isBusy} />
        </label>
        <label className={managerLabelClassName}>
          {translate('account.password')}
          <input className={managerInputClassName} name="password" type="password" autoComplete="off" required disabled={isBusy} />
        </label>
        <button type="submit" className={managerAuthPrimaryButtonClassName} disabled={isBusy}>
          {translate('manager.login')}
        </button>
        </form>
      ) : null}
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
      { label: translate('manager.airlineCode'), name: 'airlineCode' },
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
      { label: translate('trainAdmin.operatorCode'), name: 'operatorCode' },
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
    { label: translate('attractionAdmin.displayName'), name: 'displayName' },
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

function toInitialAirlineSection(currentViewKey: AppViewKey) {
  if (currentViewKey === 'managerCreateFlight') {
    return 'createFlight' as const
  }
  if (currentViewKey === 'managerFeedback') {
    return 'userFeedback' as const
  }
  if (currentViewKey === 'managerProfile') {
    return 'managerProfile' as const
  }
  return 'flightManagement' as const
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
  const [selectedEntryAuthMode, setSelectedEntryAuthMode] = useState<ManagerAuthMode>('register')
  const [currentSupplierManagerSession, setCurrentSupplierManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [managedFlightPlannerResponses, setManagedFlightPlannerResponses] = useState<FlightPlannerResponse[]>([])
  const [managedHotelPlannerResponses, setManagedHotelPlannerResponses] = useState<HotelPlannerResponse[]>([])
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

  async function reloadManagedFlights(
    managerId: string,
    filters: {
      departureAirports?: string[]
      arrivalAirports?: string[]
      departureDate?: string
      timeRange?: string
      sortDirection?: 'asc' | 'desc'
    } = {},
  ) {
    const flightListResponse = await travelMvpApiClient.listManagerFlights(managerId, filters)
    setManagedFlightPlannerResponses(flightListResponse.flights)
  }

  async function loadManagerFlightOrders(flightId: string): Promise<ManagerFlightOrderResponse[]> {
    if (!currentSupplierManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    const response = await travelMvpApiClient.listManagerFlightOrders(currentSupplierManagerSession.managerId, flightId)
    return response.orders
  }

  async function reloadManagedHotels(managerId: string) {
    const hotelListResponse = await travelMvpApiClient.listManagedHotels(managerId)
    setManagedHotelPlannerResponses(hotelListResponse.hotels)
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
        setManagedFlightPlannerResponses([])
        setManagedHotelPlannerResponses([])
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
          managerType === 'airline'
            ? Promise.resolve(setManagerTaskResponses([]))
            : reloadManagerTasks({ status: 'pending', resourceType: 'all' }, legacySession),
          managerType === 'airline'
            ? Promise.resolve(setManagerRefundTaskResponses([]))
            : reloadManagerRefundTasks(legacySession),
          managerType === 'airline'
            ? reloadManagedFlights(currentManagerSession.managerId)
            : Promise.resolve(setManagedFlightPlannerResponses([])),
          managerType === 'hotel'
            ? reloadManagedHotels(currentManagerSession.managerId)
            : Promise.resolve(setManagedHotelPlannerResponses([])),
          managerType === 'attraction'
            ? reloadManagedAttractions(currentManagerSession.managerId, currentManagerSession)
            : Promise.resolve(),
        ])
      } else if (managerType === 'train') {
        setCurrentSupplierManagerSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightPlannerResponses([])
        setManagedHotelPlannerResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        await reloadManagedTrains(currentManagerSession.managerId, currentManagerSession)
      } else if (managerType === 'siteAdmin') {
        setCurrentSupplierManagerSession(null)
        setCurrentTrainAdminSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightPlannerResponses([])
        setManagedHotelPlannerResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
      }
    })()
  }, [currentManagerSession])

  async function logoutManager() {
    await travelMvpApiClient.logoutManagerAuth()
    onManagerSessionChange(null)
    setSelectedEntryType(null)
    setSelectedEntryAuthMode('register')
    setCurrentSupplierManagerSession(null)
    setCurrentTrainAdminSession(null)
    setCurrentAttractionAdminSession(null)
    setManagedFlightPlannerResponses([])
    setManagedHotelPlannerResponses([])
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

    if (managedHotelPlannerResponses.length > 0) {
      return managedHotelPlannerResponses.map(hotel => ({
        value: hotel.hotelId,
        label: `${hotel.hotelName} · ${hotel.location}`,
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
  }, [activeManagerType, currentManagerSession?.scopeId, currentManagerSession?.displayName, managedHotelPlannerResponses])

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
  const shouldShowWorkspace =
    !isSiteAdmin &&
    (activeSection === 'workspace' || (activeManagerType === 'airline' && activeSection === 'feedback')) &&
    !(activeManagerType === 'hotel' && currentViewKey === 'managerProfile')
  const shouldShowFeedback = !isSiteAdmin && activeSection === 'feedback' && activeManagerType !== 'airline'
  const shouldShowAdvertising = !isSiteAdmin && activeSection === 'advertising' && canSubmitAdvertisements
  const shouldShowHotelProfile = !isSiteAdmin && currentViewKey === 'managerProfile' && activeManagerType === 'hotel'
  const shouldShowSiteAdminPanel = isSiteAdmin && (activeSection === 'blogAudit' || activeSection === 'advertisingReview')

  if (!currentManagerSession) {
    if (selectedEntryType) {
      const entryTitle = getManagerEntryTitle(selectedEntryType, translate)

      return (
        <section className={managerPageShellClassName}>
          <ManagerAuthCard
            key={selectedEntryType}
            title={entryTitle}
            registerTitle={getManagerRegisterTitle(selectedEntryType, translate)}
            loginTitle={getManagerLoginTitle(selectedEntryType, translate)}
            initialAuthMode={selectedEntryAuthMode}
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
                setSelectedEntryAuthMode('register')
              }, translate('manager.createAccount'), translate('notice.actionSuccess'))
            }}
            onLogin={async payload => {
              await runPageAction(async () => {
                await ensureUserLoggedOut()
                await loginSelectedManager(payload.managerType, payload.email, payload.password)
                setSelectedEntryType(null)
                setSelectedEntryAuthMode('register')
              }, translate('manager.login'), translate('notice.actionSuccess'))
            }}
            onBack={() => {
              setSelectedEntryType(null)
              setSelectedEntryAuthMode('register')
            }}
            translate={translate}
          />
        </section>
      )
    }

    return (
      <section className={managerPageShellClassName}>
        <div className={managerHeaderClassName}>
          <p className={managerEyebrowClassName}>{translate('nav.managerCenter')}</p>
          <h2 className={managerTitleClassName}>{translate('manager.title')}</h2>
          <p className={managerCopyClassName}>{translate('manager.centerDescription')}</p>
        </div>

        <div className={managerEntryGridClassName}>
          {managerBusinessEntries.map(entry => (
            <ManagerEntryCard
              key={entry.managerType}
              title={translate(entry.titleKey)}
              shortTitle={entry.shortTitle}
              accentClassName={entry.accentClassName}
              imageSrc={entry.imageSrc}
              imageAlt={translate(entry.titleKey)}
              onSelect={authMode => {
                setSelectedEntryType(entry.managerType)
                setSelectedEntryAuthMode(authMode)
              }}
            />
          ))}
        </div>
      </section>
    )
  }

  return (
    <>
      <SupplierManagerPanelSection
        isVisible={shouldShowWorkspace && (activeManagerType === 'airline' || activeManagerType === 'hotel')}
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        managerSession={currentSupplierManagerSession}
        managedFlights={managedFlightPlannerResponses}
        managedHotels={managedHotelPlannerResponses}
        managerTasks={managerTaskResponses}
        managerRefundTasks={managerRefundTaskResponses}
        initialAirlineSection={toInitialAirlineSection(currentViewKey)}
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
        onToggleManagerFlightStatus={async flightId => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            await travelMvpApiClient.toggleManagerFlightStatus({
              managerId: currentSupplierManagerSession.managerId,
              flightId,
            })
            await reloadManagedFlights(currentSupplierManagerSession.managerId)
          }, '切换航班状态', translate('notice.actionSuccess'))
        }}
        onSearchManagerFlights={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await reloadManagedFlights(currentSupplierManagerSession.managerId, payload)
        }}
        onLoadManagerFlightOrders={loadManagerFlightOrders}
        onUpdateAirlineManagerProfile={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            const nextSession = await travelMvpApiClient.updateAirlineManagerProfile({
              managerId: currentSupplierManagerSession.managerId,
              ...payload,
            })
            setCurrentSupplierManagerSession(nextSession)
            if (currentManagerSession) {
              onManagerSessionChange({
                ...currentManagerSession,
                displayName: nextSession.displayName,
                status: nextSession.status,
                scopeId: nextSession.scopeId,
              })
            }
            await reloadManagedFlights(currentSupplierManagerSession.managerId)
          }, '保存资料', translate('notice.actionSuccess'))
        }}
        onUpdateHotelManagerProfile={async payload => {
          if (!currentSupplierManagerSession) {
            throw new Error(translate('error.managerNotFound'))
          }
          await runPageAction(async () => {
            const nextSession = await travelMvpApiClient.updateHotelManagerProfile(payload)
            setCurrentSupplierManagerSession(nextSession)
            if (currentManagerSession) {
              onManagerSessionChange({
                ...currentManagerSession,
                email: nextSession.email,
                displayName: nextSession.displayName,
                status: nextSession.status,
                scopeId: nextSession.scopeId,
              })
            }
            await reloadManagedHotels(currentSupplierManagerSession.managerId)
          }, '保存资料', translate('notice.actionSuccess'))
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

      {shouldShowHotelProfile && currentSupplierManagerSession ? (
        <HotelProfileSection
          currentLanguage={currentLanguage}
          managerSession={currentSupplierManagerSession}
          managedHotels={managedHotelPlannerResponses}
          translate={translate}
          onUpdateHotelManagerProfile={async payload => {
            if (!currentSupplierManagerSession) {
              throw new Error(translate('error.managerNotFound'))
            }
            await runPageAction(async () => {
              const nextSession = await travelMvpApiClient.updateHotelManagerProfile(payload)
              setCurrentSupplierManagerSession(nextSession)
              if (currentManagerSession) {
                onManagerSessionChange({
                  ...currentManagerSession,
                  email: nextSession.email,
                  displayName: nextSession.displayName,
                  status: nextSession.status,
                  scopeId: nextSession.scopeId,
                })
              }
              await reloadManagedHotels(currentSupplierManagerSession.managerId)
            }, '保存资料', translate('notice.actionSuccess'))
          }}
          onLogoutManager={() => {
            void runPageAction(async () => {
              await logoutManager()
            }, translate('manager.logout'), translate('notice.logoutSuccess'))
          }}
        />
      ) : null}

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
          managedFlightPlannerResponses={managedFlightPlannerResponses}
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

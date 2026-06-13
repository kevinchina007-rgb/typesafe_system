import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import type { LoginManagerType, ManagerPageProps } from '../objects'
import type { ManagerPageControllerCoreState } from './useManagerPageControllerCore'

export type ManagerPageAuthActions = {
  runAction: (action: () => Promise<void>, actionLabel: string, successLabel?: string | undefined) => Promise<void>
  selectEntry: (entryType: LoginManagerType, authMode: 'register' | 'login') => void
  clearSelectedEntry: () => void
  logoutManager: () => Promise<void>
  ensureUserLoggedOut: () => Promise<void>
  loginSelectedManager: (managerType: LoginManagerType, email: string, password: string) => Promise<void>
  registerSelectedManager: (managerType: LoginManagerType, payload: Record<string, string>) => Promise<void>
}

type ManagerPageAuthActionDependencies = ManagerPageProps &
  Pick<
    ManagerPageControllerCoreState,
    | 'setSelectedEntryType'
    | 'setSelectedEntryAuthMode'
    | 'setCurrentSupplierManagerSession'
    | 'setCurrentTrainAdminSession'
    | 'setCurrentAttractionAdminSession'
    | 'setManagedFlightPlannerResponses'
    | 'setManagedHotelPlannerResponses'
    | 'setManagerTaskResponses'
    | 'setManagerRefundTaskResponses'
    | 'runPageAction'
  > & {
    onManagerSessionChange: (managerSession: CurrentManagerSessionResponse | null) => void
    onSignedInUserChange: (user: UserResponse | null) => void
  }

export function createManagerPageControllerAuthActions({
  signedInUser,
  translate,
  onManagerSessionChange,
  onSignedInUserChange,
  onNavigate,
  setSelectedEntryType,
  setSelectedEntryAuthMode,
  setCurrentSupplierManagerSession,
  setCurrentTrainAdminSession,
  setCurrentAttractionAdminSession,
  setManagedFlightPlannerResponses,
  setManagedHotelPlannerResponses,
  setManagerTaskResponses,
  setManagerRefundTaskResponses,
  runPageAction,
}: ManagerPageAuthActionDependencies): ManagerPageAuthActions {
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

  async function runAction(action: () => Promise<void>, actionLabel: string, successLabel?: string) {
    await runPageAction(action, actionLabel, successLabel ?? translate('notice.actionSuccess'))
  }

  return {
    runAction,
    selectEntry: (entryType: LoginManagerType, authMode: 'register' | 'login') => {
      setSelectedEntryType(entryType)
      setSelectedEntryAuthMode(authMode)
    },
    clearSelectedEntry: () => {
      setSelectedEntryType(null)
      setSelectedEntryAuthMode('register')
    },
    logoutManager,
    ensureUserLoggedOut,
    loginSelectedManager,
    registerSelectedManager,
  }
}

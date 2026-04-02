import { useEffect, useState } from 'react'

import { AttractionAdminPanel } from '../../components/AttractionAdminPanel'
import { ManagerPanel } from '../../components/ManagerPanel'
import { TrainAdminPanel } from '../../components/TrainAdminPanel'
import { travelMvpApiClient } from '../../lib/api-client'
import type {
  AppLanguage,
  AttractionAdminSessionResponse,
  CurrentManagerSessionResponse,
  FlightResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  TrainAdminSessionResponse,
} from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'

type ManagerPageProps = {
  currentLanguage: AppLanguage
  currentManagerSession: CurrentManagerSessionResponse | null
  translate: (translationKey: string) => string
  onManagerSessionChange: (managerSession: CurrentManagerSessionResponse | null) => void
  onShowNotice: PageNoticeHandler
}

function toLegacyManagerSession(session: CurrentManagerSessionResponse): ManagerSessionResponse {
  return {
    managerId: session.managerId,
    managerType: session.managerType,
    email: session.email,
    displayName: session.displayName,
    status: session.status,
    scopeId: session.scopeId,
    createdAt: session.createdAt,
  }
}

function toManagerTypeKey(managerType: string): 'airline' | 'hotel' | 'train' | 'attraction' {
  switch (managerType) {
    case 'Airline':
      return 'airline'
    case 'Hotel':
      return 'hotel'
    case 'Train':
      return 'train'
    case 'Attraction':
      return 'attraction'
    default:
      return 'airline'
  }
}

export function ManagerPage({
  currentLanguage,
  currentManagerSession,
  translate,
  onManagerSessionChange,
  onShowNotice,
}: ManagerPageProps) {
  const activeManagerType = currentManagerSession ? toManagerTypeKey(currentManagerSession.managerType) : null
  const [currentSupplierManagerSession, setCurrentSupplierManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [managedFlightResponses, setManagedFlightResponses] = useState<FlightResponse[]>([])
  const [currentTrainAdminSession, setCurrentTrainAdminSession] = useState<TrainAdminSessionResponse | null>(null)
  const [currentAttractionAdminSession, setCurrentAttractionAdminSession] = useState<AttractionAdminSessionResponse | null>(null)
  const [managerTaskResponses, setManagerTaskResponses] = useState<ManagerTaskResponse[]>([])
  const [managerRefundTaskResponses, setManagerRefundTaskResponses] = useState<ManagerRefundTaskResponse[]>([])
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  async function reloadManagerTasks(taskStatus: 'pending' | 'all' | 'confirmed' | 'rejected' = 'pending', session?: ManagerSessionResponse) {
    const effectiveSession = session ?? currentSupplierManagerSession
    if (!effectiveSession) {
      return
    }

    const taskListResponse = await travelMvpApiClient.listManagerTasks({
      managerId: effectiveSession.managerId,
      managerType: effectiveSession.managerType.toLowerCase(),
      status: taskStatus,
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
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        return
      }

      const managerType = toManagerTypeKey(currentManagerSession.managerType)

      if (managerType === 'airline' || managerType === 'hotel') {
        const legacySession = toLegacyManagerSession(currentManagerSession)
        setCurrentSupplierManagerSession(legacySession)
        setCurrentTrainAdminSession(null)
        setCurrentAttractionAdminSession(null)
        await Promise.all([
          reloadManagerTasks('pending', legacySession),
          reloadManagerRefundTasks(legacySession),
          managerType === 'airline' ? reloadManagedFlights(currentManagerSession.managerId) : Promise.resolve(setManagedFlightResponses([])),
        ])
      } else if (managerType === 'train') {
        setCurrentSupplierManagerSession(null)
        setCurrentAttractionAdminSession(null)
        setManagedFlightResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        await reloadManagedTrains(currentManagerSession.managerId, currentManagerSession)
      } else {
        setCurrentSupplierManagerSession(null)
        setCurrentTrainAdminSession(null)
        setManagedFlightResponses([])
        setManagerTaskResponses([])
        setManagerRefundTaskResponses([])
        await reloadManagedAttractions(currentManagerSession.managerId, currentManagerSession)
      }
    })()
  }, [currentManagerSession])

  async function logoutManager() {
    await travelMvpApiClient.logoutManagerAuth()
    onManagerSessionChange(null)
    setCurrentSupplierManagerSession(null)
    setCurrentTrainAdminSession(null)
    setCurrentAttractionAdminSession(null)
    setManagedFlightResponses([])
    setManagerTaskResponses([])
    setManagerRefundTaskResponses([])
  }

  return (
    <>
      {!activeManagerType || activeManagerType === 'airline' || activeManagerType === 'hotel' ? (
        <ManagerPanel
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          managerSession={currentSupplierManagerSession}
          managedFlights={managedFlightResponses}
          managerTasks={managerTaskResponses}
          managerRefundTasks={managerRefundTaskResponses}
          translate={translate}
          onRegisterAirlineManager={async payload => {
            await runPageAction(async () => {
              await travelMvpApiClient.registerAirlineManager(payload)
              const session = await travelMvpApiClient.loginManagerAuth({
                managerType: 'airline',
                email: payload.email,
                password: payload.password,
              })
              onManagerSessionChange(session)
            }, translate('manager.createAccount'), translate('notice.actionSuccess'))
          }}
          onRegisterHotelManager={async payload => {
            await runPageAction(async () => {
              await travelMvpApiClient.registerHotelManager(payload)
              const session = await travelMvpApiClient.loginManagerAuth({
                managerType: 'hotel',
                email: payload.email,
                password: payload.password,
              })
              onManagerSessionChange(session)
            }, translate('manager.createAccount'), translate('notice.actionSuccess'))
          }}
          onCreateManagerRoomType={async payload => {
            await runPageAction(async () => {
              await travelMvpApiClient.createManagerRoomType(payload)
            }, translate('manager.createRoomType'), translate('notice.actionSuccess'))
          }}
          onLoginManager={async payload => {
            await runPageAction(async () => {
              const session = await travelMvpApiClient.loginManagerAuth(payload)
              onManagerSessionChange(session)
            }, translate('manager.login'), translate('notice.actionSuccess'))
          }}
          onValidationError={message => {
            onShowNotice('error', translate('error.friendly.default'), message)
          }}
          onReloadTasks={async status => {
            await runPageAction(async () => {
              await reloadManagerTasks(status)
              if (currentSupplierManagerSession?.managerType === 'Airline') {
                await reloadManagedFlights(currentSupplierManagerSession.managerId)
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
      ) : null}

      {!activeManagerType || activeManagerType === 'train' ? (
        <TrainAdminPanel
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          trainAdminSession={currentTrainAdminSession}
          translate={translate}
          onRegisterRailwayManager={async payload => {
            await runPageAction(async () => {
              await travelMvpApiClient.registerRailwayManager(payload)
              const session = await travelMvpApiClient.loginManagerAuth({
                managerType: 'train',
                email: payload.email,
                password: payload.password,
              })
              onManagerSessionChange(session)
            }, translate('trainAdmin.createAccount'), translate('notice.actionSuccess'))
          }}
          onLoginRailwayManager={async payload => {
            await runPageAction(async () => {
              const session = await travelMvpApiClient.loginManagerAuth({
                managerType: 'train',
                email: payload.email,
                password: payload.password,
              })
              onManagerSessionChange(session)
            }, translate('trainAdmin.login'), translate('notice.actionSuccess'))
          }}
          onValidationError={message => {
            onShowNotice('error', translate('error.friendly.default'), message)
          }}
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
      ) : null}

      {!activeManagerType || activeManagerType === 'attraction' ? (
        <AttractionAdminPanel
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          attractionAdminSession={currentAttractionAdminSession}
          translate={translate}
          onRegisterAttractionManager={async payload => {
            await runPageAction(async () => {
              await travelMvpApiClient.registerAttractionManager(payload)
              const session = await travelMvpApiClient.loginManagerAuth({
                managerType: 'attraction',
                email: payload.email,
                password: payload.password,
              })
              onManagerSessionChange(session)
            }, translate('attractionAdmin.createAccount'), translate('notice.actionSuccess'))
          }}
          onLoginAttractionManager={async payload => {
            await runPageAction(async () => {
              const session = await travelMvpApiClient.loginManagerAuth({
                managerType: 'attraction',
                email: payload.email,
                password: payload.password,
              })
              onManagerSessionChange(session)
            }, translate('attractionAdmin.login'), translate('notice.actionSuccess'))
          }}
          onValidationError={message => {
            onShowNotice('error', translate('error.friendly.default'), message)
          }}
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
      ) : null}
    </>
  )
}

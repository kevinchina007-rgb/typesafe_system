import { useEffect, useState } from 'react'

import { AppSidebar } from '../components/AppSidebar'
import { FlightsPanel } from '../components/FlightsPanel'
import { HotelsPanel } from '../components/HotelsPanel'
import { ManagerPanel } from '../components/ManagerPanel'
import { OrderPanel } from '../components/OrderPanel'
import { PaymentModal } from '../components/PaymentModal'
import { TrainAdminPanel } from '../components/TrainAdminPanel'
import { TrainsPanel } from '../components/TrainsPanel'
import { ToastNotice } from '../components/ToastNotice'
import { TravelerPanel } from '../components/TravelerPanel'
import { UserPanel } from '../components/UserPanel'
import { travelMvpApiClient } from '../lib/api-client'
import { createTranslator } from '../lib/i18n'
import type {
  AppLanguage,
  AppNotice,
  AppViewKey,
  FlightResponse,
  HealthResponse,
  HotelResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  OrderResponse,
  TrainAdminSessionResponse,
  TrainResponse,
  TravelerResponse,
  UserResponse,
} from '../lib/mvp-types'
import { mapTechnicalErrorToFriendlyMessage } from '../lib/view-models'
import { ExplorePage } from './ExplorePage'

export function MvpApp() {
  const [currentLanguage, setCurrentLanguage] = useState<AppLanguage>('en')
  const [currentViewKey, setCurrentViewKey] = useState<AppViewKey>('explore')
  const [accountEntryMode, setAccountEntryMode] = useState<'register' | 'login'>('login')
  const [backendHealthResponse, setBackendHealthResponse] = useState<HealthResponse | null>(null)
  const [signedInUserResponse, setSignedInUserResponse] = useState<UserResponse | null>(null)
  const [travelerResponses, setTravelerResponses] = useState<TravelerResponse[]>([])
  const [orderResponses, setOrderResponses] = useState<OrderResponse[]>([])
  const [currentManagerSession, setCurrentManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [currentTrainAdminSession, setCurrentTrainAdminSession] = useState<TrainAdminSessionResponse | null>(null)
  const [managerTaskResponses, setManagerTaskResponses] = useState<ManagerTaskResponse[]>([])
  const [managerRefundTaskResponses, setManagerRefundTaskResponses] = useState<ManagerRefundTaskResponse[]>([])
  const [pendingPaymentOrder, setPendingPaymentOrder] = useState<OrderResponse | null>(null)
  const [isPageBusy, setIsPageBusy] = useState(false)
  const [loginEmailDraft, setLoginEmailDraft] = useState('')
  const [currentNotice, setCurrentNotice] = useState<AppNotice | null>(null)

  const translate = createTranslator(currentLanguage)
  const isGuestMode = signedInUserResponse === null

  useEffect(() => {
    void loadBackendHealth()
  }, [])

  function showNotice(kind: AppNotice['kind'], title: string, description: string, technicalMessage?: string) {
    setCurrentNotice({
      id: Date.now(),
      kind,
      title,
      description,
      technicalMessage,
    })
  }

  async function runPageAction(action: () => Promise<void>, successTitle: string, successDescription: string) {
    setIsPageBusy(true)
    try {
      await action()
      showNotice('success', successTitle, successDescription)
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      showNotice(
        'error',
        translate('error.friendly.default'),
        mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage),
        technicalMessage,
      )
    } finally {
      setIsPageBusy(false)
    }
  }

  async function loadBackendHealth() {
    try {
      const healthResponse = await travelMvpApiClient.getHealth()
      setBackendHealthResponse(healthResponse)
    } catch {
      setBackendHealthResponse(null)
    }
  }

  async function reloadCurrentUser() {
    if (!signedInUserResponse) {
      return
    }

    const userResponse = await travelMvpApiClient.getUser(signedInUserResponse.userId)
    setSignedInUserResponse(userResponse)
  }

  async function reloadTravelerList() {
    if (!signedInUserResponse) {
      return
    }

    const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUserResponse.userId)
    setTravelerResponses(travelerListResponse.travelers)
  }

  async function reloadOrders() {
    if (!signedInUserResponse) {
      return
    }

    const orderListResponse = await travelMvpApiClient.listOrders(signedInUserResponse.userId)
    setOrderResponses(orderListResponse.orders)
  }

  async function reloadManagerTasks(taskStatus: 'pending' | 'all' | 'confirmed' | 'rejected' = 'pending') {
    if (!currentManagerSession) {
      return
    }

    const taskListResponse = await travelMvpApiClient.listManagerTasks({
      managerId: currentManagerSession.managerId,
      managerType: currentManagerSession.managerType.toLowerCase(),
      status: taskStatus,
    })
    setManagerTaskResponses(taskListResponse.tasks)
  }

  async function reloadManagerRefundTasks() {
    if (!currentManagerSession) {
      return
    }

    const refundTaskListResponse = await travelMvpApiClient.listManagerRefundTasks({
      managerId: currentManagerSession.managerId,
      managerType: currentManagerSession.managerType.toLowerCase(),
    })
    setManagerRefundTaskResponses(refundTaskListResponse.tasks)
  }

  function requireSignedInUser() {
    if (!signedInUserResponse) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUserResponse
  }

  function requireManagerSession() {
    if (!currentManagerSession) {
      throw new Error(translate('error.managerNotFound'))
    }
    return currentManagerSession
  }

  async function searchFlights(payload: {
    departureAirport?: string
    arrivalAirport?: string
    date?: string
  }): Promise<FlightResponse[]> {
    try {
      const flightListResponse = await travelMvpApiClient.listFlights(payload)
      return flightListResponse.flights
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      showNotice('error', translate('error.friendly.default'), mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage), technicalMessage)
      return []
    }
  }

  async function searchHotels(payload: {
    location?: string
    checkInDate?: string
    checkOutDate?: string
  }): Promise<HotelResponse[]> {
    try {
      const hotelListResponse = await travelMvpApiClient.listHotels(payload)
      return hotelListResponse.hotels
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      showNotice('error', translate('error.friendly.default'), mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage), technicalMessage)
      return []
    }
  }

  async function searchTrains(payload: {
    fromStation?: string
    toStation?: string
    date?: string
  }): Promise<TrainResponse[]> {
    try {
      const trainListResponse = await travelMvpApiClient.listTrains(payload)
      return trainListResponse.trains
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      showNotice('error', translate('error.friendly.default'), mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage), technicalMessage)
      return []
    }
  }

  async function reloadManagedTrains() {
    if (!currentTrainAdminSession) {
      return
    }

    const trainListResponse = await travelMvpApiClient.listManagedTrains(currentTrainAdminSession.managerId)
    setCurrentTrainAdminSession(currentSession =>
      currentSession
        ? {
            ...currentSession,
            managedTrains: trainListResponse.trains,
          }
        : currentSession,
    )
  }

  return (
    <main className="layout-shell">
      <AppSidebar
        currentLanguage={currentLanguage}
        currentViewKey={currentViewKey}
        health={backendHealthResponse}
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
        {currentViewKey === 'explore' ? <ExplorePage translate={translate} /> : null}

        {currentViewKey === 'account' ? (
          <UserPanel
            account={signedInUserResponse}
            accountEntryMode={accountEntryMode}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            loginEmailDraft={loginEmailDraft}
            translate={translate}
            onChangeAccountEntryMode={setAccountEntryMode}
            onChangeLoginEmailDraft={setLoginEmailDraft}
            onRegisterAccount={async payload => {
              await runPageAction(async () => {
                const createdAccount = await travelMvpApiClient.createUser(payload)
                setLoginEmailDraft(createdAccount.email)
                setAccountEntryMode('login')
              }, translate('account.create'), translate('notice.registerSuccess'))
            }}
            onLoginAccount={async payload => {
              await runPageAction(async () => {
                const signedInAccount = await travelMvpApiClient.loginUser(payload)
                setSignedInUserResponse(signedInAccount)
                const [travelerListResponse, orderListResponse] = await Promise.all([
                  travelMvpApiClient.listTravelers(signedInAccount.userId),
                  travelMvpApiClient.listOrders(signedInAccount.userId),
                ])
                setTravelerResponses(travelerListResponse.travelers)
                setOrderResponses(orderListResponse.orders)
                setCurrentViewKey('travelers')
              }, translate('account.login'), translate('notice.loginSuccess'))
            }}
            onUploadAvatar={async avatarFile => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                const updatedAccount = await travelMvpApiClient.uploadUserAvatar(signedInUser.userId, avatarFile)
                setSignedInUserResponse(updatedAccount)
              }, translate('account.avatarUpload'), translate('notice.avatarUploaded'))
            }}
            onAvatarValidationError={message => {
              showNotice('error', translate('error.friendly.default'), message)
            }}
            onRefreshAccount={async () => {
              await runPageAction(async () => {
                await reloadCurrentUser()
              }, translate('account.refresh'), translate('notice.actionSuccess'))
            }}
            onLogout={() => {
              setSignedInUserResponse(null)
              setTravelerResponses([])
              setOrderResponses([])
              setCurrentViewKey('explore')
              showNotice('info', translate('guest.badge'), translate('notice.logoutSuccess'))
            }}
          />
        ) : null}

        {currentViewKey === 'travelers' ? (
          <TravelerPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            translate={translate}
            onCreateTraveler={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                await travelMvpApiClient.createTraveler(signedInUser.userId, payload)
                await Promise.all([reloadCurrentUser(), reloadTravelerList()])
              }, translate('travelers.add'), translate('notice.travelerSaved'))
            }}
            onUpdateTraveler={async payload => {
              const signedInUser = requireSignedInUser()
              if (!payload.travelerId) {
                throw new Error('Missing traveler id')
              }
              await runPageAction(async () => {
                await travelMvpApiClient.updateTraveler(signedInUser.userId, payload.travelerId!, {
                  fullName: payload.fullName,
                  documentType: payload.documentType,
                  documentNumber: payload.documentNumber,
                  phone: payload.phone,
                  birthDate: payload.birthDate,
                  seatPreference: payload.seatPreference,
                  mealPreference: payload.mealPreference,
                  accessibilityRequestNotes: payload.accessibilityRequestNotes.trim() || null,
                  emergencyContactName: payload.emergencyContactName.trim() || null,
                  emergencyContactPhoneNumber: payload.emergencyContactPhoneNumber.trim() || null,
                  isDefaultTraveler: payload.isDefaultTraveler,
                })
                await Promise.all([reloadCurrentUser(), reloadTravelerList()])
              }, translate('travelers.saveEdit'), translate('notice.travelerSaved'))
            }}
            onDeleteTraveler={async travelerId => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                await travelMvpApiClient.deleteTraveler(signedInUser.userId, travelerId)
                await Promise.all([reloadCurrentUser(), reloadTravelerList()])
              }, translate('travelers.delete'), translate('notice.travelerDeleted'))
            }}
            onReloadTravelers={async () => {
              await runPageAction(async () => {
                await reloadTravelerList()
              }, translate('travelers.refresh'), translate('notice.actionSuccess'))
            }}
          />
        ) : null}

        {currentViewKey === 'flights' ? (
          <FlightsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            translate={translate}
            onSearchFlights={searchFlights}
            onBookFlight={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                await travelMvpApiClient.createFlightOrder({
                  buyerUserId: signedInUser.userId,
                  flightId: payload.flightId,
                  travelerIds: payload.travelerIds,
                  cabinClass: payload.cabinClass,
                })
                await reloadOrders()
                setCurrentViewKey('bookings')
              }, translate('flights.bookNow'), translate('notice.bookingCreated'))
            }}
          />
        ) : null}

        {currentViewKey === 'hotels' ? (
          <HotelsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            translate={translate}
            onSearchHotels={searchHotels}
            onBookHotel={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                await travelMvpApiClient.createHotelOrder({
                  buyerUserId: signedInUser.userId,
                  roomTypeId: payload.roomTypeId,
                  guestTravelerIds: payload.guestTravelerIds,
                  checkInDate: payload.checkInDate,
                  checkOutDate: payload.checkOutDate,
                  roomCount: payload.roomCount,
                })
                await reloadOrders()
                setCurrentViewKey('bookings')
              }, translate('hotels.bookNow'), translate('notice.bookingCreated'))
            }}
          />
        ) : null}

        {currentViewKey === 'trains' ? (
          <TrainsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            translate={translate}
            onSearchTrains={searchTrains}
            onBookTrain={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                const createdOrder = await travelMvpApiClient.createOrder({
                  ownerUserId: signedInUser.userId,
                  orderCurrency: payload.orderCurrency,
                })
                await travelMvpApiClient.addTrainItemToOrder(createdOrder.orderId, {
                  buyerUserId: signedInUser.userId,
                  orderId: createdOrder.orderId,
                  trainId: payload.trainId,
                  travelerIds: payload.travelerIds,
                  fromStationCode: payload.fromStationCode,
                  toStationCode: payload.toStationCode,
                  seatClass: payload.seatClass,
                })
                await reloadOrders()
                setCurrentViewKey('bookings')
              }, translate('trains.bookNow'), translate('notice.bookingCreated'))
            }}
          />
        ) : null}

        {currentViewKey === 'bookings' ? (
          <OrderPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            orders={orderResponses}
            translate={translate}
            onReloadOrders={async () => {
              await runPageAction(async () => {
                await reloadOrders()
              }, translate('bookings.refresh'), translate('notice.actionSuccess'))
            }}
            onOpenPayment={order => {
              setPendingPaymentOrder(order)
            }}
            onCancelOrder={async orderId => {
              await runPageAction(async () => {
                await travelMvpApiClient.cancelOrder(orderId)
                await reloadOrders()
              }, translate('bookings.cancel'), translate('notice.actionSuccess'))
            }}
            onRequestRefund={async (orderId, refundReason) => {
              await runPageAction(async () => {
                await travelMvpApiClient.requestRefund(orderId, { refundReason })
                await reloadOrders()
              }, translate('bookings.requestRefund'), translate('notice.actionSuccess'))
            }}
          />
        ) : null}

        {currentViewKey === 'manager' ? (
          <ManagerPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            managerSession={currentManagerSession}
            managerTasks={managerTaskResponses}
            managerRefundTasks={managerRefundTaskResponses}
            translate={translate}
            onRegisterAirlineManager={async payload => {
              await runPageAction(async () => {
                const session = await travelMvpApiClient.registerAirlineManager(payload)
                setCurrentManagerSession(session)
                await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
              }, translate('manager.createAccount'), translate('notice.actionSuccess'))
            }}
            onRegisterHotelManager={async payload => {
              await runPageAction(async () => {
                const session = await travelMvpApiClient.registerHotelManager(payload)
                setCurrentManagerSession(session)
                await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks()])
              }, translate('manager.createAccount'), translate('notice.actionSuccess'))
            }}
            onCreateManagerRoomType={async payload => {
              await runPageAction(async () => {
                await travelMvpApiClient.createManagerRoomType(payload)
              }, translate('manager.createRoomType'), translate('notice.actionSuccess'))
            }}
            onLoginManager={async payload => {
              await runPageAction(async () => {
                const session = await travelMvpApiClient.loginManager(payload)
                setCurrentManagerSession(session)
                const [tasks, refundTasks] = await Promise.all([
                  travelMvpApiClient.listManagerTasks({
                    managerId: session.managerId,
                    managerType: session.managerType.toLowerCase(),
                    status: 'pending',
                  }),
                  travelMvpApiClient.listManagerRefundTasks({
                    managerId: session.managerId,
                    managerType: session.managerType.toLowerCase(),
                  }),
                ])
                setManagerTaskResponses(tasks.tasks)
                setManagerRefundTaskResponses(refundTasks.tasks)
              }, translate('manager.login'), translate('notice.actionSuccess'))
            }}
            onReloadTasks={async status => {
              await runPageAction(async () => {
                await reloadManagerTasks(status)
              }, translate('manager.refresh'), translate('notice.actionSuccess'))
            }}
            onReloadRefundTasks={async () => {
              await runPageAction(async () => {
                await reloadManagerRefundTasks()
              }, translate('manager.refundTasks'), translate('notice.actionSuccess'))
            }}
            onCreateManagerFlight={async payload => {
              const managerSession = requireManagerSession()
              await runPageAction(async () => {
                await travelMvpApiClient.createManagerFlight({
                  managerId: managerSession.managerId,
                  ...payload,
                })
              }, translate('manager.createFlight'), translate('notice.actionSuccess'))
            }}
            onConfirmTask={async payload => {
              const managerSession = requireManagerSession()
              await runPageAction(async () => {
                await travelMvpApiClient.confirmManagerBookingItem(payload.orderItemId, {
                  managerId: managerSession.managerId,
                  managerType: managerSession.managerType.toLowerCase(),
                  note: payload.note.trim() || null,
                })
                await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks(), reloadOrders()])
              }, translate('manager.confirm'), translate('notice.actionSuccess'))
            }}
            onRejectTask={async payload => {
              const managerSession = requireManagerSession()
              await runPageAction(async () => {
                await travelMvpApiClient.rejectManagerBookingItem(payload.orderItemId, {
                  managerId: managerSession.managerId,
                  managerType: managerSession.managerType.toLowerCase(),
                  reason: payload.reason,
                })
                await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks(), reloadOrders()])
              }, translate('manager.reject'), translate('notice.actionSuccess'))
            }}
            onApproveRefundTask={async payload => {
              const managerSession = requireManagerSession()
              await runPageAction(async () => {
                await travelMvpApiClient.approveRefund(payload.orderId, managerSession.managerId, managerSession.managerType.toLowerCase())
                await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks(), reloadOrders()])
              }, translate('manager.approveRefund'), translate('notice.actionSuccess'))
            }}
            onRejectRefundTask={async payload => {
              const managerSession = requireManagerSession()
              await runPageAction(async () => {
                await travelMvpApiClient.rejectRefund(payload.orderId, managerSession.managerId, managerSession.managerType.toLowerCase())
                await Promise.all([reloadManagerTasks(), reloadManagerRefundTasks(), reloadOrders()])
              }, translate('manager.rejectRefund'), translate('notice.actionSuccess'))
            }}
            onLogoutManager={() => {
              setCurrentManagerSession(null)
              setManagerTaskResponses([])
              setManagerRefundTaskResponses([])
            }}
          />
        ) : null}

        {currentViewKey === 'trainAdmin' ? (
          <TrainAdminPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            trainAdminSession={currentTrainAdminSession}
            translate={translate}
            onRegisterRailwayManager={async payload => {
              await runPageAction(async () => {
                const session = await travelMvpApiClient.registerRailwayManager(payload)
                setCurrentTrainAdminSession(session)
              }, translate('trainAdmin.createAccount'), translate('notice.actionSuccess'))
            }}
            onLoginRailwayManager={async payload => {
              await runPageAction(async () => {
                const session = await travelMvpApiClient.loginRailwayManager(payload)
                setCurrentTrainAdminSession(session)
              }, translate('trainAdmin.login'), translate('notice.actionSuccess'))
            }}
            onReloadManagedTrains={async () => {
              await runPageAction(async () => {
                await reloadManagedTrains()
              }, translate('trainAdmin.refresh'), translate('notice.actionSuccess'))
            }}
            onCreateTrainJourney={async payload => {
              const currentSession = currentTrainAdminSession
              if (!currentSession) {
                throw new Error(translate('error.managerNotFound'))
              }
              await runPageAction(async () => {
                await travelMvpApiClient.createTrainJourney({
                  managerId: currentSession.managerId,
                  trainNumber: payload.trainNumber,
                  saleStartsAt: payload.saleStartsAt,
                  stops: payload.stops,
                  seatInventories: payload.seatInventories,
                  segmentPrices: payload.segmentPrices,
                  refundPolicies: payload.refundPolicies,
                })
                await reloadManagedTrains()
              }, translate('trainAdmin.createTrain'), translate('notice.actionSuccess'))
            }}
            onLogoutRailwayManager={() => {
              setCurrentTrainAdminSession(null)
            }}
          />
        ) : null}
      </section>

      <ToastNotice notice={currentNotice} onDismiss={() => setCurrentNotice(null)} />
      <PaymentModal
        isOpen={pendingPaymentOrder !== null}
        order={pendingPaymentOrder}
        isBusy={isPageBusy}
        translate={translate}
        onClose={() => setPendingPaymentOrder(null)}
        onConfirmPayment={async payload => {
          await runPageAction(async () => {
            await travelMvpApiClient.payOrder(payload.orderId, {
              paymentMethod: payload.paymentMethod,
              paymentSucceeded: payload.paymentSucceeded,
            })
            await reloadOrders()
            if (payload.paymentSucceeded) {
              setPendingPaymentOrder(null)
            }
          }, translate('bookings.pay'), payload.paymentSucceeded ? translate('notice.paymentSuccess') : translate('notice.paymentPending'))
        }}
      />
    </main>
  )
}

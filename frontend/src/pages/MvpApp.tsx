import { useEffect, useState } from 'react'

import { AppSidebar } from '../components/AppSidebar'
import { FlightsPanel } from '../components/FlightsPanel'
import { HotelsPanel } from '../components/HotelsPanel'
import { OrderPanel } from '../components/OrderPanel'
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
  OrderResponse,
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
  const [currentOrderResponse, setCurrentOrderResponse] = useState<OrderResponse | null>(null)
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

    await runPageAction(async () => {
      const userResponse = await travelMvpApiClient.getUser(signedInUserResponse.userId)
      setSignedInUserResponse(userResponse)
    }, translate('account.refresh'), translate('notice.actionSuccess'))
  }

  async function reloadTravelerList() {
    if (!signedInUserResponse) {
      return
    }

    await runPageAction(async () => {
      const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUserResponse.userId)
      setTravelerResponses(travelerListResponse.travelers)
    }, translate('travelers.refresh'), translate('notice.actionSuccess'))
  }

  async function reloadCurrentOrder() {
    if (!currentOrderResponse) {
      return
    }

    await runPageAction(async () => {
      const orderResponse = await travelMvpApiClient.getOrder(currentOrderResponse.orderId)
      setCurrentOrderResponse(orderResponse)
    }, translate('bookings.refresh'), translate('notice.actionSuccess'))
  }

  function requireSignedInUser() {
    if (!signedInUserResponse) {
      throw new Error(translate('error.loginRequired'))
    }
    return signedInUserResponse
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
      showNotice(
        'error',
        translate('error.friendly.default'),
        mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage),
        technicalMessage,
      )
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
      showNotice(
        'error',
        translate('error.friendly.default'),
        mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage),
        technicalMessage,
      )
      return []
    }
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
                const travelerListResponse = await travelMvpApiClient.listTravelers(signedInAccount.userId)
                setTravelerResponses(travelerListResponse.travelers)
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
            onRefreshAccount={reloadCurrentUser}
            onLogout={() => {
              setSignedInUserResponse(null)
              setTravelerResponses([])
              setCurrentOrderResponse(null)
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
                const refreshedUserResponse = await travelMvpApiClient.getUser(signedInUser.userId)
                const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUser.userId)
                setSignedInUserResponse(refreshedUserResponse)
                setTravelerResponses(travelerListResponse.travelers)
              }, translate('travelers.add'), translate('notice.travelerSaved'))
            }}
            onUpdateTraveler={async payload => {
              const signedInUser = requireSignedInUser()
              if (!payload.travelerId) {
                throw new Error('Missing traveler id')
              }
              const travelerId = payload.travelerId
              await runPageAction(async () => {
                await travelMvpApiClient.updateTraveler(signedInUser.userId, travelerId, {
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
                const refreshedUserResponse = await travelMvpApiClient.getUser(signedInUser.userId)
                const travelerListResponse = await travelMvpApiClient.listTravelers(signedInUser.userId)
                setSignedInUserResponse(refreshedUserResponse)
                setTravelerResponses(travelerListResponse.travelers)
              }, translate('travelers.saveEdit'), translate('notice.travelerSaved'))
            }}
            onReloadTravelers={reloadTravelerList}
          />
        ) : null}

        {currentViewKey === 'flights' ? (
          <FlightsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            currentBooking={currentOrderResponse}
            translate={translate}
            onSearchFlights={searchFlights}
            onCreateBookingShell={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.createOrder({
                  ownerUserId: signedInUser.userId,
                  orderCurrency: payload.orderCurrency,
                })
                setCurrentOrderResponse(bookingResponse)
              }, translate('flights.createBooking'), translate('notice.bookingCreated'))
            }}
            onAddFlightToBooking={async payload => {
              const signedInUser = requireSignedInUser()
              if (!currentOrderResponse) {
                throw new Error(translate('flights.requireBooking'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.addFlightItemToOrder(currentOrderResponse.orderId, {
                  buyerUserId: signedInUser.userId,
                  flightId: payload.flightId,
                  travelerIds: payload.travelerIds,
                  cabinClass: payload.cabinClass,
                })
                setCurrentOrderResponse(bookingResponse)
                setCurrentViewKey('bookings')
              }, translate('flights.addToBooking'), translate('notice.flightAdded'))
            }}
          />
        ) : null}

        {currentViewKey === 'hotels' ? (
          <HotelsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            currentBooking={currentOrderResponse}
            translate={translate}
            onSearchHotels={searchHotels}
            onCreateBookingShell={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.createOrder({
                  ownerUserId: signedInUser.userId,
                  orderCurrency: payload.orderCurrency,
                })
                setCurrentOrderResponse(bookingResponse)
              }, translate('hotels.createBooking'), translate('notice.bookingCreated'))
            }}
            onAddHotelToBooking={async payload => {
              const signedInUser = requireSignedInUser()
              if (!currentOrderResponse) {
                throw new Error(translate('hotels.requireBooking'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.addHotelItemToOrder(currentOrderResponse.orderId, {
                  buyerUserId: signedInUser.userId,
                  roomTypeId: payload.roomTypeId,
                  guestTravelerIds: payload.guestTravelerIds,
                  checkInDate: payload.checkInDate,
                  checkOutDate: payload.checkOutDate,
                  roomCount: payload.roomCount,
                })
                setCurrentOrderResponse(bookingResponse)
                setCurrentViewKey('bookings')
              }, translate('hotels.addToBooking'), translate('notice.hotelAdded'))
            }}
          />
        ) : null}

        {currentViewKey === 'bookings' ? (
          <OrderPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            booking={currentOrderResponse}
            translate={translate}
            onCreateBooking={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.createOrder({
                  ownerUserId: signedInUser.userId,
                  orderCurrency: payload.orderCurrency,
                })
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.create'), translate('notice.bookingCreated'))
            }}
            onReloadBooking={reloadCurrentOrder}
            onSubmitBooking={async () => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.submitOrder(currentOrderResponse.orderId)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.continuePayment'), translate('notice.actionSuccess'))
            }}
            onAuthorizePayment={async payload => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.authorizePayment(currentOrderResponse.orderId, payload)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.pay'), translate('notice.actionSuccess'))
            }}
            onCapturePayment={async paymentId => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.capturePayment(currentOrderResponse.orderId, paymentId)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.confirmPayment'), translate('notice.actionSuccess'))
            }}
            onCancelBooking={async () => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.cancelOrder(currentOrderResponse.orderId)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.cancel'), translate('notice.actionSuccess'))
            }}
            onRequestRefund={async payload => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.requestRefund(currentOrderResponse.orderId, payload)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.requestRefund'), translate('notice.actionSuccess'))
            }}
            onApproveRefund={async refundId => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.approveRefund(currentOrderResponse.orderId, refundId)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.approveRefund'), translate('notice.actionSuccess'))
            }}
            onSettleRefund={async refundId => {
              if (!currentOrderResponse) {
                throw new Error(translate('bookings.empty'))
              }
              await runPageAction(async () => {
                const bookingResponse = await travelMvpApiClient.settleRefund(currentOrderResponse.orderId, refundId)
                setCurrentOrderResponse(bookingResponse)
              }, translate('bookings.settleRefund'), translate('notice.actionSuccess'))
            }}
          />
        ) : null}
      </section>

      <ToastNotice notice={currentNotice} onDismiss={() => setCurrentNotice(null)} />
    </main>
  )
}

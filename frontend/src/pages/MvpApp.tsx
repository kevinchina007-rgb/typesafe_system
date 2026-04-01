import { useEffect, useState } from 'react'

import { AppSidebar } from '../components/AppSidebar'
import { AttractionAdminPanel } from '../components/AttractionAdminPanel'
import { AttractionsPanel } from '../components/AttractionsPanel'
import { BlogPanel } from '../components/BlogPanel'
import { FlightsPanel } from '../components/FlightsPanel'
import { HotelsPanel } from '../components/HotelsPanel'
import { ManagerPanel } from '../components/ManagerPanel'
import { MyReviewsPanel } from '../components/MyReviewsPanel'
import { OrderPanel } from '../components/OrderPanel'
import { PaymentModal } from '../components/PaymentModal'
import { TrainAdminPanel } from '../components/TrainAdminPanel'
import { TrainsPanel } from '../components/TrainsPanel'
import { ToastNotice } from '../components/ToastNotice'
import { TourGroupsPanel } from '../components/TourGroupsPanel'
import { TravelerPanel } from '../components/TravelerPanel'
import { UserPanel } from '../components/UserPanel'
import { travelMvpApiClient } from '../lib/api-client'
import { createTranslator } from '../lib/i18n'
import type {
  AppLanguage,
  AppNotice,
  AppViewKey,
  AttractionAdminSessionResponse,
  AttractionResponse,
  BlogPostResponse,
  BlogPostSummaryResponse,
  FlightResponse,
  HealthResponse,
  HotelResponse,
  ManagerRefundTaskResponse,
  ManagerSessionResponse,
  ManagerTaskResponse,
  OrderResponse,
  ResourceReviewSummaryResponse,
  ReviewEligibilityResponse,
  ReviewResponse,
  TourGroupSummaryResponse,
  TrainAdminSessionResponse,
  TrainResponse,
  TravelerResponse,
  UserResponse,
} from '../lib/mvp-types'
import { mapTechnicalErrorToFriendlyMessage } from '../lib/view-models'
import { ExplorePage } from './ExplorePage'

export function MvpApp() {
  const [currentLanguage, setCurrentLanguage] = useState<AppLanguage>('en')
  const [currentViewKey, setCurrentViewKey] = useState<AppViewKey>('blog')
  const [accountEntryMode, setAccountEntryMode] = useState<'register' | 'login'>('login')
  const [backendHealthResponse, setBackendHealthResponse] = useState<HealthResponse | null>(null)
  const [signedInUserResponse, setSignedInUserResponse] = useState<UserResponse | null>(null)
  const [travelerResponses, setTravelerResponses] = useState<TravelerResponse[]>([])
  const [orderResponses, setOrderResponses] = useState<OrderResponse[]>([])
  const [reviewResponses, setReviewResponses] = useState<ReviewResponse[]>([])
  const [currentManagerSession, setCurrentManagerSession] = useState<ManagerSessionResponse | null>(null)
  const [managedFlightResponses, setManagedFlightResponses] = useState<FlightResponse[]>([])
  const [currentTrainAdminSession, setCurrentTrainAdminSession] = useState<TrainAdminSessionResponse | null>(null)
  const [currentAttractionAdminSession, setCurrentAttractionAdminSession] = useState<AttractionAdminSessionResponse | null>(null)
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
    if (currentViewKey === 'bookings' && signedInUserResponse) {
      void reloadOrders()
    }
  }, [currentViewKey, signedInUserResponse])

  useEffect(() => {
    if ((currentViewKey === 'reviews' || currentViewKey === 'bookings') && signedInUserResponse) {
      void reloadReviews()
    }
  }, [currentViewKey, signedInUserResponse])

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

  async function reloadReviews() {
    if (!signedInUserResponse) {
      return
    }

    const reviewListResponse = await travelMvpApiClient.listMyReviews(signedInUserResponse.userId)
    setReviewResponses(reviewListResponse.reviews)
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

  async function reloadManagedFlights() {
    if (!currentManagerSession || currentManagerSession.managerType !== 'Airline') {
      setManagedFlightResponses([])
      return
    }

    const flightListResponse = await travelMvpApiClient.listManagerFlights(currentManagerSession.managerId)
    setManagedFlightResponses(flightListResponse.flights)
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

  async function searchAttractions(payload: {
    city?: string
  }): Promise<AttractionResponse[]> {
    try {
      const attractionListResponse = await travelMvpApiClient.listAttractions(payload)
      const detailedAttractions = await Promise.all(
        attractionListResponse.attractions.map(async attractionSummary => {
          try {
            return await travelMvpApiClient.getAttraction(attractionSummary.attractionId)
          } catch {
            return attractionSummary
          }
        }),
      )
      return detailedAttractions
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      showNotice('error', translate('error.friendly.default'), mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage), technicalMessage)
      return []
    }
  }

  async function listBlogPosts(scope: 'latest' | 'mine', query?: string): Promise<BlogPostSummaryResponse[]> {
    const response = await travelMvpApiClient.listBlogPosts(scope, signedInUserResponse?.userId, query)
    return response.posts
  }

  async function loadBlogPost(postId: string): Promise<BlogPostResponse> {
    return travelMvpApiClient.getBlogPost(postId, signedInUserResponse?.userId)
  }

  async function listMyReviews(): Promise<ReviewResponse[]> {
    const signedInUser = requireSignedInUser()
    const response = await travelMvpApiClient.listMyReviews(signedInUser.userId)
    return response.reviews
  }

  async function loadReviewSummary(payload: {
    resourceType: string
    resourceId: string
  }): Promise<ResourceReviewSummaryResponse> {
    const signedInUser = requireSignedInUser()
    return travelMvpApiClient.getReviewResourceSummary({
      userId: signedInUser.userId,
      resourceType: payload.resourceType,
      resourceId: payload.resourceId,
    })
  }

  async function loadReviewsByResource(payload: {
    resourceType: string
    resourceId: string
  }): Promise<ReviewResponse[]> {
    const signedInUser = requireSignedInUser()
    const response = await travelMvpApiClient.listReviewsByResource({
      userId: signedInUser.userId,
      resourceType: payload.resourceType,
      resourceId: payload.resourceId,
    })
    return response.reviews
  }

  async function loadReviewEligibility(orderItemId: string): Promise<ReviewEligibilityResponse> {
    const signedInUser = requireSignedInUser()
    return travelMvpApiClient.getReviewEligibility({
      userId: signedInUser.userId,
      orderItemId,
    })
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

  async function reloadManagedAttractions() {
    if (!currentAttractionAdminSession) {
      return
    }

    const attractionListResponse = await travelMvpApiClient.listManagedAttractions(currentAttractionAdminSession.managerId)
    setCurrentAttractionAdminSession(currentSession =>
      currentSession
        ? {
            ...currentSession,
            managedAttractions: attractionListResponse.attractions,
          }
        : currentSession,
    )
  }

  async function runPageActionWithResult<TValue>(
    action: () => Promise<TValue>,
    successTitle: string,
    successDescription: string,
  ): Promise<TValue> {
    setIsPageBusy(true)
    try {
      const result = await action()
      showNotice('success', successTitle, successDescription)
      return result
    } catch (error) {
      const technicalMessage = error instanceof Error ? error.message : 'Unknown error'
      showNotice(
        'error',
        translate('error.friendly.default'),
        mapTechnicalErrorToFriendlyMessage(technicalMessage, currentLanguage),
        technicalMessage,
      )
      throw error
    } finally {
      setIsPageBusy(false)
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
        {currentViewKey === 'blog' ? (
          <BlogPanel
              currentLanguage={currentLanguage}
              isBusy={isPageBusy}
              signedInUser={signedInUserResponse}
              translate={translate}
              onListPosts={(scope, query) => listBlogPosts(scope, query)}
              onLoadPost={postId => loadBlogPost(postId)}
              onUploadImage={async imageFile => {
                const signedInUser = requireSignedInUser()
                return runPageActionWithResult(
                  () => travelMvpApiClient.uploadBlogImage(signedInUser.userId, imageFile),
                  translate('content.imagesUpload'),
                  translate('notice.actionSuccess'),
                )
              }}
              onCreatePost={async payload => {
                const signedInUser = requireSignedInUser()
                return runPageActionWithResult(
                  () =>
                    travelMvpApiClient.createBlogPost({
                      userId: signedInUser.userId,
                      title: payload.title,
                      summary: payload.summary,
                      content: payload.content,
                      images: payload.images,
                    }),
                  translate('blog.publish'),
                  translate('notice.actionSuccess'),
                )
              }}
            onUpdatePost={async (postId, payload) => {
              const signedInUser = requireSignedInUser()
              return runPageActionWithResult(
                () =>
                    travelMvpApiClient.updateBlogPost(postId, {
                      userId: signedInUser.userId,
                      title: payload.title,
                      summary: payload.summary,
                      content: payload.content,
                      images: payload.images,
                    }),
                translate('blog.save'),
                translate('notice.actionSuccess'),
              )
            }}
            onArchivePost={async postId => {
              const signedInUser = requireSignedInUser()
              return runPageActionWithResult(
                () =>
                  travelMvpApiClient.archiveBlogPost(postId, {
                    userId: signedInUser.userId,
                  }),
                translate('blog.archive'),
                translate('notice.actionSuccess'),
              )
            }}
            onCommentPost={async (postId, content) => {
              const signedInUser = requireSignedInUser()
              return runPageActionWithResult(
                () =>
                  travelMvpApiClient.addBlogComment(postId, {
                    userId: signedInUser.userId,
                    content,
                  }),
                translate('blog.submitComment'),
                translate('notice.actionSuccess'),
              )
            }}
            onDeleteComment={async commentId => {
              const signedInUser = requireSignedInUser()
              return runPageActionWithResult(
                () =>
                  travelMvpApiClient.deleteBlogComment(commentId, {
                    userId: signedInUser.userId,
                  }),
                translate('blog.deleteComment'),
                translate('notice.actionSuccess'),
              )
            }}
            onLikePost={async postId => {
              const signedInUser = requireSignedInUser()
              return runPageActionWithResult(
                () =>
                  travelMvpApiClient.likeBlogPost(postId, {
                    userId: signedInUser.userId,
                  }),
                translate('blog.like'),
                translate('notice.actionSuccess'),
              )
            }}
            onUnlikePost={async postId => {
              const signedInUser = requireSignedInUser()
              return runPageActionWithResult(
                () =>
                  travelMvpApiClient.unlikeBlogPost(postId, {
                    userId: signedInUser.userId,
                  }),
                translate('blog.unlike'),
                translate('notice.actionSuccess'),
              )
            }}
          />
        ) : null}

        {currentViewKey === 'reviews' ? (
          <MyReviewsPanel
              currentLanguage={currentLanguage}
              isBusy={isPageBusy}
              signedInUser={signedInUserResponse}
              translate={translate}
              onListMyReviews={listMyReviews}
              onUploadImage={async imageFile => {
                const signedInUser = requireSignedInUser()
                return runPageActionWithResult(
                  () => travelMvpApiClient.uploadReviewImage(signedInUser.userId, imageFile),
                  translate('content.imagesUpload'),
                  translate('notice.actionSuccess'),
                )
              }}
              onUpdateReview={async (reviewId, payload) => {
                const signedInUser = requireSignedInUser()
                return runPageActionWithResult(
                  async () => {
                    const updatedReview = await travelMvpApiClient.updateReview(reviewId, {
                      userId: signedInUser.userId,
                      rating: payload.rating,
                      title: payload.title,
                      content: payload.content,
                      images: payload.images,
                    })
                    await reloadReviews()
                    return updatedReview
                  },
                translate('reviews.save'),
                translate('notice.actionSuccess'),
              )
            }}
            onDeleteReview={async reviewId => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                await travelMvpApiClient.deleteReview(reviewId, { userId: signedInUser.userId })
                await reloadReviews()
              }, translate('reviews.delete'), translate('notice.actionSuccess'))
            }}
          />
        ) : null}

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
                const reviewListResponse = await travelMvpApiClient.listMyReviews(signedInAccount.userId)
                setReviewResponses(reviewListResponse.reviews)
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
              setReviewResponses([])
              setCurrentViewKey('blog')
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
            onLoadReviewSummary={loadReviewSummary}
            onLoadReviews={loadReviewsByResource}
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
            onLoadReviewSummary={loadReviewSummary}
            onLoadReviews={loadReviewsByResource}
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
            onLoadReviewSummary={loadReviewSummary}
            onLoadReviews={loadReviewsByResource}
          />
        ) : null}

        {currentViewKey === 'attractions' ? (
          <AttractionsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            travelers={travelerResponses}
            translate={translate}
            onSearchAttractions={searchAttractions}
            onBookAttraction={async payload => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                const createdOrder = await travelMvpApiClient.createOrder({
                  ownerUserId: signedInUser.userId,
                  orderCurrency: payload.orderCurrency,
                })
                await travelMvpApiClient.addAttractionItemToOrder(createdOrder.orderId, {
                  buyerUserId: signedInUser.userId,
                  orderId: createdOrder.orderId,
                  attractionId: payload.attractionId,
                  ticketTypeId: payload.ticketTypeId,
                  travelerIds: payload.travelerIds,
                  useDate: payload.useDate,
                })
                await reloadOrders()
                setCurrentViewKey('bookings')
              }, translate('attractions.bookNow'), translate('notice.bookingCreated'))
            }}
            onLoadReviewSummary={loadReviewSummary}
            onLoadReviews={loadReviewsByResource}
          />
        ) : null}

        {currentViewKey === 'tourGroups' ? (
          <TourGroupsPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            signedInUser={signedInUserResponse}
            travelers={travelerResponses}
            translate={translate}
            onListGroups={async (): Promise<TourGroupSummaryResponse[]> => {
              const response = await travelMvpApiClient.listTourGroups()
              return response.groups
            }}
            onLoadGroupDetails={groupId => travelMvpApiClient.getTourGroup(groupId)}
            onCreateGroup={payload =>
              runPageActionWithResult(
                () => travelMvpApiClient.createTourGroup(payload),
                translate('tourGroups.createGroup'),
                translate('notice.actionSuccess'),
              )
            }
            onJoinGroup={(groupId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.joinTourGroup(groupId, payload),
                translate('tourGroups.joinGroup'),
                translate('notice.actionSuccess'),
              )
            }
            onAddMembershipTraveler={(groupId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.addTourGroupMembershipTraveler(groupId, payload),
                translate('tourGroups.addMembershipTraveler'),
                translate('notice.actionSuccess'),
              )
            }
            onCreatePlanItem={(groupId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.createTourGroupPlanItem(groupId, payload),
                translate('tourGroups.createPlanItem'),
                translate('notice.actionSuccess'),
              )
            }
            onCreatePlanOption={(planItemId, groupId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.createTourGroupPlanOption(planItemId, groupId, payload),
                translate('tourGroups.createOption'),
                translate('notice.actionSuccess'),
              )
            }
            onCreateSelection={(planItemId, groupId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.createTourGroupSelection(planItemId, groupId, payload),
                translate('tourGroups.createSelection'),
                translate('notice.actionSuccess'),
              )
            }
            onSubmitSelection={(selectionId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.submitTourGroupSelection(selectionId, payload),
                translate('tourGroups.submitSelection'),
                translate('notice.actionSuccess'),
              )
            }
            onConfirmSelection={(selectionId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.confirmTourGroupSelection(selectionId, payload),
                translate('tourGroups.confirmSelection'),
                translate('notice.actionSuccess'),
              )
            }
            onRejectSelection={(selectionId, payload) =>
              runPageActionWithResult(
                () => travelMvpApiClient.rejectTourGroupSelection(selectionId, payload),
                translate('tourGroups.rejectSelection'),
                translate('notice.actionSuccess'),
              )
            }
            onSearchFlights={searchFlights}
            onSearchHotels={searchHotels}
            onSearchTrains={searchTrains}
            onSearchAttractions={searchAttractions}
            onOpenBookings={async () => {
              await runPageAction(async () => {
                await reloadOrders()
                setCurrentViewKey('bookings')
              }, translate('nav.bookings'), translate('notice.actionSuccess'))
            }}
          />
        ) : null}

        {currentViewKey === 'bookings' ? (
          <OrderPanel
            currentLanguage={currentLanguage}
            isBusy={isPageBusy}
            isGuestMode={isGuestMode}
            orders={orderResponses}
            reviews={reviewResponses}
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
              onLoadReviewEligibility={loadReviewEligibility}
              onUploadReviewImage={async imageFile => {
                const signedInUser = requireSignedInUser()
                return runPageActionWithResult(
                  () => travelMvpApiClient.uploadReviewImage(signedInUser.userId, imageFile),
                  translate('content.imagesUpload'),
                  translate('notice.actionSuccess'),
                )
              }}
              onCreateReview={async payload => {
                const signedInUser = requireSignedInUser()
                await runPageAction(async () => {
                  await travelMvpApiClient.createReview({
                    userId: signedInUser.userId,
                    orderId: payload.orderId,
                    orderItemId: payload.orderItemId,
                    rating: payload.rating,
                    title: payload.title,
                    content: payload.content,
                    images: payload.images,
                  })
                  await reloadReviews()
                }, translate('reviews.submit'), translate('notice.actionSuccess'))
              }}
            onUpdateReview={async (reviewId, payload) => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                  await travelMvpApiClient.updateReview(reviewId, {
                    userId: signedInUser.userId,
                    rating: payload.rating,
                    title: payload.title,
                    content: payload.content,
                    images: payload.images,
                  })
                  await reloadReviews()
                }, translate('reviews.save'), translate('notice.actionSuccess'))
              }}
            onDeleteReview={async reviewId => {
              const signedInUser = requireSignedInUser()
              await runPageAction(async () => {
                await travelMvpApiClient.deleteReview(reviewId, { userId: signedInUser.userId })
                await reloadReviews()
              }, translate('reviews.delete'), translate('notice.actionSuccess'))
            }}
          />
        ) : null}

        {currentViewKey === 'manager' ? (
          <>
            <ManagerPanel
              currentLanguage={currentLanguage}
              isBusy={isPageBusy}
              managerSession={currentManagerSession}
              managedFlights={managedFlightResponses}
              managerTasks={managerTaskResponses}
              managerRefundTasks={managerRefundTaskResponses}
              translate={translate}
              onRegisterAirlineManager={async payload => {
                await runPageAction(async () => {
                  const session = await travelMvpApiClient.registerAirlineManager(payload)
                  setCurrentManagerSession(session)
                  const [tasks, refundTasks, flights] = await Promise.all([
                    travelMvpApiClient.listManagerTasks({
                      managerId: session.managerId,
                      managerType: session.managerType.toLowerCase(),
                      status: 'pending',
                    }),
                    travelMvpApiClient.listManagerRefundTasks({
                      managerId: session.managerId,
                      managerType: session.managerType.toLowerCase(),
                    }),
                    travelMvpApiClient.listManagerFlights(session.managerId),
                  ])
                  setManagerTaskResponses(tasks.tasks)
                  setManagerRefundTaskResponses(refundTasks.tasks)
                  setManagedFlightResponses(flights.flights)
                }, translate('manager.createAccount'), translate('notice.actionSuccess'))
              }}
              onRegisterHotelManager={async payload => {
                await runPageAction(async () => {
                  const session = await travelMvpApiClient.registerHotelManager(payload)
                  setCurrentManagerSession(session)
                  setManagedFlightResponses([])
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
                  if (session.managerType === 'Airline') {
                    const flights = await travelMvpApiClient.listManagerFlights(session.managerId)
                    setManagedFlightResponses(flights.flights)
                  } else {
                    setManagedFlightResponses([])
                  }
                }, translate('manager.login'), translate('notice.actionSuccess'))
              }}
              onReloadTasks={async status => {
                await runPageAction(async () => {
                  await reloadManagerTasks(status)
                  await reloadManagedFlights()
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
                  await reloadManagedFlights()
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
                setManagedFlightResponses([])
                setManagerTaskResponses([])
                setManagerRefundTaskResponses([])
              }}
            />

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

            <AttractionAdminPanel
              currentLanguage={currentLanguage}
              isBusy={isPageBusy}
              attractionAdminSession={currentAttractionAdminSession}
              translate={translate}
              onRegisterAttractionManager={async payload => {
                await runPageAction(async () => {
                  const session = await travelMvpApiClient.registerAttractionManager(payload)
                  setCurrentAttractionAdminSession(session)
                }, translate('attractionAdmin.createAccount'), translate('notice.actionSuccess'))
              }}
              onLoginAttractionManager={async payload => {
                await runPageAction(async () => {
                  const session = await travelMvpApiClient.loginAttractionManager(payload)
                  setCurrentAttractionAdminSession(session)
                }, translate('attractionAdmin.login'), translate('notice.actionSuccess'))
              }}
              onReloadManagedAttractions={async () => {
                await runPageAction(async () => {
                  await reloadManagedAttractions()
                }, translate('attractionAdmin.refresh'), translate('notice.actionSuccess'))
              }}
              onCreateAttraction={async payload => {
                const currentSession = currentAttractionAdminSession
                if (!currentSession) {
                  throw new Error(translate('error.managerNotFound'))
                }
                await runPageAction(async () => {
                  await travelMvpApiClient.createAttraction({
                    managerId: currentSession.managerId,
                    ...payload,
                  })
                  await reloadManagedAttractions()
                }, translate('attractionAdmin.createAttraction'), translate('notice.actionSuccess'))
              }}
              onCreateTicketType={async payload => {
                const currentSession = currentAttractionAdminSession
                if (!currentSession) {
                  throw new Error(translate('error.managerNotFound'))
                }
                await runPageAction(async () => {
                  await travelMvpApiClient.createAttractionTicketType({
                    managerId: currentSession.managerId,
                    ...payload,
                  })
                  await reloadManagedAttractions()
                }, translate('attractionAdmin.createTicketType'), translate('notice.actionSuccess'))
              }}
              onCreateRule={async payload => {
                const currentSession = currentAttractionAdminSession
                if (!currentSession) {
                  throw new Error(translate('error.managerNotFound'))
                }
                await runPageAction(async () => {
                  await travelMvpApiClient.createAttractionTicketRule({
                    managerId: currentSession.managerId,
                    ...payload,
                  })
                  await reloadManagedAttractions()
                }, translate('attractionAdmin.createRule'), translate('notice.actionSuccess'))
              }}
              onLogoutAttractionManager={() => {
                setCurrentAttractionAdminSession(null)
              }}
            />
          </>
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

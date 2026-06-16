import { useEffect, useMemo, useState } from 'react'

import { handleOrderCancellationRequest, markFeedbackThreadRead, sendFeedbackMessage, useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import type { ManagerFlightPlannerResponse } from '@/lib/mvp-types/manager'
import type { ManagerFlightOrderResponse } from '@/lib/mvp-types/manager'
import type { AirlineWorkspaceSection, ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { FeedbackConversationWorkspace } from '@/pages/shared/feedback/FeedbackConversationWorkspace'

import { CreateFlightSection, FlightManagementSection, ManagerFlightOrdersSection, buildManagerFlightSearchPayload, type ManagerFlightSearchDraft, validateManagerFlightSearchDraft } from './manager-panel-workspace-flight'
import { HotelWorkspace, ManagerProfileSection, buildProfileDraft } from './manager-panel-workspace-profile'
export { HotelProfileSection } from './manager-panel-workspace-profile'

type ManagerPanelWorkspaceProps = Pick<
  ManagerPanelProps,
  | 'currentLanguage'
  | 'isBusy'
  | 'managerSession'
  | 'managedFlights'
  | 'managedHotels'
  | 'managerTasks'
  | 'managerRefundTasks'
  | 'initialAirlineSection'
  | 'translate'
  | 'onCreateManagerFlight'
  | 'onToggleManagerFlightStatus'
  | 'onSearchManagerFlights'
  | 'onLoadManagerFlightOrders'
  | 'onUpdateAirlineManagerProfile'
  | 'onUpdateHotelManagerProfile'
  | 'onCreateManagerRoomType'
  | 'onValidationError'
  | 'onLogoutManager'
>

export function ManagerPanelWorkspace({
  currentLanguage,
  isBusy,
  managerSession,
  managedFlights,
  managedHotels,
  managerTasks,
  managerRefundTasks,
  initialAirlineSection,
  translate,
  onCreateManagerFlight,
  onToggleManagerFlightStatus,
  onSearchManagerFlights,
  onLoadManagerFlightOrders,
  onUpdateAirlineManagerProfile,
  onCreateManagerRoomType,
  onValidationError,
  onLogoutManager,
}: ManagerPanelWorkspaceProps) {
  const [activeSection, setActiveSection] = useState<AirlineWorkspaceSection>(initialAirlineSection ?? 'flightManagement')
  const [searchDraft, setSearchDraft] = useState<ManagerFlightSearchDraft>({
    departureCity: '',
    arrivalCity: '',
    departureDate: '',
    timeRange: 'all',
  })
  const [submittedSearch, setSubmittedSearch] = useState<ManagerFlightSearchDraft | null>(null)
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc')
  const [selectedDepartureAirport, setSelectedDepartureAirport] = useState('all')
  const [selectedArrivalAirport, setSelectedArrivalAirport] = useState('all')
  const [selectedFlight, setSelectedFlight] = useState<ManagerFlightPlannerResponse | null>(null)
  const [selectedFlightOrders, setSelectedFlightOrders] = useState<ManagerFlightOrderResponse[]>([])
  const [isLoadingSelectedFlightOrders, setIsLoadingSelectedFlightOrders] = useState(false)
  const [profileDraft, setProfileDraft] = useState(() => buildProfileDraft(managerSession, managedFlights))
  const [savedProfile, setSavedProfile] = useState(profileDraft)
  const [profileSavedAt, setProfileSavedAt] = useState<string | null>(null)
  const loadManagerThreads = useFeedbackChatStore(state => state.loadManagerThreads)
  const managerThreads = useFeedbackChatStore(state => state.managerThreads)

  useEffect(() => {
    const nextProfile = buildProfileDraft(managerSession, managedFlights)
    setProfileDraft(nextProfile)
    setSavedProfile(nextProfile)
    setProfileSavedAt(null)
  }, [managerSession?.managerId, managedFlights])

  useEffect(() => {
    if (initialAirlineSection) {
      setActiveSection(initialAirlineSection)
    }
  }, [initialAirlineSection])

  useEffect(() => {
    if (managerSession?.managerType !== 'Airline') {
      return
    }
    void loadManagerThreads()
  }, [loadManagerThreads, managerSession?.managerId, managerSession?.managerType])

  const departureAirportOptions = useMemo(
    () => managedFlights.map(flight => flight.departureAirport).filter((value, index, array) => array.indexOf(value) === index),
    [managedFlights],
  )
  const arrivalAirportOptions = useMemo(
    () => managedFlights.map(flight => flight.arrivalAirport).filter((value, index, array) => array.indexOf(value) === index),
    [managedFlights],
  )

  const displayFlights = useMemo(() => {
    const filteredFlights = managedFlights.filter(flight => {
      const matchesDepartureAirport = selectedDepartureAirport === 'all' || flight.departureAirport === selectedDepartureAirport
      const matchesArrivalAirport = selectedArrivalAirport === 'all' || flight.arrivalAirport === selectedArrivalAirport

      return matchesDepartureAirport && matchesArrivalAirport
    })

    return [...filteredFlights].sort((left, right) => {
      const diff = new Date(left.departureTime).getTime() - new Date(right.departureTime).getTime()
      return sortDirection === 'asc' ? diff : -diff
    })
  }, [managedFlights, selectedArrivalAirport, selectedDepartureAirport, sortDirection])

  if (!managerSession) {
    return null
  }

  if (managerSession.managerType !== 'Airline') {
    return (
      <HotelWorkspace
        currentLanguage={currentLanguage}
        isBusy={isBusy}
        managerSession={managerSession}
        managedHotels={managedHotels}
        translate={translate}
        onCreateManagerRoomType={onCreateManagerRoomType}
      />
    )
  }

  if (initialAirlineSection === 'managerProfile') {
    return (
      <section className="grid gap-6">
        <ManagerProfileSection
          managerSession={managerSession}
          profileDraft={profileDraft}
          profileSavedAt={profileSavedAt}
          currentLanguage={currentLanguage}
          onProfileChange={setProfileDraft}
          onSaveProfile={async () => {
            await onUpdateAirlineManagerProfile({
              displayName: profileDraft.displayName,
              airlineName: profileDraft.companyName,
              airlineCode: profileDraft.airlineCode,
              logoAssetPath: profileDraft.logoPath.trim() || null,
            })
            setSavedProfile(profileDraft)
            setProfileSavedAt(new Date().toISOString())
          }}
          onLogout={onLogoutManager}
        />
      </section>
    )
  }

  return (
    <section className="grid gap-6">
      {activeSection === 'createFlight' ? (
        <CreateFlightSection
          isBusy={isBusy}
          translate={translate}
          onCreateManagerFlight={onCreateManagerFlight}
          onValidationError={onValidationError}
        />
      ) : null}

      {activeSection === 'flightManagement' ? (
        selectedFlight ? (
          <ManagerFlightOrdersSection
            currentLanguage={currentLanguage}
            flight={selectedFlight}
            isLoading={isLoadingSelectedFlightOrders}
            orders={selectedFlightOrders}
            profile={savedProfile}
            onBack={() => {
              setSelectedFlight(null)
              setSelectedFlightOrders([])
            }}
          />
        ) : (
          <FlightManagementSection
            currentLanguage={currentLanguage}
            flights={displayFlights}
            totalCount={managedFlights.length}
            searchDraft={searchDraft}
            hasSubmittedSearch={submittedSearch !== null}
            sortDirection={sortDirection}
            selectedDepartureAirport={selectedDepartureAirport}
            selectedArrivalAirport={selectedArrivalAirport}
            departureAirportOptions={departureAirportOptions}
            arrivalAirportOptions={arrivalAirportOptions}
            profile={savedProfile}
            translate={translate}
            onOpenFlight={async flight => {
              setSelectedFlight(flight)
              setIsLoadingSelectedFlightOrders(true)
              try {
                setSelectedFlightOrders(await onLoadManagerFlightOrders(flight.flightId))
              } finally {
                setIsLoadingSelectedFlightOrders(false)
              }
            }}
            onToggleFlightStatus={onToggleManagerFlightStatus}
            onSearchDraftChange={nextDraft => {
              setSearchDraft(nextDraft)
              if (nextDraft.departureCity !== searchDraft.departureCity) {
                setSelectedDepartureAirport('all')
              }
              if (nextDraft.arrivalCity !== searchDraft.arrivalCity) {
                setSelectedArrivalAirport('all')
              }
            }}
            onSearchSubmit={() => {
              const validationMessage = validateManagerFlightSearchDraft(searchDraft)
              if (validationMessage) {
                onValidationError(validationMessage)
                return
              }
              void onSearchManagerFlights(buildManagerFlightSearchPayload(searchDraft, sortDirection))
              setSubmittedSearch({ ...searchDraft })
              setSelectedDepartureAirport('all')
              setSelectedArrivalAirport('all')
            }}
            onSwapSearchRoute={() =>
              setSearchDraft(current => ({
                ...current,
                departureCity: current.arrivalCity,
                arrivalCity: current.departureCity,
              }))
            }
            onDepartureAirportChange={setSelectedDepartureAirport}
            onArrivalAirportChange={setSelectedArrivalAirport}
            onSortDirectionChange={nextDirection => {
              setSortDirection(nextDirection)
              void onSearchManagerFlights(buildManagerFlightSearchPayload(searchDraft, nextDirection))
            }}
          />
        )
      ) : null}

      {activeSection === 'userFeedback' ? (
        <section className="grid gap-5">
          <FeedbackConversationWorkspace
            audience="Manager"
            audienceDisplayName={managerSession.displayName}
            emptyTitle={translate('feedback.managerTitle')}
            emptyDescription={translate('feedback.managerEmpty')}
            threads={managerThreads}
            fullScreen
            translate={translate}
            unreadCountSelector={thread => thread.unreadByManager}
            onMarkRead={markFeedbackThreadRead}
            onSendMessage={(threadId, body) =>
              sendFeedbackMessage({
                threadId,
                senderRole: 'Manager',
                senderDisplayName: managerSession.displayName,
                body,
              })
            }
            onHandleCancellationRequest={(threadId, messageId, status, managerNote) =>
              handleOrderCancellationRequest({
                threadId,
                messageId,
                status,
                managerNote,
                handledBy: managerSession.displayName,
                handlerRole: 'Manager',
              })
            }
          />
        </section>
      ) : null}

      {activeSection === 'managerProfile' ? (
        <ManagerProfileSection
          managerSession={managerSession}
          profileDraft={profileDraft}
          profileSavedAt={profileSavedAt}
          currentLanguage={currentLanguage}
          onProfileChange={setProfileDraft}
          onSaveProfile={async () => {
            await onUpdateAirlineManagerProfile({
              displayName: profileDraft.displayName,
              airlineName: profileDraft.companyName,
              airlineCode: profileDraft.airlineCode,
              logoAssetPath: profileDraft.logoPath.trim() || null,
            })
            setSavedProfile(profileDraft)
            setProfileSavedAt(new Date().toISOString())
          }}
          onLogout={onLogoutManager}
        />
      ) : null}

      <div className="hidden">
        {managerTasks.length}
        {managerRefundTasks.length}
      </div>
    </section>
  )
}

import type { ChangeEventHandler, ReactNode } from 'react'
import { useEffect, useMemo, useState } from 'react'
import { ArrowLeftRight } from 'lucide-react'

import { handleOrderCancellationRequest, markFeedbackThreadRead, sendFeedbackMessage, useFeedbackChatStore } from '@/app/stores/feedback-chat-store'
import { flightCityOptions, formatFlightAirportLabel, formatFlightRouteCity, getFlightDetailsPlannerCityAirportCodes } from '@/app/stores/models/flights/flightConstants'
import { getFlightDetailsPlannerAirlineDisplayNameByCode, getFlightDetailsPlannerAirlineLogoPathByCode } from '@/app/stores/models/flights/flightAirlineCatalog'
import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { ManagerFlightOrderResponse } from '@/lib/mvp-types/manager'
import { formatIsoDateTime, localizeBedType, localizeCabinClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'
import type { AirlineWorkspaceSection, ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { normalizeDateTimeInput } from '@/pages/ManagerPage/components/managers/manager-panel-shared'
import { FeedbackConversationWorkspace } from '@/pages/shared/feedback/FeedbackConversationWorkspace'

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

const timeWindows = ['00:00-03:59', '04:00-07:59', '08:00-11:59', '12:00-15:59', '16:00-19:59', '20:00-23:59']

type ManagerFlightSearchDraft = {
  departureCity: string
  arrivalCity: string
  departureDate: string
  timeRange: string
}

const defaultManagerFlightSearchDraft: ManagerFlightSearchDraft = {
  departureCity: '',
  arrivalCity: '',
  departureDate: '',
  timeRange: 'all',
}

const createFlightTimeWindows = [
  { value: '00:00-03:59', label: '凌晨 00:00-03:59', start: '00:00', end: '03:59' },
  { value: '04:00-07:59', label: '清晨 04:00-07:59', start: '04:00', end: '07:59' },
  { value: '08:00-11:59', label: '上午 08:00-11:59', start: '08:00', end: '11:59' },
  { value: '12:00-15:59', label: '下午 12:00-15:59', start: '12:00', end: '15:59' },
  { value: '16:00-19:59', label: '傍晚 16:00-19:59', start: '16:00', end: '19:59' },
  { value: '20:00-23:59', label: '夜间 20:00-23:59', start: '20:00', end: '23:59' },
]

type CreateFlightDraft = {
  flightNumber: string
  departureCity: string
  departureAirport: string
  arrivalCity: string
  arrivalAirport: string
  departureDate: string
  timeRange: string
  departureClock: string
  arrivalClock: string
  economySeatCount: number
  economyPrice: string
  economyDiscounted: boolean
  economyDiscountRate: string
  premiumEconomySeatCount: number
  premiumEconomyPrice: string
  premiumEconomyDiscounted: boolean
  premiumEconomyDiscountRate: string
  businessSeatCount: number
  businessPrice: string
  businessDiscounted: boolean
  businessDiscountRate: string
  firstSeatCount: number
  firstPrice: string
  firstDiscounted: boolean
  firstDiscountRate: string
}

const defaultCreateFlightDraft: CreateFlightDraft = {
  flightNumber: '',
  departureCity: '',
  departureAirport: '',
  arrivalCity: '',
  arrivalAirport: '',
  departureDate: '',
  timeRange: createFlightTimeWindows[2].value,
  departureClock: createFlightTimeWindows[2].start,
  arrivalClock: '10:00',
  economySeatCount: 120,
  economyPrice: '680',
  economyDiscounted: false,
  economyDiscountRate: '10',
  premiumEconomySeatCount: 36,
  premiumEconomyPrice: '1180',
  premiumEconomyDiscounted: false,
  premiumEconomyDiscountRate: '10',
  businessSeatCount: 24,
  businessPrice: '2880',
  businessDiscounted: false,
  businessDiscountRate: '10',
  firstSeatCount: 8,
  firstPrice: '4880',
  firstDiscounted: false,
  firstDiscountRate: '10',
}

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
  const [searchDraft, setSearchDraft] = useState<ManagerFlightSearchDraft>(defaultManagerFlightSearchDraft)
  const [submittedSearch, setSubmittedSearch] = useState<ManagerFlightSearchDraft | null>(null)
  const [sortDirection, setSortDirection] = useState<'asc' | 'desc'>('asc')
  const [selectedDepartureAirport, setSelectedDepartureAirport] = useState('all')
  const [selectedArrivalAirport, setSelectedArrivalAirport] = useState('all')
  const [selectedFlight, setSelectedFlight] = useState<FlightPlannerResponse | null>(null)
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
    () => buildManagerAirportOptions(searchDraft.departureCity, managedFlights.map(flight => flight.departureAirport)),
    [managedFlights, searchDraft.departureCity],
  )
  const arrivalAirportOptions = useMemo(
    () => buildManagerAirportOptions(searchDraft.arrivalCity, managedFlights.map(flight => flight.arrivalAirport)),
    [managedFlights, searchDraft.arrivalCity],
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

function CreateFlightSection({
  isBusy,
  onCreateManagerFlight,
  onValidationError,
}: {
  isBusy: boolean
  translate: (translationKey: string) => string
  onCreateManagerFlight: ManagerPanelProps['onCreateManagerFlight']
  onValidationError: ManagerPanelProps['onValidationError']
}) {
  const [draft, setDraft] = useState<CreateFlightDraft>(defaultCreateFlightDraft)
  const departureAirportOptions = useMemo(() => getFlightDetailsPlannerCityAirportCodes(draft.departureCity), [draft.departureCity])
  const arrivalAirportOptions = useMemo(() => getFlightDetailsPlannerCityAirportCodes(draft.arrivalCity), [draft.arrivalCity])
  const selectedWindow = createFlightTimeWindows.find(option => option.value === draft.timeRange) ?? createFlightTimeWindows[2]

  function validateDraft(): string | null {
    if (!draft.flightNumber.trim()) return '请填写航班号。'
    if (!draft.departureCity || !draft.arrivalCity) return '请先选择出发地点和目的地。'
    if (draft.departureCity === draft.arrivalCity) return '出发地点和目的地不能相同。'
    if (!draft.departureAirport || !draft.arrivalAirport) return '请根据城市选择对应机场。'
    if (!draft.departureDate) return '请填写出发日期。'
    if (!isClockInsideWindow(draft.departureClock, selectedWindow.start, selectedWindow.end) || !isClockInsideWindow(draft.arrivalClock, selectedWindow.start, selectedWindow.end)) {
      return '具体出发时间和到达时间必须落在所选时段内。'
    }
    if (buildLocalDateTime(draft.departureDate, draft.arrivalClock) <= buildLocalDateTime(draft.departureDate, draft.departureClock)) return '到达时间必须晚于出发时间。'
    if ([draft.economySeatCount, draft.premiumEconomySeatCount, draft.businessSeatCount, draft.firstSeatCount].some(value => value <= 0)) return '每个舱位的数量都要大于 0。'
    if ([draft.economyPrice, draft.premiumEconomyPrice, draft.businessPrice, draft.firstPrice].some(value => Number(value) <= 0)) return '每个舱位的原价都要大于 0。'
    const discountRates = [draft.economyDiscountRate, draft.premiumEconomyDiscountRate, draft.businessDiscountRate, draft.firstDiscountRate].map(Number)
    if (discountRates.some(value => !Number.isFinite(value) || value <= 0 || value > 10)) return '折扣要填写 0 到 10 之间的数字，例如 8.5 表示八五折。'
    return null
  }

  return (
    <form
      className="mx-auto grid w-full max-w-[75%] gap-7 border border-slate-200 bg-white p-7 text-slate-950 shadow-sm shadow-slate-200/50 max-xl:max-w-full"
      onSubmit={async event => {
        event.preventDefault()
        const validationMessage = validateDraft()
        if (validationMessage) {
          onValidationError(validationMessage)
          return
        }
        await onCreateManagerFlight({
          flightNumber: draft.flightNumber.trim(),
          departureAirport: draft.departureAirport,
          arrivalAirport: draft.arrivalAirport,
          departureTime: normalizeDateTimeInput(buildLocalDateTime(draft.departureDate, draft.departureClock)),
          arrivalTime: normalizeDateTimeInput(buildLocalDateTime(draft.departureDate, draft.arrivalClock)),
          economyCabin: {
            seatCount: draft.economySeatCount,
            originalPrice: draft.economyPrice,
            discounted: draft.economyDiscounted,
            discountRate: draft.economyDiscountRate,
          },
          premiumEconomyCabin: {
            seatCount: draft.premiumEconomySeatCount,
            originalPrice: draft.premiumEconomyPrice,
            discounted: draft.premiumEconomyDiscounted,
            discountRate: draft.premiumEconomyDiscountRate,
          },
          businessCabin: {
            seatCount: draft.businessSeatCount,
            originalPrice: draft.businessPrice,
            discounted: draft.businessDiscounted,
            discountRate: draft.businessDiscountRate,
          },
          firstCabin: {
            seatCount: draft.firstSeatCount,
            originalPrice: draft.firstPrice,
            discounted: draft.firstDiscounted,
            discountRate: draft.firstDiscountRate,
          },
          currency: 'CNY',
        })
        setDraft(defaultCreateFlightDraft)
      }}
    >
      <div>
        <p className="m-0 text-base font-bold text-slate-500">创建航班</p>
        <h3 className="m-0 text-3xl font-black text-slate-950">填写核心航班信息</h3>
      </div>

      <div className="grid gap-5">
        <div className="grid gap-4 xl:grid-cols-2">
          <CreateFlightField label="出发地点" important>
            <CitySelect value={draft.departureCity} placeholder="请选择出发地点" onChange={event => setDraft({ ...draft, departureCity: event.target.value, departureAirport: '' })} />
          </CreateFlightField>
          <CreateFlightField label="出发机场" important>
            <select value={draft.departureAirport} onChange={event => setDraft({ ...draft, departureAirport: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950">
              <option value="">请先选择出发机场</option>
              {departureAirportOptions.map(airport => <option key={airport} value={airport}>{formatFlightAirportLabel(airport)}</option>)}
            </select>
          </CreateFlightField>
        </div>

        <div className="grid gap-4 xl:grid-cols-2">
          <CreateFlightField label="目的地" important>
            <CitySelect value={draft.arrivalCity} placeholder="请选择目的地" onChange={event => setDraft({ ...draft, arrivalCity: event.target.value, arrivalAirport: '' })} />
          </CreateFlightField>
          <CreateFlightField label="到达机场" important>
            <select value={draft.arrivalAirport} onChange={event => setDraft({ ...draft, arrivalAirport: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950">
              <option value="">请先选择到达机场</option>
              {arrivalAirportOptions.map(airport => <option key={airport} value={airport}>{formatFlightAirportLabel(airport)}</option>)}
            </select>
          </CreateFlightField>
        </div>

        <div className="grid gap-4 xl:grid-cols-4">
          <CreateFlightField label="日期" important>
            <input type="date" value={draft.departureDate} onChange={event => setDraft({ ...draft, departureDate: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950" />
          </CreateFlightField>
          <CreateFlightField label="时段" important>
            <select
              value={draft.timeRange}
              onChange={event => {
                const nextWindow = createFlightTimeWindows.find(option => option.value === event.target.value) ?? createFlightTimeWindows[2]
                setDraft({ ...draft, timeRange: nextWindow.value, departureClock: nextWindow.start, arrivalClock: bumpClockInsideWindow(nextWindow.start, nextWindow.end) })
              }}
              className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
            >
              {createFlightTimeWindows.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </CreateFlightField>
          <CreateFlightField label="出发时间" important>
            <input type="time" min={selectedWindow.start} max={selectedWindow.end} value={draft.departureClock} onChange={event => setDraft({ ...draft, departureClock: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950" />
          </CreateFlightField>
          <CreateFlightField label="到达时间" important>
            <input type="time" min={selectedWindow.start} max={selectedWindow.end} value={draft.arrivalClock} onChange={event => setDraft({ ...draft, arrivalClock: event.target.value })} className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950" />
          </CreateFlightField>
        </div>

        <CreateFlightField label="航班号">
          <input value={draft.flightNumber} onChange={event => setDraft({ ...draft, flightNumber: event.target.value })} className="min-h-12 w-full border-2 border-slate-300 bg-white px-4 text-lg font-medium text-slate-950 outline-none transition focus:border-slate-950" />
        </CreateFlightField>
      </div>

      <div className="grid gap-4">
        <h4 className="m-0 text-2xl font-black text-slate-950">舱位价格与数量</h4>
        <div className="grid gap-4 xl:grid-cols-2 2xl:grid-cols-4">
          <CabinDraftCard title="经济舱" seats={draft.economySeatCount} price={draft.economyPrice} discounted={draft.economyDiscounted} discountRate={draft.economyDiscountRate} onSeatsChange={economySeatCount => setDraft({ ...draft, economySeatCount })} onPriceChange={economyPrice => setDraft({ ...draft, economyPrice })} onDiscountedChange={economyDiscounted => setDraft({ ...draft, economyDiscounted })} onDiscountRateChange={economyDiscountRate => setDraft({ ...draft, economyDiscountRate })} />
          <CabinDraftCard title="超级经济舱" seats={draft.premiumEconomySeatCount} price={draft.premiumEconomyPrice} discounted={draft.premiumEconomyDiscounted} discountRate={draft.premiumEconomyDiscountRate} onSeatsChange={premiumEconomySeatCount => setDraft({ ...draft, premiumEconomySeatCount })} onPriceChange={premiumEconomyPrice => setDraft({ ...draft, premiumEconomyPrice })} onDiscountedChange={premiumEconomyDiscounted => setDraft({ ...draft, premiumEconomyDiscounted })} onDiscountRateChange={premiumEconomyDiscountRate => setDraft({ ...draft, premiumEconomyDiscountRate })} />
          <CabinDraftCard title="商务舱" seats={draft.businessSeatCount} price={draft.businessPrice} discounted={draft.businessDiscounted} discountRate={draft.businessDiscountRate} onSeatsChange={businessSeatCount => setDraft({ ...draft, businessSeatCount })} onPriceChange={businessPrice => setDraft({ ...draft, businessPrice })} onDiscountedChange={businessDiscounted => setDraft({ ...draft, businessDiscounted })} onDiscountRateChange={businessDiscountRate => setDraft({ ...draft, businessDiscountRate })} />
          <CabinDraftCard title="头等舱" seats={draft.firstSeatCount} price={draft.firstPrice} discounted={draft.firstDiscounted} discountRate={draft.firstDiscountRate} onSeatsChange={firstSeatCount => setDraft({ ...draft, firstSeatCount })} onPriceChange={firstPrice => setDraft({ ...draft, firstPrice })} onDiscountedChange={firstDiscounted => setDraft({ ...draft, firstDiscounted })} onDiscountRateChange={firstDiscountRate => setDraft({ ...draft, firstDiscountRate })} />
        </div>
      </div>

      <button className="inline-flex min-h-12 w-fit items-center justify-center bg-pink-500 px-8 py-3 text-base font-bold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
        创建航班
      </button>
    </form>
  )
}

function CreateFlightField({ label, important = false, children }: { label: string; important?: boolean; children: ReactNode }) {
  return (
    <label className="grid gap-2">
      <span className={important ? 'text-xl font-black text-slate-950' : 'text-base font-bold text-slate-500'}>{label}</span>
      {children}
    </label>
  )
}

function CabinDraftCard({
  title,
  seats,
  price,
  discounted,
  discountRate,
  onSeatsChange,
  onPriceChange,
  onDiscountedChange,
  onDiscountRateChange,
}: {
  title: string
  seats: number
  price: string
  discounted: boolean
  discountRate: string
  onSeatsChange: (value: number) => void
  onPriceChange: (value: string) => void
  onDiscountedChange: (value: boolean) => void
  onDiscountRateChange: (value: string) => void
}) {
  const actualPrice = calculateCabinActualPrice(price, discounted, discountRate)
  return (
    <div className="grid gap-3 border border-slate-200 bg-slate-50 p-4">
      <strong className="text-lg font-black text-slate-950">{title}</strong>
      <label className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">数量</span>
        <input type="number" min={1} value={seats} onChange={event => onSeatsChange(Number(event.target.value))} className="min-h-11 border-2 border-slate-300 bg-white px-3 text-base outline-none focus:border-slate-950" />
      </label>
      <label className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">原价</span>
        <input type="number" min={1} value={price} onChange={event => onPriceChange(event.target.value)} className="min-h-11 border-2 border-slate-300 bg-white px-3 text-base outline-none focus:border-slate-950" />
      </label>
      <label className="flex items-center gap-2 text-sm font-bold text-slate-600">
        <input type="checkbox" checked={discounted} onChange={event => onDiscountedChange(event.target.checked)} className="h-4 w-4 accent-pink-500" />
        是否打折
      </label>
      <label className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">折扣</span>
        <input type="number" min={0.1} max={10} step={0.1} value={discountRate} disabled={!discounted} onChange={event => onDiscountRateChange(event.target.value)} className="min-h-11 border-2 border-slate-300 bg-white px-3 text-base outline-none focus:border-slate-950 disabled:bg-slate-100 disabled:text-slate-400" />
      </label>
      <p className="m-0 text-base font-black text-orange-600">{`实际价格：¥${actualPrice}`}</p>
    </div>
  )
}

function ManagerFlightSearchCard({
  draft,
  onDraftChange,
  onSubmit,
  onSwapRoute,
}: {
  draft: ManagerFlightSearchDraft
  onDraftChange: (value: ManagerFlightSearchDraft) => void
  onSubmit: () => void
  onSwapRoute: () => void
}) {
  return (
    <section className="relative z-20 grid gap-6 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/70">
      <div className="grid gap-4 xl:grid-cols-[1.35fr_auto_1.35fr_1.05fr_1fr]">
        <ManagerSearchField label="出发地">
          <CitySelect
            value={draft.departureCity}
            placeholder="出发地"
            onChange={event => onDraftChange({ ...draft, departureCity: event.target.value })}
          />
        </ManagerSearchField>

        <div className="flex items-end justify-center">
          <button
            type="button"
            aria-label="交换出发地和目的地"
            className="mb-1 inline-flex h-14 w-14 items-center justify-center border-2 border-slate-300 bg-white text-slate-600 transition hover:border-slate-950 hover:text-slate-950"
            onClick={onSwapRoute}
          >
            <ArrowLeftRight className="h-6 w-6" />
          </button>
        </div>

        <ManagerSearchField label="目的地">
          <CitySelect
            value={draft.arrivalCity}
            placeholder="目的地"
            onChange={event => onDraftChange({ ...draft, arrivalCity: event.target.value })}
          />
        </ManagerSearchField>

        <ManagerSearchField label="出发日期">
          <input
            type="date"
            value={draft.departureDate}
            onChange={event => onDraftChange({ ...draft, departureDate: event.target.value })}
            className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
          />
        </ManagerSearchField>

        <ManagerSearchField label="出发时段">
          <select
            value={draft.timeRange}
            onChange={event => onDraftChange({ ...draft, timeRange: event.target.value })}
            className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
          >
            <option value="all">全天</option>
            {timeWindows.map(option => (
              <option key={option} value={option}>
                {option}
              </option>
            ))}
          </select>
        </ManagerSearchField>
      </div>

      <div className="flex justify-center">
        <button
          type="button"
          className="inline-flex min-h-14 min-w-64 items-center justify-center bg-gradient-to-r from-amber-400 to-orange-500 px-10 py-3 text-xl font-bold text-white shadow-xl shadow-orange-200/70 transition hover:from-amber-500 hover:to-orange-600"
          onClick={onSubmit}
        >
          搜索航班
        </button>
      </div>
    </section>
  )
}

function ManagerSearchField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="grid gap-2">
      <span className="text-base font-medium text-slate-500">{label}</span>
      {children}
    </label>
  )
}

function CitySelect({
  value,
  placeholder,
  onChange,
}: {
  value: string
  placeholder: string
  onChange: ChangeEventHandler<HTMLSelectElement>
}) {
  return (
    <select
      value={value}
      onChange={onChange}
      className="min-h-14 w-full border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none transition focus:border-slate-950"
    >
      <option value="">{placeholder}</option>
      {flightCityOptions.map(city => (
        <option key={city} value={city}>
          {city}
        </option>
      ))}
    </select>
  )
}

function FlightManagementSection({
  currentLanguage,
  flights,
  totalCount,
  searchDraft,
  hasSubmittedSearch,
  sortDirection,
  selectedDepartureAirport,
  selectedArrivalAirport,
  departureAirportOptions,
  arrivalAirportOptions,
  profile,
  translate,
  onSearchDraftChange,
  onSearchSubmit,
  onSwapSearchRoute,
  onOpenFlight,
  onToggleFlightStatus,
  onDepartureAirportChange,
  onArrivalAirportChange,
  onSortDirectionChange,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  flights: FlightPlannerResponse[]
  totalCount: number
  searchDraft: ManagerFlightSearchDraft
  hasSubmittedSearch: boolean
  sortDirection: 'asc' | 'desc'
  selectedDepartureAirport: string
  selectedArrivalAirport: string
  departureAirportOptions: string[]
  arrivalAirportOptions: string[]
  profile: AirlineProfileDraft
  translate: (translationKey: string) => string
  onSearchDraftChange: (value: ManagerFlightSearchDraft) => void
  onSearchSubmit: () => void
  onSwapSearchRoute: () => void
  onOpenFlight: (flight: FlightPlannerResponse) => void | Promise<void>
  onToggleFlightStatus: (flightId: string) => void | Promise<void>
  onDepartureAirportChange: (value: string) => void
  onArrivalAirportChange: (value: string) => void
  onSortDirectionChange: (value: 'asc' | 'desc') => void
}) {
  return (
    <section className="grid gap-6 bg-slate-100 px-6 pb-8 pt-8">
      <ManagerFlightSearchCard
        draft={searchDraft}
        onDraftChange={onSearchDraftChange}
        onSubmit={onSearchSubmit}
        onSwapRoute={onSwapSearchRoute}
      />

      <div className="flex flex-wrap items-end justify-between gap-4">
        <h3 className="m-0 text-3xl font-bold text-slate-950">
          <span className="mr-3 text-xl font-medium">航班管理:</span>
          {profile.companyName || '航空公司'}
          <span className="ml-4 text-lg font-medium text-slate-600">
            {hasSubmittedSearch ? `${flights.length} / ${totalCount} 条航班` : `${totalCount} 条航班`}
          </span>
        </h3>
      </div>

      <div className="grid items-center gap-4 bg-white px-6 py-5 lg:grid-cols-[minmax(0,1fr)_auto]">
        <div className="grid max-w-3xl grid-cols-2 gap-3">
          <FilterSelect label="出发机场" value={selectedDepartureAirport} onChange={onDepartureAirportChange} options={departureAirportOptions} renderOption={formatFlightAirportLabel} />
          <FilterSelect label="到达机场" value={selectedArrivalAirport} onChange={onArrivalAirportChange} options={arrivalAirportOptions} renderOption={formatFlightAirportLabel} />
        </div>
        <button
          type="button"
          className="text-base font-medium text-sky-600 transition hover:text-slate-950"
          onClick={() => onSortDirectionChange(sortDirection === 'asc' ? 'desc' : 'asc')}
        >
          {sortDirection === 'asc' ? '起飞时间早-晚' : '起飞时间晚-早'}
        </button>
      </div>

      <div className="overflow-hidden bg-white">
        {flights.length > 0 ? (
          flights.map(flight => (
            <ManagerFlightCard
              key={flight.flightId}
              flight={flight}
              profile={profile}
              currentLanguage={currentLanguage}
              onOpen={() => void onOpenFlight(flight)}
              onToggleStatus={() => void onToggleFlightStatus(flight.flightId)}
            />
          ))
        ) : (
          <p className="m-0 px-7 py-10 text-lg text-slate-500">{translate('flights.empty')}</p>
        )}
      </div>
    </section>
  )
}

function ManagerFlightCard({
  flight,
  profile,
  currentLanguage,
  onOpen,
  onToggleStatus,
}: {
  flight: FlightPlannerResponse
  profile: AirlineProfileDraft
  currentLanguage: ManagerPanelProps['currentLanguage']
  onOpen: () => void
  onToggleStatus: () => void
}) {
  const airlineName = profile.companyName || getFlightDetailsPlannerAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)
  const logoPath = profile.logoPath || getFlightDetailsPlannerAirlineLogoPathByCode(flight.airlineCode, flight.airlineLogoPath)
  const isOpenForBooking = flight.status === 'OpenForBooking'
  const isClosedForBooking = flight.status === 'ClosedForBooking'

  return (
    <article
      className="grid cursor-pointer gap-5 border-b border-slate-200 px-7 py-6 transition hover:bg-slate-50 last:border-b-0 xl:grid-cols-[minmax(260px,1.1fr)_minmax(360px,1.4fr)_minmax(180px,0.7fr)]"
      role="button"
      tabIndex={0}
      onClick={onOpen}
      onKeyDown={event => {
        if (event.key === 'Enter' || event.key === ' ') {
          event.preventDefault()
          onOpen()
        }
      }}
    >
      <div className="flex items-center gap-5">
        <div className="flex h-16 w-16 items-center justify-center border-2 border-slate-200 bg-slate-950 text-sm font-medium text-white">
          {logoPath ? <img src={logoPath} alt="" className="h-full w-full object-cover" /> : flight.airlineCode}
        </div>
        <div className="grid gap-1">
          <strong className="text-2xl font-bold text-slate-950">{airlineName}</strong>
          <div className="flex flex-wrap gap-3 text-base font-medium text-sky-600">
            <span>{flight.flightNumber}</span>
            <span>{flight.aircraftModel}</span>
          </div>
          <div className="flex flex-wrap gap-2 text-sm text-slate-500">
            {(flight.cabinInventories ?? []).map(cabin => (
              <span key={cabin.inventoryId}>{localizeCabinClass(cabin.cabinClass, currentLanguage)}</span>
            ))}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">
        <TimeBlock time={formatFlightClock(flight.departureTime)} airport={formatFlightAirportLabel(flight.departureAirport)} />
        <div className="h-px min-w-24 bg-slate-200" />
        <TimeBlock time={formatFlightClock(flight.arrivalTime)} airport={formatFlightAirportLabel(flight.arrivalAirport)} />
      </div>

      <div className="grid content-center justify-items-end gap-2 text-right">
        <button
          type="button"
          className={[
            'inline-flex min-h-11 items-center justify-center border px-5 py-2 text-base font-black transition',
            isOpenForBooking
              ? 'border-emerald-200 bg-emerald-50 text-emerald-700 hover:border-emerald-700'
              : isClosedForBooking
                ? 'border-rose-200 bg-rose-50 text-rose-700 hover:border-rose-700'
                : 'border-slate-200 bg-slate-50 text-slate-700 hover:border-slate-700',
          ].join(' ')}
          onClick={event => {
            event.stopPropagation()
            onToggleStatus()
          }}
        >
          {isOpenForBooking ? '可预订' : isClosedForBooking ? '不可预订' : mapBackendStatusToProductLabel(flight.status, currentLanguage)}
        </button>
        <span className="text-sm text-slate-500">{`${formatFlightRouteCity(flight.departureAirport)} -> ${formatFlightRouteCity(flight.arrivalAirport)}`}</span>
      </div>
    </article>
  )
}

function ManagerFlightOrdersSection({
  currentLanguage,
  flight,
  isLoading,
  orders,
  profile,
  onBack,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  flight: FlightPlannerResponse
  isLoading: boolean
  orders: ManagerFlightOrderResponse[]
  profile: AirlineProfileDraft
  onBack: () => void
}) {
  const cabinSections = [
    { key: 'ECONOMY', label: '经济舱' },
    { key: 'PREMIUM_ECONOMY', label: '超级经济舱' },
    { key: 'BUSINESS', label: '商务舱' },
    { key: 'FIRST', label: '头等舱' },
  ]
  const airlineName = profile.companyName || getFlightDetailsPlannerAirlineDisplayNameByCode(flight.airlineCode, flight.airlineName)

  return (
    <section className="grid gap-6 bg-slate-100 px-6 pb-8 pt-8">
      <div className="grid gap-5 bg-white p-7">
        <button
          type="button"
          className="inline-flex min-h-11 w-fit items-center justify-center border border-slate-300 bg-white px-5 py-2 text-sm font-bold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
          onClick={onBack}
        >
          返回航班管理
        </button>

        <div className="grid gap-5 xl:grid-cols-[minmax(260px,1fr)_minmax(360px,1.2fr)]">
          <div>
            <p className="m-0 text-sm font-bold text-slate-500">航班订单</p>
            <h3 className="m-0 text-3xl font-black text-slate-950">{airlineName}</h3>
            <p className="m-0 text-base font-semibold text-sky-600">{`${flight.flightNumber} ${flight.aircraftModel ?? ''}`}</p>
          </div>
          <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">
            <TimeBlock time={formatFlightClock(flight.departureTime)} airport={formatFlightAirportLabel(flight.departureAirport)} />
            <div className="h-px min-w-24 bg-slate-200" />
            <TimeBlock time={formatFlightClock(flight.arrivalTime)} airport={formatFlightAirportLabel(flight.arrivalAirport)} />
          </div>
        </div>
      </div>

      {isLoading ? <p className="m-0 bg-white px-7 py-10 text-lg text-slate-500">正在读取订单...</p> : null}

      {!isLoading ? (
        <div className="grid gap-5">
          {cabinSections.map(section => {
            const cabinOrders = orders.filter(order => normalizeCabinKey(order.cabinClass) === section.key)
            return (
              <section key={section.key} className="grid gap-3 bg-white p-6">
                <div className="flex flex-wrap items-baseline justify-between gap-3">
                  <h4 className="m-0 text-2xl font-black text-slate-950">{section.label}</h4>
                  <span className="text-sm font-medium text-slate-500">{`${cabinOrders.length} 个订单`}</span>
                </div>
                {cabinOrders.length > 0 ? (
                  <ul className="grid gap-3">
                    {cabinOrders.map(order => (
                      <ManagerFlightOrderCard key={order.orderItemId} currentLanguage={currentLanguage} order={order} />
                    ))}
                  </ul>
                ) : (
                  <p className="m-0 border border-dashed border-slate-200 px-5 py-8 text-base text-slate-500">这个舱位暂时没有订单。</p>
                )}
              </section>
            )
          })}
        </div>
      ) : null}
    </section>
  )
}

function ManagerFlightOrderCard({
  currentLanguage,
  order,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  order: ManagerFlightOrderResponse
}) {
  return (
    <li className="grid gap-4 border border-slate-200 bg-white p-5 md:grid-cols-[1fr_1fr_auto]">
      <div className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">用户昵称</span>
        <strong className="text-xl font-black text-slate-950">{order.buyerNickname || order.buyerUserId}</strong>
      </div>
      <div className="grid gap-1">
        <span className="text-sm font-bold text-slate-500">出行人</span>
        {order.travelers.length > 0 ? (
          <div className="flex flex-wrap gap-2">
            {order.travelers.map(traveler => (
              <ManagerTravelerHoverCard key={traveler.travelerId} traveler={traveler} />
            ))}
          </div>
        ) : (
          <strong className="text-base font-bold text-slate-950">{formatManagerFlightTravelers(order)}</strong>
        )}
      </div>
      <div className="grid gap-1 text-left md:text-right">
        <span className="text-sm font-medium text-slate-500">{formatIsoDateTime(order.orderCreatedAt, '-')}</span>
        <span className={statusBadgeClassName(order.orderStatus)}>{mapBackendStatusToProductLabel(order.orderStatus, currentLanguage)}</span>
      </div>
    </li>
  )
}

function ManagerTravelerHoverCard({ traveler }: { traveler: ManagerFlightOrderResponse['travelers'][number] }) {
  return (
    <span className="group relative inline-flex">
      <button
        type="button"
        className="inline-flex min-h-9 items-center border border-slate-300 bg-white px-3 text-sm font-black text-slate-950 transition hover:border-pink-500 hover:text-pink-600"
      >
        {traveler.fullName}
      </button>
      <span className="pointer-events-none absolute left-0 top-11 z-20 hidden w-80 border border-slate-300 bg-white p-4 text-left shadow-xl group-hover:grid">
        <strong className="text-lg font-black text-slate-950">{traveler.fullName}</strong>
        <span className="mt-1 text-sm text-slate-600">{`${traveler.serviceSummary.age ?? '-'}岁 / ${traveler.basicInfo.gender} / ${traveler.basicInfo.nationality}`}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">证件</span>
        <span className="text-sm text-slate-950">{`${traveler.documentInfo.documentType} ${traveler.documentInfo.documentNumber}`}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">联系</span>
        <span className="text-sm text-slate-950">{traveler.serviceSummary.contactLabel || traveler.contactInfo.phone}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">偏好</span>
        <span className="text-sm text-slate-950">{formatTravelerPreferenceSummary(traveler)}</span>
        <span className="mt-3 text-sm font-bold text-slate-500">特殊要求</span>
        <span className={traveler.serviceSummary.warningLevel === 'attention' ? 'text-sm font-bold text-pink-600' : 'text-sm text-slate-950'}>
          {formatTravelerRequirementSummary(traveler)}
        </span>
      </span>
    </span>
  )
}
function clockToMinutes(clock: string): number {
  const [hourText, minuteText] = clock.split(':')
  return Number(hourText) * 60 + Number(minuteText)
}

function formatClockFromMinutes(totalMinutes: number): string {
  const hour = Math.floor(totalMinutes / 60).toString().padStart(2, '0')
  const minute = (totalMinutes % 60).toString().padStart(2, '0')
  return `${hour}:${minute}`
}

function isClockInsideWindow(clock: string, windowStart: string, windowEnd: string): boolean {
  const value = clockToMinutes(clock)
  return value >= clockToMinutes(windowStart) && value <= clockToMinutes(windowEnd)
}

function bumpClockInsideWindow(windowStart: string, windowEnd: string): string {
  const preferred = clockToMinutes(windowStart) + 90
  return formatClockFromMinutes(Math.min(preferred, clockToMinutes(windowEnd)))
}

function buildLocalDateTime(date: string, clock: string): string {
  return `${date}T${clock}`
}

function calculateCabinActualPrice(price: string, discounted: boolean, discountRate: string): string {
  const originalPrice = Number(price)
  const rate = Number(discountRate)
  if (!Number.isFinite(originalPrice) || originalPrice <= 0 || !Number.isFinite(rate) || rate <= 0) {
    return '--'
  }
  const actualPrice = discounted ? originalPrice * rate / 10 : originalPrice
  return actualPrice.toFixed(2)
}

function TimeBlock({ time, airport }: { time: string; airport: string }) {
  return (
    <div className="grid gap-1">
      <strong className="text-4xl font-bold leading-none text-slate-950">{time}</strong>
      <span className="text-base text-slate-600">{airport}</span>
    </div>
  )
}

function FilterSelect({
  label,
  value,
  options,
  onChange,
  renderOption = option => option,
}: {
  label: string
  value: string
  options: string[]
  onChange: (value: string) => void
  renderOption?: (value: string) => string
}) {
  return (
    <select
      className="h-14 w-full min-w-0 border-2 border-slate-400 bg-white px-4 text-lg text-slate-950 outline-none focus:border-sky-500"
      value={value}
      onChange={event => onChange(event.target.value)}
    >
      <option value="all">{label}</option>
      {options.map(option => (
        <option key={option} value={option}>
          {renderOption(option)}
        </option>
      ))}
    </select>
  )
}

function validateManagerFlightSearchDraft(draft: ManagerFlightSearchDraft): string | null {
  const departureCity = draft.departureCity.trim()
  const arrivalCity = draft.arrivalCity.trim()
  if (departureCity && arrivalCity && departureCity === arrivalCity) {
    return '出发地和目的地不能选择同一个地方。'
  }

  return null
}

function buildManagerFlightSearchPayload(draft: ManagerFlightSearchDraft, sortDirection: 'asc' | 'desc') {
  return {
    departureAirports: draft.departureCity ? getFlightDetailsPlannerCityAirportCodes(draft.departureCity) : undefined,
    arrivalAirports: draft.arrivalCity ? getFlightDetailsPlannerCityAirportCodes(draft.arrivalCity) : undefined,
    departureDate: draft.departureDate || undefined,
    timeRange: draft.timeRange === 'all' ? undefined : draft.timeRange,
    sortDirection,
  }
}

function buildManagerAirportOptions(cityName: string, fallbackAirportCodes: string[]): string[] {
  const cityAirportCodes = cityName ? getFlightDetailsPlannerCityAirportCodes(cityName) : []
  if (cityAirportCodes.length > 0) {
    return cityAirportCodes
  }

  return unique(fallbackAirportCodes)
}

type AirlineProfileDraft = {
  displayName: string
  companyName: string
  airlineCode: string
  logoPath: string
}

function ManagerProfileSection({
  managerSession,
  profileDraft,
  profileSavedAt,
  currentLanguage,
  onProfileChange,
  onSaveProfile,
  onLogout,
}: {
  managerSession: NonNullable<ManagerPanelProps['managerSession']>
  profileDraft: AirlineProfileDraft
  profileSavedAt: string | null
  currentLanguage: ManagerPanelProps['currentLanguage']
  onProfileChange: (draft: AirlineProfileDraft) => void
  onSaveProfile: () => Promise<void>
  onLogout: () => void
}) {
  return (
    <section className="grid gap-6 bg-slate-100 px-6 py-8">
      <div className="grid gap-6 bg-white p-7 md:grid-cols-[220px_minmax(0,1fr)]">
        <div className="grid content-start justify-items-center gap-4">
          <div className="flex h-32 w-32 items-center justify-center border-2 border-slate-200 bg-slate-950 text-xl font-bold text-white">
            {profileDraft.logoPath ? <img src={profileDraft.logoPath} alt="" className="h-full w-full object-cover" /> : profileDraft.airlineCode || 'LOGO'}
          </div>
          <strong className="text-2xl font-bold text-slate-950">{profileDraft.companyName || '航空公司'}</strong>
          <span className="text-sm font-medium text-slate-500">{mapBackendStatusToProductLabel(managerSession.status, currentLanguage)}</span>
        </div>

        <form
          className="grid gap-5"
          onSubmit={event => {
            event.preventDefault()
            void onSaveProfile()
          }}
        >
          <div>
            <p className="text-sm font-bold text-slate-500">管理者信息</p>
            <h3 className="m-0 text-3xl font-bold text-slate-950">资料设置</h3>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <label>管理者昵称<input value={profileDraft.displayName} onChange={event => onProfileChange({ ...profileDraft, displayName: event.target.value })} /></label>
            <label>登录邮箱<input value={managerSession.email} readOnly /></label>
            <label>航空公司名称<input value={profileDraft.companyName} onChange={event => onProfileChange({ ...profileDraft, companyName: event.target.value })} /></label>
            <label>航空公司代码<input value={profileDraft.airlineCode} onChange={event => onProfileChange({ ...profileDraft, airlineCode: event.target.value })} /></label>
            <label className="md:col-span-2">头像 / Logo 地址<input value={profileDraft.logoPath} onChange={event => onProfileChange({ ...profileDraft, logoPath: event.target.value })} /></label>
          </div>
          <div className="flex flex-wrap items-center gap-4">
            <button type="submit" className="inline-flex min-h-12 items-center justify-center bg-pink-500 px-8 py-3 text-base font-bold text-white transition hover:bg-pink-600">
              保存资料
            </button>
            <button
              type="button"
              className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-8 py-3 text-base font-bold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
              onClick={onLogout}
            >
              退出登录
            </button>
            {profileSavedAt ? <span className="text-sm font-medium text-slate-500">{`已保存 ${formatIsoDateTime(profileSavedAt, '-')}`}</span> : null}
          </div>
        </form>
      </div>
    </section>
  )
}

function HotelWorkspace({
  currentLanguage,
  isBusy,
  managerSession,
  managedHotels,
  translate,
  onCreateManagerRoomType,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  isBusy: boolean
  managerSession: NonNullable<ManagerPanelProps['managerSession']>
  managedHotels: ManagerPanelProps['managedHotels']
  translate: (translationKey: string) => string
  onCreateManagerRoomType: ManagerPanelProps['onCreateManagerRoomType']
}) {
  return (
    <section className="grid gap-5">
      <form
        className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const roomImageEntry = formData.get('roomImageFile')
          await onCreateManagerRoomType({
            managerId: managerSession.managerId,
            roomTypeName: String(formData.get('roomTypeName') ?? ''),
            capacity: Number(formData.get('capacity') ?? 2),
            bedType: String(formData.get('bedType') ?? 'queen'),
            nightlyPrice: String(formData.get('nightlyPrice') ?? '699'),
            currency: String(formData.get('currency') ?? 'CNY'),
            availableRooms: Number(formData.get('availableRooms') ?? 5),
            inventoryStartDate: String(formData.get('inventoryStartDate') ?? ''),
            inventoryEndDate: String(formData.get('inventoryEndDate') ?? ''),
            roomImageFile: roomImageEntry instanceof File && roomImageEntry.size > 0 ? roomImageEntry : null,
          })
          event.currentTarget.reset()
        }}
      >
        <h3 className="m-0 text-2xl font-bold text-slate-950">{translate('manager.createRoomType')}</h3>
        <div className="grid gap-4 md:grid-cols-3">
          <label>{translate('manager.roomTypeName')}<input name="roomTypeName" required /></label>
          <label>{translate('manager.capacity')}<input name="capacity" type="number" min={1} defaultValue={2} required /></label>
          <label>{translate('manager.nightlyPrice')}<input name="nightlyPrice" type="number" min={1} defaultValue={699} required /></label>
          <label>{translate('manager.availableRooms')}<input name="availableRooms" type="number" min={1} defaultValue={5} required /></label>
          <label>{translate('manager.inventoryStartDate')}<input name="inventoryStartDate" type="date" defaultValue="2026-04-01" required /></label>
          <label>{translate('manager.inventoryEndDate')}<input name="inventoryEndDate" type="date" defaultValue="2026-04-30" required /></label>
          <label className="md:col-span-3 grid gap-2">
            <span>{translate('manager.roomImage')}</span>
            <input name="roomImageFile" type="file" accept="image/png,image/jpeg,image/jpg,image/webp" />
            <span className="text-sm font-medium text-slate-500">可选，上传后会显示在房型卡片中间。</span>
          </label>
        </div>
        <button className="inline-flex min-h-11 w-fit items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white" type="submit" disabled={isBusy}>
          {translate('manager.createRoomType')}
        </button>
      </form>

      <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
        <h3 className="m-0 text-2xl font-bold text-slate-950">{translate('manager.hotelName')}</h3>
        {managedHotels.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('manager.empty')}</p>
        ) : (
          <ul className="grid gap-3">
            {managedHotels.map(hotel => (
              <li key={hotel.hotelId} className="grid gap-2 border border-slate-200 bg-slate-50 p-4">
                <strong className="text-xl font-black text-slate-950">{hotel.hotelName}</strong>
                <p className="m-0 text-sm font-medium text-slate-600">{`${translate('manager.hotelLocation')}: ${hotel.location}`}</p>
                <div className="flex flex-wrap gap-2">
                  {(hotel.roomTypes ?? []).map(roomType => (
                    <span key={roomType.roomTypeId} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                      {`${roomType.roomTypeName} · ${localizeBedType(roomType.bedType, currentLanguage)} · ${roomType.basePrice} ${roomType.currency}`}
                    </span>
                  ))}
                </div>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

export function HotelProfileSection({
  currentLanguage,
  managerSession,
  managedHotels,
  translate,
  onUpdateHotelManagerProfile,
  onLogoutManager,
}: {
  currentLanguage: ManagerPanelProps['currentLanguage']
  managerSession: NonNullable<ManagerPanelProps['managerSession']>
  managedHotels: ManagerPanelProps['managedHotels']
  translate: (translationKey: string) => string
  onUpdateHotelManagerProfile: ManagerPanelProps['onUpdateHotelManagerProfile']
  onLogoutManager: ManagerPanelProps['onLogoutManager']
}) {
  const currentHotel = managedHotels.find(hotel => hotel.hotelId === managerSession.scopeId) ?? managedHotels[0] ?? null
  const [profileDraft, setProfileDraft] = useState(() => buildHotelProfileDraft(managerSession, currentHotel))

  useEffect(() => {
    setProfileDraft(buildHotelProfileDraft(managerSession, currentHotel))
  }, [currentHotel?.hotelId, currentHotel?.hotelName, currentHotel?.location, managerSession.displayName, managerSession.email, managerSession.managerId])

  return (
    <section className="grid gap-6 bg-slate-100 px-6 py-8">
      <div className="grid gap-6 bg-white p-7 md:grid-cols-[220px_minmax(0,1fr)]">
        <div className="grid content-start justify-items-center gap-4">
          <div className="flex h-32 w-32 items-center justify-center border-2 border-slate-200 bg-slate-950 text-xl font-bold text-white">
            {currentHotel?.hotelName?.slice(0, 1) ?? '酒'}
          </div>
          <strong className="text-2xl font-bold text-slate-950">{currentHotel?.hotelName ?? '酒店'}</strong>
          <span className="text-sm font-medium text-slate-500">{mapBackendStatusToProductLabel(managerSession.status, currentLanguage)}</span>
        </div>

        <form
          className="grid gap-5"
          onSubmit={event => {
            event.preventDefault()
            void onUpdateHotelManagerProfile({
              managerId: managerSession.managerId,
              displayName: profileDraft.displayName.trim(),
              email: profileDraft.email.trim(),
              hotelName: profileDraft.hotelName.trim(),
              hotelLocation: profileDraft.hotelLocation.trim(),
            })
          }}
        >
          <div>
            <p className="text-sm font-bold text-slate-500">管理者信息</p>
            <h3 className="m-0 text-3xl font-bold text-slate-950">资料设置</h3>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.displayName')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.displayName}
                onChange={event => setProfileDraft({ ...profileDraft, displayName: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.email')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.email}
                onChange={event => setProfileDraft({ ...profileDraft, email: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.hotelName')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.hotelName}
                onChange={event => setProfileDraft({ ...profileDraft, hotelName: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950">
              {translate('manager.hotelLocation')}
              <input
                className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none"
                value={profileDraft.hotelLocation}
                onChange={event => setProfileDraft({ ...profileDraft, hotelLocation: event.target.value })}
              />
            </label>
            <label className="grid gap-2 text-lg font-medium text-slate-950 md:col-span-2">
              {translate('manager.scope')}
              <input className="min-h-14 border-2 border-slate-300 bg-white px-5 text-xl font-medium text-slate-950 outline-none" value={currentHotel?.hotelId ?? managerSession.scopeId} readOnly />
            </label>
          </div>

          <div className="flex flex-wrap items-center gap-5 pt-2">
            <button type="submit" className="inline-flex min-h-12 items-center justify-center bg-pink-500 px-12 py-3 text-base font-bold text-white transition hover:bg-pink-600">
              保存资料
            </button>
            <button
              type="button"
              className="inline-flex min-h-12 items-center justify-center border border-slate-300 bg-white px-12 py-3 text-base font-bold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
              onClick={onLogoutManager}
            >
              退出登录
            </button>
          </div>
        </form>
      </div>
    </section>
  )
}

type HotelProfileDraft = {
  displayName: string
  email: string
  hotelName: string
  hotelLocation: string
}

function buildHotelProfileDraft(
  managerSession: NonNullable<ManagerPanelProps['managerSession']>,
  currentHotel: ManagerPanelProps['managedHotels'][number] | null,
): HotelProfileDraft {
  return {
    displayName: managerSession.displayName,
    email: managerSession.email,
    hotelName: currentHotel?.hotelName ?? '',
    hotelLocation: currentHotel?.location ?? '',
  }
}

function buildProfileDraft(managerSession: ManagerPanelProps['managerSession'], managedFlights: FlightPlannerResponse[]): AirlineProfileDraft {
  const firstFlight = managedFlights[0]
  return {
    displayName: managerSession?.displayName ?? '',
    companyName: firstFlight ? getFlightDetailsPlannerAirlineDisplayNameByCode(firstFlight.airlineCode, firstFlight.airlineName) : '',
    airlineCode: firstFlight?.airlineCode ?? '',
    logoPath: firstFlight ? getFlightDetailsPlannerAirlineLogoPathByCode(firstFlight.airlineCode, firstFlight.airlineLogoPath) ?? '' : '',
  }
}

function formatFlightClock(isoDateTime: string | null): string {
  if (!isoDateTime) {
    return '--:--'
  }

  return new Date(isoDateTime).toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit',
    hour12: false,
  })
}

function normalizeCabinKey(value: string): string {
  const normalized = value.trim().toUpperCase().replaceAll('-', '_')
  if (normalized === 'PREMIUMECONOMY' || normalized === 'PREMIUM_ECONOMY') {
    return 'PREMIUM_ECONOMY'
  }
  if (normalized === 'BUSINESS') {
    return 'BUSINESS'
  }
  if (normalized === 'FIRST') {
    return 'FIRST'
  }
  return 'ECONOMY'
}

function formatManagerFlightTravelers(order: ManagerFlightOrderResponse): string {
  if (order.travelers.length > 0) {
    return order.travelers.map(traveler => `${traveler.fullName} (${traveler.documentNumber})`).join('、')
  }
  if (order.travelerIds.length > 0) {
    return order.travelerIds.join('、')
  }
  return '未选择出行人'
}

function formatTravelerPreferenceSummary(traveler: ManagerFlightOrderResponse['travelers'][number]): string {
  const seatLabels: Record<string, string> = {
    Window: '靠窗',
    Aisle: '靠过道',
    Middle: '中间座',
    NoPreference: '无座位偏好',
    window: '靠窗',
    aisle: '靠过道',
    middle: '中间座',
    none: '无座位偏好',
  }
  const mealLabels: Record<string, string> = {
    Standard: '标准餐',
    Vegetarian: '素食餐',
    Vegan: '纯素餐',
    Halal: '清真餐',
    Kosher: '犹太餐',
    ChildMeal: '儿童餐',
    NoPreference: '无餐食偏好',
    standard: '标准餐',
    vegetarian: '素食餐',
    vegan: '纯素餐',
    halal: '清真餐',
  }
  return [
    seatLabels[traveler.preferenceInfo.seatPreference] ?? traveler.preferenceInfo.seatPreference,
    mealLabels[traveler.preferenceInfo.mealPreference] ?? traveler.preferenceInfo.mealPreference,
    traveler.preferenceInfo.quietSeatPreferred ? '安静座位' : '',
  ].filter(Boolean).join(' / ')
}

function formatTravelerRequirementSummary(traveler: ManagerFlightOrderResponse['travelers'][number]): string {
  const requirement = traveler.specialRequirementInfo
  return [
    requirement.assistanceType && requirement.assistanceType !== 'none' ? requirement.assistanceType : '',
    requirement.requirementNote ?? '',
    requirement.hasLargeLuggage ? '大件行李' : '',
    requirement.luggageNote ?? '',
  ].filter(Boolean).join(' / ') || '无特殊要求'
}

function statusBadgeClassName(status: string): string {
  if (status === 'Refunded') {
    return 'inline-flex min-h-10 items-center justify-center border border-sky-200 bg-sky-50 px-4 text-base font-black text-sky-700'
  }
  if (status === 'Confirmed' || status === 'Paid' || status === 'Booked') {
    return 'inline-flex min-h-10 items-center justify-center border border-emerald-200 bg-emerald-50 px-4 text-base font-black text-emerald-700'
  }
  return 'inline-flex min-h-10 items-center justify-center border border-amber-200 bg-amber-50 px-4 text-base font-black text-amber-700'
}

function unique(values: string[]): string[] {
  return [...new Set(values.filter(Boolean))]
}

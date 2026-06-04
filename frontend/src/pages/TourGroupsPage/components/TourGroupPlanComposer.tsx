import { useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type {
  AppLanguage,
  AttractionResponse,
  FlightPlannerResponse,
  GroupPlanItemResponse,
  HotelPlannerResponse,
  SearchSuggestionResponse,
  TrainResponse,
} from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeCabinClass, localizeTrainSeatClass } from '@/lib/presenters/view-models'

type TourGroupPlanComposerProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  existingPlanItems: GroupPlanItemResponse[]
  translate: (translationKey: string) => string
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightPlannerResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelPlannerResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onCreatePlanItem: (payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<GroupPlanItemResponse | null>
  onCreateOptionForPlanItem: (
    planItemId: string,
    payload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) => Promise<void>
}

function nextDay(dateText: string): string {
  const date = new Date(`${dateText}T00:00:00`)
  date.setDate(date.getDate() + 1)
  return date.toISOString().slice(0, 10)
}

function atUtc(dateText: string, hour: string): string {
  return `${dateText}T${hour}:00Z`
}

function toInstantString(value: string): string {
  const parsedDate = new Date(value)
  if (Number.isNaN(parsedDate.getTime())) {
    return value
  }
  return parsedDate.toISOString()
}

function normalizeSearchToken(value: string): string {
  return value.trim().toLowerCase()
}

function resolveTrainStop(train: TrainResponse, query: string): TrainResponse['stops'][number] | null {
  const normalizedQuery = normalizeSearchToken(query)
  return (
    train.stops.find(stop => stop.stationCode.trim().toLowerCase() === normalizedQuery) ??
    train.stops.find(stop => normalizeSearchToken(stop.stationName) === normalizedQuery) ??
    train.stops.find(stop => normalizeSearchToken(stop.stationName).includes(normalizedQuery)) ??
    null
  )
}

function renderSuggestionList(
  suggestions: SearchSuggestionResponse[],
  onPick: (value: string) => void,
) {
  if (suggestions.length === 0) {
    return null
  }

  return (
    <ul className="grid gap-2 border border-sky-200 bg-white/85 p-3 shadow-sm shadow-sky-100/40">
      {suggestions.map(suggestion => (
        <li key={`${suggestion.resourceType}:${suggestion.value}`}>
          <button
            type="button"
            className="inline-flex items-center justify-center text-sm font-bold text-sky-600 underline-offset-4 hover:underline"
            onClick={() => onPick(suggestion.value)}
          >
            <strong>{suggestion.title}</strong>
          </button>
          <p className="text-sm leading-6 text-slate-500">{suggestion.subtitle}</p>
        </li>
      ))}
    </ul>
  )
}

export function TourGroupPlanComposer({
  currentLanguage,
  isBusy,
  existingPlanItems,
  translate,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
  onCreatePlanItem,
  onCreateOptionForPlanItem,
}: TourGroupPlanComposerProps) {
  const [itemType, setItemType] = useState('Flight')
  const [date, setDate] = useState('')
  const [hotelCheckOutDate, setHotelCheckOutDate] = useState('')
  const [departureLocation, setDepartureLocation] = useState('')
  const [arrivalLocation, setArrivalLocation] = useState('')
  const [location, setLocation] = useState('')
  const [searchMessage, setSearchMessage] = useState('')
  const [departureLocationSuggestions, setDepartureLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [arrivalLocationSuggestions, setArrivalLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [locationSuggestions, setLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [flightResults, setFlightResults] = useState<FlightPlannerResponse[]>([])
  const [hotelResults, setHotelResults] = useState<HotelPlannerResponse[]>([])
  const [trainResults, setTrainResults] = useState<TrainResponse[]>([])
  const [attractionResults, setAttractionResults] = useState<AttractionResponse[]>([])

  const nextSequenceNo = useMemo(
    () => Math.max(0, ...existingPlanItems.map(planItem => planItem.sequenceNo)) + 1,
    [existingPlanItems],
  )

  async function loadLocationSuggestions(
    nextLocation: string,
    target: 'departure' | 'arrival' | 'location',
  ) {
    const normalizedLocation = nextLocation.trim()
    if (normalizedLocation.length < 2) {
      if (target === 'departure') setDepartureLocationSuggestions([])
      if (target === 'arrival') setArrivalLocationSuggestions([])
      if (target === 'location') setLocationSuggestions([])
      return
    }

    try {
      const response = await travelMvpApiClient.listExploreSuggestions(normalizedLocation)
      const allowedTypes =
        itemType === 'Flight'
          ? new Set(['flight'])
          : itemType === 'Hotel'
            ? new Set(['hotel'])
            : itemType === 'Train'
              ? new Set(['train'])
              : new Set(['attraction'])

      const nextSuggestions = response.suggestions.filter(suggestion => allowedTypes.has(suggestion.resourceType)).slice(0, 6)
      if (target === 'departure') setDepartureLocationSuggestions(nextSuggestions)
      if (target === 'arrival') setArrivalLocationSuggestions(nextSuggestions)
      if (target === 'location') setLocationSuggestions(nextSuggestions)
    } catch {
      if (target === 'departure') setDepartureLocationSuggestions([])
      if (target === 'arrival') setArrivalLocationSuggestions([])
      if (target === 'location') setLocationSuggestions([])
    }
  }

  async function runSearch() {
    setSearchMessage('')
    setFlightResults([])
    setHotelResults([])
    setTrainResults([])
    setAttractionResults([])
    setDepartureLocationSuggestions([])
    setArrivalLocationSuggestions([])
    setLocationSuggestions([])

    if (!date) {
      setSearchMessage(translate('tourGroups.searchInputHint'))
      return
    }

    if (itemType === 'Flight') {
      const departure = departureLocation.trim()
      const arrival = arrivalLocation.trim()
      if (!departure || !arrival) {
        setSearchMessage('请填写出发地点和到达地点。')
        return
      }
      setFlightResults(await onSearchFlights({ departureAirport: departure, arrivalAirport: arrival, date }))
      return
    }

    if (itemType === 'Hotel') {
      if (!location.trim()) {
        setSearchMessage(translate('tourGroups.searchInputHint'))
        return
      }
      const effectiveCheckOutDate = hotelCheckOutDate || nextDay(date)
      if (effectiveCheckOutDate <= date) {
        setSearchMessage(translate('tourGroups.hotelDateRangeHint'))
        return
      }
      setHotelResults(await onSearchHotels({ location, checkInDate: date, checkOutDate: effectiveCheckOutDate }))
      return
    }

    if (itemType === 'Train') {
      const departure = departureLocation.trim()
      const arrival = arrivalLocation.trim()
      if (!departure || !arrival) {
        setSearchMessage('请填写出发地点和到达地点。')
        return
      }
      setTrainResults(await onSearchTrains({ fromStation: departure, toStation: arrival, date }))
      return
    }

    if (!location.trim()) {
      setSearchMessage(translate('tourGroups.searchInputHint'))
      return
    }
    setAttractionResults(await onSearchAttractions({ city: location }))
  }

  async function createPlanWithOption(
    planPayload: {
      itemType: string
      title: string
      description: string
      scheduledAt: string
      endsAt?: string | null
    },
    optionPayload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) {
    setSearchMessage('')
    const createdPlanItem = await onCreatePlanItem({
      ...planPayload,
      sequenceNo: nextSequenceNo,
    })
    if (!createdPlanItem) {
      setSearchMessage(translate('tourGroups.createPlanFailedHint'))
      return
    }
    await onCreateOptionForPlanItem(createdPlanItem.planItemId, optionPayload)
    setSearchMessage(translate('tourGroups.createPlanSuccessHint'))
  }

  const isRouteItem = itemType === 'Flight' || itemType === 'Train'
  const formGridClassName = itemType === 'Attraction' ? 'grid gap-4 md:grid-cols-3' : 'grid gap-4 md:grid-cols-4'

  return (
    <section className="grid gap-3 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('tourGroups.createPlanEyebrow')}</p>
          <h3>{translate('tourGroups.createPlanSearchTitle')}</h3>
          <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.createPlanSearchHint')}</p>
        </div>
      </div>

      <form
        className="grid gap-4 border border-sky-200 bg-white/85 p-5 text-slate-950 shadow-sm shadow-sky-100/40"
        onSubmit={async event => {
          event.preventDefault()
          await runSearch()
        }}
      >
        <div className={itemType === 'Hotel' ? 'grid gap-4 md:grid-cols-4 hotel' : formGridClassName}>
          <label className="grid gap-2">
            {translate('tourGroups.itemType')}
            <select value={itemType} onChange={event => setItemType(event.target.value)}>
              <option value="Flight">{translate('tourGroups.itemType.flight')}</option>
              <option value="Hotel">{translate('tourGroups.itemType.hotel')}</option>
              <option value="Train">{translate('tourGroups.itemType.train')}</option>
              <option value="Attraction">{translate('tourGroups.itemType.attraction')}</option>
            </select>
          </label>

          <label className="grid gap-2">
            {translate(itemType === 'Hotel' ? 'tourGroups.hotelCheckInDate' : 'tourGroups.search.date')}
            <input type="date" value={date} onChange={event => setDate(event.target.value)} required />
          </label>

          {isRouteItem ? (
            <label className="grid gap-2">
              出发地点
              <input
                value={departureLocation}
                onFocus={() => void loadLocationSuggestions(departureLocation, 'departure')}
                onChange={event => {
                  const nextValue = event.target.value
                  setDepartureLocation(nextValue)
                  void loadLocationSuggestions(nextValue, 'departure')
                }}
                required
              />
              {renderSuggestionList(departureLocationSuggestions, value => {
                setDepartureLocation(value)
                setDepartureLocationSuggestions([])
              })}
            </label>
          ) : null}

          {isRouteItem ? (
            <label className="grid gap-2">
              到达地点
              <input
                value={arrivalLocation}
                onFocus={() => void loadLocationSuggestions(arrivalLocation, 'arrival')}
                onChange={event => {
                  const nextValue = event.target.value
                  setArrivalLocation(nextValue)
                  void loadLocationSuggestions(nextValue, 'arrival')
                }}
                required
              />
              {renderSuggestionList(arrivalLocationSuggestions, value => {
                setArrivalLocation(value)
                setArrivalLocationSuggestions([])
              })}
            </label>
          ) : null}

          {!isRouteItem ? (
            <label className="grid gap-2">
              {translate('tourGroups.search.locationLabel')}
              <input
                value={location}
                onFocus={() => void loadLocationSuggestions(location, 'location')}
                onChange={event => {
                  const nextLocation = event.target.value
                  setLocation(nextLocation)
                  void loadLocationSuggestions(nextLocation, 'location')
                }}
                required
              />
              {renderSuggestionList(locationSuggestions, value => {
                setLocation(value)
                setLocationSuggestions([])
              })}
            </label>
          ) : null}

          {itemType === 'Hotel' ? (
            <label className="grid gap-2">
              {translate('tourGroups.hotelCheckOutDate')}
              <input
                type="date"
                value={hotelCheckOutDate}
                onChange={event => setHotelCheckOutDate(event.target.value)}
                required
              />
            </label>
          ) : null}
        </div>

        <div className="flex flex-wrap items-center gap-3">
          <button
            className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            type="submit"
            disabled={isBusy}
          >
            {translate('tourGroups.searchOptions')}
          </button>
        </div>

        {searchMessage ? <p className="text-sm leading-6 text-slate-500">{searchMessage}</p> : null}
      </form>

      {itemType === 'Flight' && flightResults.length > 0 ? (
        <ul className="grid gap-3">
          {flightResults.map(flight => (
            <li key={flight.flightId}>
              <div className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4 shadow-sm shadow-sky-100/40">
                <strong>{`${flight.airlineCode} ${flight.flightNumber}`}</strong>
                <p>{`${flight.departureAirport} → ${flight.arrivalAirport}`}</p>
                <p>{formatIsoDateTime(flight.departureTime, '-')}</p>
                <div className="grid gap-2">
                  {flight.cabinInventories.map(cabin => (
                    <button
                      className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      key={cabin.inventoryId}
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        void createPlanWithOption(
                          {
                            itemType: 'Flight',
                            title: `${flight.departureAirport} → ${flight.arrivalAirport}`,
                            description: `${flight.airlineName} ${flight.flightNumber}`,
                            scheduledAt: toInstantString(flight.departureTime),
                            endsAt: toInstantString(flight.arrivalTime),
                          },
                          {
                            resourceType: 'Flight',
                            resourceId: flight.flightId,
                            resourceVariantCode: cabin.cabinClass,
                            label: `${flight.flightNumber} ${localizeCabinClass(cabin.cabinClass, currentLanguage)}`,
                            description: `${flight.departureAirport} → ${flight.arrivalAirport} / ${cabin.unitPrice} ${cabin.currency}`,
                            defaultQuantity: 1,
                          },
                        )
                      }
                    >
                      {`${localizeCabinClass(cabin.cabinClass, currentLanguage)} / ${cabin.unitPrice} ${cabin.currency}`}
                    </button>
                  ))}
                </div>
              </div>
            </li>
          ))}
        </ul>
      ) : null}

      {itemType === 'Hotel' && hotelResults.length > 0 ? (
        <ul className="grid gap-3">
          {hotelResults.map(hotel => (
            <li key={hotel.hotelId}>
              <div className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4 shadow-sm shadow-sky-100/40">
                <strong>{hotel.hotelName}</strong>
                <p>{hotel.location}</p>
                <div className="grid gap-2">
                  {hotel.roomTypes.map(roomType => (
                    <button
                      className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      key={roomType.roomTypeId}
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        void createPlanWithOption(
                          {
                            itemType: 'Hotel',
                            title: `${hotel.location} stay`,
                            description: hotel.hotelName,
                            scheduledAt: atUtc(date, '15:00'),
                            endsAt: atUtc(hotelCheckOutDate || nextDay(date), '12:00'),
                          },
                          {
                            resourceType: 'HotelRoomType',
                            resourceId: roomType.roomTypeId,
                            label: `${hotel.hotelName} ${roomType.roomTypeName}`,
                            description: `${roomType.roomTypeName} / ${roomType.basePrice} ${roomType.currency}`,
                            defaultQuantity: 1,
                          },
                        )
                      }
                    >
                      {`${roomType.roomTypeName} / ${roomType.basePrice} ${roomType.currency}`}
                    </button>
                  ))}
                </div>
              </div>
            </li>
          ))}
        </ul>
      ) : null}

      {itemType === 'Train' && trainResults.length > 0 ? (
        <ul className="grid gap-3">
          {trainResults.map(train => {
            const fromStop = resolveTrainStop(train, departureLocation)
            const toStop = resolveTrainStop(train, arrivalLocation)
            return (
              <li key={train.trainId}>
                <div className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4 shadow-sm shadow-sky-100/40">
                  <strong>{train.trainNumber}</strong>
                  <p>{train.stops.map(stop => stop.stationCode).join(' → ')}</p>
                  <div className="grid gap-2">
                    {fromStop && toStop
                      ? train.seatInventories.map(seat => (
                          <button
                            className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                            key={seat.inventoryId}
                            type="button"
                            disabled={isBusy}
                            onClick={() =>
                              void createPlanWithOption(
                                {
                                  itemType: 'Train',
                                  title: `${fromStop.stationName} → ${toStop.stationName}`,
                                  description: train.trainNumber,
                                  scheduledAt: atUtc(date, '09:00'),
                                },
                                {
                                  resourceType: 'TrainJourneySeat',
                                  resourceId: train.trainId,
                                  resourceVariantCode: seat.seatClass,
                                  resourceContext: `${fromStop.stationCode}|${toStop.stationCode}`,
                                  label: `${train.trainNumber} ${localizeTrainSeatClass(seat.seatClass, currentLanguage)}`,
                                  description: `${fromStop.stationName} → ${toStop.stationName}`,
                                  defaultQuantity: 1,
                                },
                              )
                            }
                          >
                            {localizeTrainSeatClass(seat.seatClass, currentLanguage)}
                          </button>
                        ))
                      : <p className="text-sm leading-6 text-slate-500">请先填写出发地点和到达地点。</p>}
                  </div>
                </div>
              </li>
            )
          })}
        </ul>
      ) : null}

      {itemType === 'Attraction' && attractionResults.length > 0 ? (
        <ul className="grid gap-3">
          {attractionResults.map(attraction => (
            <li key={attraction.attractionId}>
              <div className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4 shadow-sm shadow-sky-100/40">
                <strong>{attraction.attractionName}</strong>
                <p>{`${attraction.city} / ${attraction.location}`}</p>
                <div className="grid gap-2">
                  {attraction.ticketTypes.map(ticketType => (
                    <button
                      className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      key={ticketType.ticketTypeId}
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        void createPlanWithOption(
                          {
                            itemType: 'Attraction',
                            title: attraction.attractionName,
                            description: ticketType.ticketTypeName,
                            scheduledAt: atUtc(date, '09:00'),
                          },
                          {
                            resourceType: 'AttractionTicketType',
                            resourceId: ticketType.ticketTypeId,
                            resourceContext: attraction.attractionId,
                            label: `${attraction.attractionName} ${ticketType.ticketTypeName}`,
                            description: `${ticketType.ticketTypeName} / ${ticketType.priceAmount} ${ticketType.priceCurrency}`,
                            defaultQuantity: 1,
                          },
                        )
                      }
                    >
                      {`${ticketType.ticketTypeName} / ${ticketType.priceAmount} ${ticketType.priceCurrency}`}
                    </button>
                  ))}
                </div>
              </div>
            </li>
          ))}
        </ul>
      ) : null}
    </section>
  )
}

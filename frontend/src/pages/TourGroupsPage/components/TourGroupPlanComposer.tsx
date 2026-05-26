import { useMemo, useState } from 'react'

import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AttractionResponse, FlightResponse, GroupPlanItemResponse, HotelResponse, SearchSuggestionResponse, TrainResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeCabinClass, localizeTrainSeatClass } from '@/lib/presenters/view-models'

type TourGroupPlanComposerProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  existingPlanItems: GroupPlanItemResponse[]
  translate: (translationKey: string) => string
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelResponse[]>
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

function parseRouteInput(value: string): { from: string; to: string } | null {
  const normalized = value.trim()
  if (!normalized) {
    return null
  }

  const separators = ['->', '→', '到', '-', '—']
  for (const separator of separators) {
    if (normalized.includes(separator)) {
      const [from, to] = normalized.split(separator, 2).map(part => part.trim())
      if (from && to) {
        return { from, to }
      }
    }
  }

  return null
}

function normalizeSearchToken(value: string): string {
  return value.trim().toLowerCase()
}

function resolveTrainStop(
  train: TrainResponse,
  query: string,
): TrainResponse['stops'][number] | null {
  const normalizedQuery = normalizeSearchToken(query)
  return (
    train.stops.find(stop => stop.stationCode.trim().toLowerCase() === normalizedQuery) ??
    train.stops.find(stop => normalizeSearchToken(stop.stationName) === normalizedQuery) ??
    train.stops.find(stop => normalizeSearchToken(stop.stationName).includes(normalizedQuery)) ??
    null
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
  const [location, setLocation] = useState('')
  const [searchMessage, setSearchMessage] = useState('')
  const [locationSuggestions, setLocationSuggestions] = useState<SearchSuggestionResponse[]>([])
  const [flightResults, setFlightResults] = useState<FlightResponse[]>([])
  const [hotelResults, setHotelResults] = useState<HotelResponse[]>([])
  const [trainResults, setTrainResults] = useState<TrainResponse[]>([])
  const [attractionResults, setAttractionResults] = useState<AttractionResponse[]>([])

  const nextSequenceNo = useMemo(
    () => Math.max(0, ...existingPlanItems.map(planItem => planItem.sequenceNo)) + 1,
    [existingPlanItems],
  )

  async function loadLocationSuggestions(nextLocation: string) {
    const normalizedLocation = nextLocation.trim()
    if (normalizedLocation.length < 2) {
      setLocationSuggestions([])
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
      setLocationSuggestions(response.suggestions.filter(suggestion => allowedTypes.has(suggestion.resourceType)).slice(0, 6))
    } catch {
      setLocationSuggestions([])
    }
  }

  async function runSearch() {
    setSearchMessage('')
    setFlightResults([])
    setHotelResults([])
    setTrainResults([])
    setAttractionResults([])

    if (!date || !location.trim()) {
      setSearchMessage(translate('tourGroups.searchInputHint'))
      return
    }

    if (itemType === 'Flight') {
      const route = parseRouteInput(location)
      if (!route) {
        setSearchMessage(translate('tourGroups.routeInputHint'))
        return
      }
      setFlightResults(await onSearchFlights({ departureAirport: route.from, arrivalAirport: route.to, date }))
      return
    }

    if (itemType === 'Hotel') {
      const effectiveCheckOutDate = hotelCheckOutDate || nextDay(date)
      if (effectiveCheckOutDate <= date) {
        setSearchMessage(translate('tourGroups.hotelDateRangeHint'))
        return
      }
      setHotelResults(await onSearchHotels({ location, checkInDate: date, checkOutDate: effectiveCheckOutDate }))
      return
    }

    if (itemType === 'Train') {
      const route = parseRouteInput(location)
      if (!route) {
        setSearchMessage(translate('tourGroups.routeInputHint'))
        return
      }
      setTrainResults(await onSearchTrains({ fromStation: route.from, toStation: route.to, date }))
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

  return (
    <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('tourGroups.createPlanEyebrow')}</p>
          <h3>{translate('tourGroups.createPlanSearchTitle')}</h3>
          <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.createPlanSearchHint')}</p>
        </div>
      </div>

      <form
        className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4"
        onSubmit={async event => {
          event.preventDefault()
          await runSearch()
        }}
      >
        <div className={itemType === 'Hotel' ? 'grid gap-4 md:grid-cols-3 hotel' : 'grid gap-4 md:grid-cols-3'}>
          <label>
            {translate('tourGroups.itemType')}
            <select value={itemType} onChange={event => setItemType(event.target.value)}>
              <option value="Flight">{translate('tourGroups.itemType.flight')}</option>
              <option value="Hotel">{translate('tourGroups.itemType.hotel')}</option>
              <option value="Train">{translate('tourGroups.itemType.train')}</option>
              <option value="Attraction">{translate('tourGroups.itemType.attraction')}</option>
            </select>
          </label>
          <label>
            {translate(itemType === 'Hotel' ? 'tourGroups.hotelCheckInDate' : 'tourGroups.search.date')}
            <input type="date" value={date} onChange={event => setDate(event.target.value)} required />
          </label>
          {itemType === 'Hotel' ? (
            <label>
              {translate('tourGroups.hotelCheckOutDate')}
              <input
                type="date"
                value={hotelCheckOutDate}
                onChange={event => setHotelCheckOutDate(event.target.value)}
                required
              />
            </label>
          ) : null}
          <label>
            {translate('tourGroups.search.locationLabel')}
            <input
              value={location}
              onChange={event => {
                const nextLocation = event.target.value
                setLocation(nextLocation)
                void loadLocationSuggestions(nextLocation)
              }}
              required
            />
          </label>
        </div>
        {locationSuggestions.length > 0 ? (
          <ul className="grid gap-3 grid gap-2">
            {locationSuggestions.map(suggestion => (
              <li key={`${suggestion.resourceType}:${suggestion.value}`}>
                <button
                  type="button"
                  className="inline-flex items-center justify-center text-sm font-bold text-sky-600 underline-offset-4 hover:underline"
                  onClick={() => {
                    setLocation(suggestion.value)
                    setLocationSuggestions([])
                  }}
                >
                  <strong>{suggestion.title}</strong>
                </button>
                <p>{suggestion.subtitle}</p>
              </li>
            ))}
          </ul>
        ) : null}
        <div className="flex flex-wrap items-center gap-3">
          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
            {translate('tourGroups.searchOptions')}
          </button>
        </div>
        {searchMessage ? <p className="text-sm leading-6 text-slate-500">{searchMessage}</p> : null}
      </form>

      {itemType === 'Flight' && flightResults.length > 0 ? (
        <ul className="grid gap-3">
          {flightResults.map(flight => (
            <li key={flight.flightId}>
              <div>
                <strong>{`${flight.airlineCode} ${flight.flightNumber}`}</strong>
                <p>{`${flight.departureAirport} -> ${flight.arrivalAirport}`}</p>
                <p>{formatIsoDateTime(flight.departureTime, '-')}</p>
                <div className="grid gap-2">
                  {flight.cabinInventories.map(cabin => (
                    <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      key={cabin.inventoryId}
                      type="button"
                      disabled={isBusy}
                      onClick={() =>
                        void createPlanWithOption(
                          {
                            itemType: 'Flight',
                            title: `${flight.departureAirport} -> ${flight.arrivalAirport}`,
                            description: `${flight.airlineName} ${flight.flightNumber}`,
                            scheduledAt: toInstantString(flight.departureTime),
                            endsAt: toInstantString(flight.arrivalTime),
                          },
                          {
                            resourceType: 'Flight',
                            resourceId: flight.flightId,
                            resourceVariantCode: cabin.cabinClass,
                            label: `${flight.flightNumber} ${localizeCabinClass(cabin.cabinClass, currentLanguage)}`,
                            description: `${flight.departureAirport} -> ${flight.arrivalAirport} / ${cabin.unitPrice} ${cabin.currency}`,
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
              <div>
                <strong>{hotel.hotelName}</strong>
                <p>{hotel.location}</p>
                <div className="grid gap-2">
                  {hotel.roomTypes.map(roomType => (
                    <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
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
            const route = parseRouteInput(location)
            const fromStop = route ? resolveTrainStop(train, route.from) : null
            const toStop = route ? resolveTrainStop(train, route.to) : null
            return (
              <li key={train.trainId}>
                <div>
                  <strong>{train.trainNumber}</strong>
                  <p>{train.stops.map(stop => stop.stationCode).join(' -> ')}</p>
                  <div className="grid gap-2">
                    {route && fromStop && toStop
                      ? train.seatInventories.map(seat => (
                          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                            key={seat.inventoryId}
                            type="button"
                            disabled={isBusy}
                            onClick={() =>
                              void createPlanWithOption(
                                {
                                  itemType: 'Train',
                                  title: `${fromStop.stationName} -> ${toStop.stationName}`,
                                  description: train.trainNumber,
                                  scheduledAt: atUtc(date, '09:00'),
                                },
                                {
                                  resourceType: 'TrainJourneySeat',
                                  resourceId: train.trainId,
                                  resourceVariantCode: seat.seatClass,
                                  resourceContext: `${fromStop.stationCode}|${toStop.stationCode}`,
                                  label: `${train.trainNumber} ${localizeTrainSeatClass(seat.seatClass, currentLanguage)}`,
                                  description: `${fromStop.stationName} -> ${toStop.stationName}`,
                                  defaultQuantity: 1,
                                },
                              )
                            }
                          >
                            {localizeTrainSeatClass(seat.seatClass, currentLanguage)}
                          </button>
                        ))
                      : (
                          <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.routeInputHint')}</p>
                        )}
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
              <div>
                <strong>{attraction.attractionName}</strong>
                <p>{`${attraction.city} / ${attraction.location}`}</p>
                <div className="grid gap-2">
                  {attraction.ticketTypes.map(ticketType => (
                    <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
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

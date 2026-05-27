import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, AttractionResponse, FlightPlannerResponse, GroupPlanItemResponse, HotelPlannerResponse, TrainResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeCabinClass, localizeTourGroupItemType, localizeTrainSeatClass, mapBackendStatusToProductLabel } from '@/lib/presenters/view-models'

type TourGroupOptionPickerProps = {
  currentLanguage: AppLanguage
  isOpen: boolean
  displayMode?: 'modal' | 'inline'
  isBusy: boolean
  planItem: GroupPlanItemResponse | null
  translate: (translationKey: string) => string
  onClose: () => void
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightPlannerResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelPlannerResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onCreateOption: (payload: {
    resourceType: string
    resourceId: string
    resourceVariantCode?: string | null
    resourceContext?: string | null
    label: string
    description: string
    defaultQuantity: number
  }) => Promise<void>
}

function quoteTrainSegmentAmount(
  train: TrainResponse,
  fromStationCode: string,
  toStationCode: string,
  seatClass: string,
): { amount: string; currency: string } | null {
  const normalizedFrom = fromStationCode.trim().toUpperCase()
  const normalizedTo = toStationCode.trim().toUpperCase()
  const normalizedSeatClass = seatClass.trim().toLowerCase()
  const fromIndex = train.stops.findIndex(stop => stop.stationCode.toUpperCase() === normalizedFrom)
  const toIndex = train.stops.findIndex(stop => stop.stationCode.toUpperCase() === normalizedTo)

  if (fromIndex < 0 || toIndex < 0 || fromIndex >= toIndex) {
    return null
  }

  const pathStops = train.stops.slice(fromIndex, toIndex + 1)
  const segmentPrices = pathStops.slice(0, -1).map((currentStop, index) =>
    train.segmentPrices.find(
      segmentPrice =>
        segmentPrice.fromStationCode.toUpperCase() === currentStop.stationCode.toUpperCase() &&
        segmentPrice.toStationCode.toUpperCase() === pathStops[index + 1].stationCode.toUpperCase() &&
        segmentPrice.seatClass.trim().toLowerCase() === normalizedSeatClass,
    ),
  )

  if (segmentPrices.some(price => !price)) {
    return null
  }

  const prices = segmentPrices.flatMap(price => (price ? [price] : []))
  const currency = prices[0]?.currency
  if (!currency || prices.some(price => price.currency !== currency)) {
    return null
  }

  return {
    amount: prices.reduce((sum, price) => sum + Number(price.amount), 0).toString(),
    currency,
  }
}

export function TourGroupOptionPicker({
  currentLanguage,
  isOpen,
  displayMode = 'modal',
  isBusy,
  planItem,
  translate,
  onClose,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
  onCreateOption,
}: TourGroupOptionPickerProps) {
  const [flightResults, setFlightResults] = useState<FlightPlannerResponse[]>([])
  const [hotelResults, setHotelResults] = useState<HotelPlannerResponse[]>([])
  const [trainResults, setTrainResults] = useState<TrainResponse[]>([])
  const [attractionResults, setAttractionResults] = useState<AttractionResponse[]>([])
  const [flightFrom, setFlightFrom] = useState('')
  const [flightTo, setFlightTo] = useState('')
  const [flightDate, setFlightDate] = useState('')
  const [hotelLocation, setHotelLocation] = useState('')
  const [hotelCheckIn, setHotelCheckIn] = useState('')
  const [hotelCheckOut, setHotelCheckOut] = useState('')
  const [trainFrom, setTrainFrom] = useState('')
  const [trainTo, setTrainTo] = useState('')
  const [trainDate, setTrainDate] = useState('')
  const [attractionCity, setAttractionCity] = useState('')

  useEffect(() => {
    if (!planItem) {
      return
    }

    const scheduledDate = planItem.scheduledAt.slice(0, 10)
    setFlightDate(scheduledDate)
    setHotelCheckIn(scheduledDate)
    setHotelCheckOut(planItem.endsAt ? planItem.endsAt.slice(0, 10) : scheduledDate)
    setTrainDate(scheduledDate)
  }, [planItem])

  const title = useMemo(() => {
    if (!planItem) {
      return ''
    }
    return `${translate('tourGroups.createOption')} 路 ${localizeTourGroupItemType(planItem.itemType, currentLanguage)}`
  }, [currentLanguage, planItem, translate])

  if (!isOpen || !planItem) {
    return null
  }

  async function addOption(payload: {
    resourceType: string
    resourceId: string
    resourceVariantCode?: string | null
    resourceContext?: string | null
    label: string
    description: string
    defaultQuantity: number
  }) {
    await onCreateOption(payload)
    if (displayMode === 'modal') {
      onClose()
    }
  }

  const content = (
      <div
        className={displayMode === 'modal' ? 'grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20 max-w-5xl' : 'grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4'}
        role="dialog"
        aria-modal={displayMode === 'modal' ? 'true' : undefined}
        aria-label={title}
      >
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('tourGroups.optionPickerEyebrow')}</p>
            <h3>{title}</h3>
            {displayMode === 'inline' ? (
              <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.optionPickerInlineHint')}</p>
            ) : null}
          </div>
          {displayMode === 'modal' ? (
            <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onClose}>
              {translate('payment.close')}
            </button>
          ) : null}
        </div>

        {planItem.itemType === 'Flight' ? (
          <>
            <form
              className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4"
              onSubmit={async event => {
                event.preventDefault()
                setFlightResults(await onSearchFlights({ departureAirport: flightFrom || undefined, arrivalAirport: flightTo || undefined, date: flightDate || undefined }))
              }}
            >
              <div className="grid gap-4 md:grid-cols-3">
                <label>
                  {translate('tourGroups.search.departure')}
                  <input value={flightFrom} onChange={event => setFlightFrom(event.target.value)} />
                </label>
                <label>
                  {translate('tourGroups.search.arrival')}
                  <input value={flightTo} onChange={event => setFlightTo(event.target.value)} />
                </label>
                <label>
                  {translate('tourGroups.search.date')}
                  <input type="date" value={flightDate} onChange={event => setFlightDate(event.target.value)} />
                </label>
              </div>
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>{translate('tourGroups.searchOptions')}</button>
            </form>
            <ul className="grid gap-3">
              {flightResults.map(flight => (
                <li key={flight.flightId}>
                  <div>
                    <strong>{`${flight.airlineCode} ${flight.flightNumber}`}</strong>
                    <p>{`${flight.departureAirport} 鈫?${flight.arrivalAirport}`}</p>
                    <p>{`${formatIsoDateTime(flight.departureTime, '-')} 路 ${mapBackendStatusToProductLabel(flight.status, currentLanguage)}`}</p>
                    <div className="grid gap-2">
                      {flight.cabinInventories.map(cabin => (
                        <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                          key={cabin.inventoryId}
                          type="button"
                          disabled={isBusy}
                          onClick={() =>
                            void addOption({
                              resourceType: 'Flight',
                              resourceId: flight.flightId,
                              resourceVariantCode: cabin.cabinClass,
                              label: `${flight.flightNumber} 路 ${localizeCabinClass(cabin.cabinClass, currentLanguage)}`,
                              description: `${flight.departureAirport} 鈫?${flight.arrivalAirport} 路 ${formatIsoDateTime(flight.departureTime, '-')}`,
                              defaultQuantity: 1,
                            })
                          }
                        >
                          {`${localizeCabinClass(cabin.cabinClass, currentLanguage)} 路 ${cabin.unitPrice} ${cabin.currency}`}
                        </button>
                      ))}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        ) : null}

        {planItem.itemType === 'Hotel' ? (
          <>
            <form
              className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4"
              onSubmit={async event => {
                event.preventDefault()
                setHotelResults(await onSearchHotels({ location: hotelLocation || undefined, checkInDate: hotelCheckIn || undefined, checkOutDate: hotelCheckOut || undefined }))
              }}
            >
              <div className="grid gap-4 md:grid-cols-3">
                <label>
                  {translate('tourGroups.search.location')}
                  <input value={hotelLocation} onChange={event => setHotelLocation(event.target.value)} />
                </label>
                <label>
                  {translate('hotels.checkInDate')}
                  <input type="date" value={hotelCheckIn} onChange={event => setHotelCheckIn(event.target.value)} />
                </label>
                <label>
                  {translate('hotels.checkOutDate')}
                  <input type="date" value={hotelCheckOut} onChange={event => setHotelCheckOut(event.target.value)} />
                </label>
              </div>
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>{translate('tourGroups.searchOptions')}</button>
            </form>
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
                            void addOption({
                              resourceType: 'HotelRoomType',
                              resourceId: roomType.roomTypeId,
                              label: `${hotel.hotelName} 路 ${roomType.roomTypeName}`,
                              description: `${roomType.roomTypeName} 路 ${roomType.basePrice} ${roomType.currency}`,
                              defaultQuantity: 1,
                            })
                          }
                        >
                          {`${roomType.roomTypeName} 路 ${roomType.basePrice} ${roomType.currency}`}
                        </button>
                      ))}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        ) : null}

        {planItem.itemType === 'Train' ? (
          <>
            <form
              className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4"
              onSubmit={async event => {
                event.preventDefault()
                setTrainResults(await onSearchTrains({ fromStation: trainFrom || undefined, toStation: trainTo || undefined, date: trainDate || undefined }))
              }}
            >
              <div className="grid gap-4 md:grid-cols-3">
                <label>
                  {translate('tourGroups.search.fromStation')}
                  <input value={trainFrom} onChange={event => setTrainFrom(event.target.value)} />
                </label>
                <label>
                  {translate('tourGroups.search.toStation')}
                  <input value={trainTo} onChange={event => setTrainTo(event.target.value)} />
                </label>
                <label>
                  {translate('tourGroups.search.date')}
                  <input type="date" value={trainDate} onChange={event => setTrainDate(event.target.value)} />
                </label>
              </div>
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>{translate('tourGroups.searchOptions')}</button>
            </form>
            <ul className="grid gap-3">
              {trainResults.map(train => (
                <li key={train.trainId}>
                  <div>
                    <strong>{train.trainNumber}</strong>
                    <p>{train.stops.map(stop => stop.stationCode).join(' 鈫?')}</p>
                    <div className="grid gap-2">
                      {train.seatInventories.map(seat => {
                        const quote = quoteTrainSegmentAmount(train, trainFrom, trainTo, seat.seatClass)
                        return (
                          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                            key={seat.inventoryId}
                            type="button"
                            disabled={isBusy || !quote}
                            onClick={() =>
                              quote
                                ? void addOption({
                                    resourceType: 'TrainJourneySeat',
                                    resourceId: train.trainId,
                                    resourceVariantCode: seat.seatClass,
                                    resourceContext: `${trainFrom.toUpperCase()}|${trainTo.toUpperCase()}`,
                                    label: `${train.trainNumber} 路 ${localizeTrainSeatClass(seat.seatClass, currentLanguage)}`,
                                    description: `${trainFrom.toUpperCase()} 鈫?${trainTo.toUpperCase()} 路 ${quote.amount} ${quote.currency}`,
                                    defaultQuantity: 1,
                                  })
                                : undefined
                            }
                          >
                            {`${localizeTrainSeatClass(seat.seatClass, currentLanguage)} 路 ${quote ? `${quote.amount} ${quote.currency}` : translate('trains.routePriceUnavailable')}`}
                          </button>
                        )
                      })}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        ) : null}

        {planItem.itemType === 'Attraction' ? (
          <>
            <form
              className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4"
              onSubmit={async event => {
                event.preventDefault()
                setAttractionResults(await onSearchAttractions({ city: attractionCity || undefined }))
              }}
            >
              <div className="grid gap-4 md:grid-cols-3">
                <label>
                  {translate('tourGroups.search.city')}
                  <input value={attractionCity} onChange={event => setAttractionCity(event.target.value)} />
                </label>
              </div>
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>{translate('tourGroups.searchOptions')}</button>
            </form>
            <ul className="grid gap-3">
              {attractionResults.map(attraction => (
                <li key={attraction.attractionId}>
                  <div>
                    <strong>{attraction.attractionName}</strong>
                    <p>{`${attraction.city} 路 ${attraction.location}`}</p>
                    <div className="grid gap-2">
                      {attraction.ticketTypes.map(ticketType => (
                        <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                          key={ticketType.ticketTypeId}
                          type="button"
                          disabled={isBusy}
                          onClick={() =>
                            void addOption({
                              resourceType: 'AttractionTicketType',
                              resourceId: ticketType.ticketTypeId,
                              resourceContext: attraction.attractionId,
                              label: `${attraction.attractionName} 路 ${ticketType.ticketTypeName}`,
                              description: `${ticketType.ticketTypeName} 路 ${ticketType.priceAmount} ${ticketType.priceCurrency}`,
                              defaultQuantity: 1,
                            })
                          }
                        >
                          {`${ticketType.ticketTypeName} 路 ${ticketType.priceAmount} ${ticketType.priceCurrency}`}
                        </button>
                      ))}
                    </div>
                  </div>
                </li>
              ))}
            </ul>
          </>
        ) : null}
      </div>
  )

  if (displayMode === 'inline') {
    return content
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6" role="presentation">
      {content}
    </div>
  )
}

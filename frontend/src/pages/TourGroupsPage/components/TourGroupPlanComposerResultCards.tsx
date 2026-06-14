import type { AppLanguage, AttractionResponse, FlightPlannerResponse, HotelPlannerResponse, TrainPlannerResponse } from '@/lib/mvp-types/index'
import { formatIsoDateTime, localizeCabinClass, localizeTrainSeatClass } from '@/lib/presenters/view-models'

import { atUtc, nextDay, resolveTrainStop, toInstantString } from '@/pages/TourGroupsPage/components/TourGroupPlanComposer.utils'

type PlanCreatorPayloads = {
  onCreatePlanWithOption: (
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
  ) => void
}

export function TourGroupFlightResults({
  currentLanguage,
  isBusy,
  flightResults,
  onCreatePlanWithOption,
}: {
  currentLanguage: AppLanguage
  isBusy: boolean
  flightResults: FlightPlannerResponse[]
} & PlanCreatorPayloads) {
  if (flightResults.length === 0) return null
  return (
    <ul className="grid gap-3">
      {flightResults.map(flight => (
        <li key={flight.flightId}>
          <div className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4 shadow-sm shadow-sky-100/40">
            <strong>{`${flight.airlineCode} ${flight.flightNumber}`}</strong>
            <p>{`${flight.departureAirport} -> ${flight.arrivalAirport}`}</p>
            <p>{formatIsoDateTime(flight.departureTime, '-')}</p>
            <div className="grid gap-2">
              {flight.cabinInventories.map(cabin => (
                <button
                  className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                  key={cabin.inventoryId}
                  type="button"
                  disabled={isBusy}
                  onClick={() =>
                    void onCreatePlanWithOption(
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
  )
}

export function TourGroupHotelResults({
  isBusy,
  hotelResults,
  date,
  hotelCheckOutDate,
  onCreatePlanWithOption,
}: {
  isBusy: boolean
  hotelResults: HotelPlannerResponse[]
  date: string
  hotelCheckOutDate: string
} & PlanCreatorPayloads) {
  if (hotelResults.length === 0) return null
  return (
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
                    void onCreatePlanWithOption(
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
  )
}

export function TourGroupTrainResults({
  currentLanguage,
  isBusy,
  trainResults,
  departureLocation,
  arrivalLocation,
  date,
  onCreatePlanWithOption,
}: {
  currentLanguage: AppLanguage
  isBusy: boolean
  trainResults: TrainPlannerResponse[]
  departureLocation: string
  arrivalLocation: string
  date: string
} & PlanCreatorPayloads) {
  if (trainResults.length === 0) return null
  return (
    <ul className="grid gap-3">
      {trainResults.map(train => {
        const fromStop = resolveTrainStop(train, departureLocation)
        const toStop = resolveTrainStop(train, arrivalLocation)
        return (
          <li key={train.trainId}>
            <div className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4 shadow-sm shadow-sky-100/40">
              <strong>{train.trainNumber}</strong>
              <p>{train.stops.map(stop => stop.stationCode).join(' -> ')}</p>
              <div className="grid gap-2">
                {fromStop && toStop
                  ? train.seatInventories.map(seat => (
                      <button
                        className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                        key={seat.inventoryId}
                        type="button"
                        disabled={isBusy}
                        onClick={() =>
                          void onCreatePlanWithOption(
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
                  : <p className="text-sm leading-6 text-slate-500">请先填写出发地点和到达地点。</p>}
              </div>
            </div>
          </li>
        )
      })}
    </ul>
  )
}

export function TourGroupAttractionResults({
  isBusy,
  attractionResults,
  date,
  onCreatePlanWithOption,
}: {
  isBusy: boolean
  attractionResults: AttractionResponse[]
  date: string
} & PlanCreatorPayloads) {
  if (attractionResults.length === 0) return null
  return (
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
                    void onCreatePlanWithOption(
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
  )
}

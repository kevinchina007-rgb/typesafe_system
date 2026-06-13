import type { FormEvent } from 'react'

import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'
import type { FlightResultsSectionProps, DisplayFlight } from '../../objects'
import { formatFlightClock, getPriceToneClass, getPriceToneLabel, formatPrice } from './FlightResultsUtils'

export function FlightResultsCards({
  displayFlights,
  isBusy,
  selectedTravelerIds,
  getLateBookingNotice,
  onSubmit,
  translate,
}: {
  displayFlights: DisplayFlight[]
  isBusy: boolean
  selectedTravelerIds: string[]
  getLateBookingNotice: (flightResponse: FlightPlannerResponse) => string
  onSubmit: (event: FormEvent<HTMLFormElement>, displayFlight: DisplayFlight, travelerIds: string[]) => void
  translate: (translationKey: string) => string
}) {
  return (
    <div className="overflow-hidden bg-white">
      {displayFlights.length > 0 ? (
        displayFlights.map(displayFlight => (
          <FlightResultCard
            key={displayFlight.flight.flightId}
            displayFlight={displayFlight}
            isBusy={isBusy}
            selectedTravelerIds={selectedTravelerIds}
            getLateBookingNotice={getLateBookingNotice}
            onSubmit={event => void onSubmit(event, displayFlight, selectedTravelerIds)}
          />
        ))
      ) : (
        <p className="m-0 px-7 py-10 text-lg text-slate-500">{translate('flights.empty')}</p>
      )}
    </div>
  )
}

function FlightResultCard({
  displayFlight,
  isBusy,
  selectedTravelerIds,
  getLateBookingNotice,
  onSubmit,
}: {
  displayFlight: DisplayFlight
  isBusy: boolean
  selectedTravelerIds: string[]
  getLateBookingNotice: (flightResponse: FlightPlannerResponse) => string
  onSubmit: (event: FormEvent<HTMLFormElement>) => void
}) {
  const flight = displayFlight.flight

  return (
    <article className="grid gap-5 border-b border-slate-200 px-7 py-6 last:border-b-0 xl:grid-cols-[minmax(240px,1.1fr)_minmax(360px,1.2fr)_minmax(190px,0.8fr)_auto]">
      <div className="flex items-center gap-5">
        <div className="flex h-16 w-16 items-center justify-center border-2 border-slate-200 bg-slate-950 text-sm font-medium text-white">
          {displayFlight.airlineLogoPath ? <img src={displayFlight.airlineLogoPath} alt="" className="h-full w-full object-cover" /> : flight.airlineCode}
        </div>
        <div className="grid gap-1">
          <strong className="text-2xl font-bold text-slate-950">{displayFlight.airlineName}</strong>
          <div className="flex flex-wrap gap-3 text-base font-medium text-sky-600">
            <span>{flight.flightNumber}</span>
            <span>{flight.aircraftModel}</span>
            <span>{displayFlight.displayCabinLabel}</span>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-5">
        <TimeBlock time={formatFlightClock(flight.departureTime)} airport={displayFlight.departureAirportName} />
        <div className="h-px min-w-24 bg-slate-200" />
        <TimeBlock time={formatFlightClock(flight.arrivalTime)} airport={displayFlight.arrivalAirportName} />
      </div>

      <div className="grid content-center justify-items-end gap-1">
        <strong className={`text-4xl font-bold ${getPriceToneClass(displayFlight.priceTone)}`}>
          {formatPrice(displayFlight.displayPrice)}
        </strong>
        <span className="text-base text-slate-500">{getPriceToneLabel(displayFlight.priceTone)}</span>
      </div>

      <form className="grid content-center justify-items-end gap-3" onSubmit={onSubmit}>
        <button
          type="submit"
          className="inline-flex min-h-14 min-w-28 items-center justify-center bg-pink-500 px-6 py-3 text-lg font-bold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:bg-slate-300"
          disabled={isBusy || !displayFlight.isDisplayCabinBookable || selectedTravelerIds.length === 0}
        >
          订票
        </button>
        {flight.bookingWindowStatus === 'SurchargeRequired' ? (
          <p className="m-0 max-w-52 text-right text-sm text-amber-700">{getLateBookingNotice(flight)}</p>
        ) : null}
      </form>
    </article>
  )
}

export function FlightTravelerSelectionPanel({
  travelers,
  selectedTravelerIds,
  onToggleTravelerSelection,
}: {
  travelers: FlightResultsSectionProps['travelers']
  selectedTravelerIds: string[]
  onToggleTravelerSelection: (travelerId: string) => void
}) {
  if (travelers.length === 0) {
    return null
  }

  return (
    <section className="grid gap-4 border border-slate-200 bg-white px-6 py-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="m-0 text-2xl font-bold text-slate-950">选择出行人</h3>
        <p className="m-0 text-sm font-medium text-slate-500">这里勾选的出行人会直接带到支付页</p>
      </div>
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {travelers.map(traveler => {
          const isSelected = selectedTravelerIds.includes(traveler.travelerId)
          return (
            <label
              key={traveler.travelerId}
              className={`flex cursor-pointer items-center gap-4 border px-4 py-4 text-base font-semibold transition ${
                isSelected ? 'border-sky-500 bg-sky-50 text-slate-950' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-400'
              }`}
            >
              <input type="checkbox" checked={isSelected} onChange={() => onToggleTravelerSelection(traveler.travelerId)} />
              <span className="inline-flex h-11 w-11 items-center justify-center border border-slate-300 bg-slate-50 text-lg font-black text-slate-700">
                {traveler.fullName.slice(0, 1)}
              </span>
              <span className="truncate">{`${traveler.fullName} (${traveler.documentNumber.slice(-4)})`}</span>
            </label>
          )
        })}
      </div>
      {selectedTravelerIds.length === 0 ? <p className="m-0 text-sm font-medium text-rose-600">请至少选择一位出行人</p> : null}
    </section>
  )
}

function TimeBlock({ time, airport }: { time: string; airport: string }) {
  return (
    <div className="grid gap-1">
      <strong className="text-4xl font-bold leading-none text-slate-950">{time}</strong>
      <span className="text-base text-slate-600">{airport}</span>
    </div>
  )
}

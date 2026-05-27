import type { FlightPlannerResponse } from '@/lib/mvp-types/flights'

type FlightBookingWindowDialogProps = {
  flight: FlightPlannerResponse | null
  translate: (translationKey: string) => string
  onClose: () => void
}

export function FlightBookingWindowDialog({ flight, translate, onClose }: FlightBookingWindowDialogProps) {
  if (!flight) {
    return null
  }

  const localizedNotice = flight.lateBookingSurchargeAmount
    ? translate('flights.surchargeNoticeWithAmount')
        .replace('{amount}', flight.lateBookingSurchargeAmount)
        .replace('{currency}', flight.lateBookingSurchargeCurrency || flight.currency)
    : translate('flights.surchargeDialogDescription')

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6" role="presentation" onClick={onClose}>
      <div
        className="grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20 flight-booking-window-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={translate('flights.surchargeDialogTitle')}
        onClick={event => event.stopPropagation()}
      >
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('flights.surchargeDialogEyebrow')}</p>
            <h3>{translate('flights.surchargeDialogTitle')}</h3>
          </div>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onClose}>
            ×
          </button>
        </div>

        <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{localizedNotice}</p>

        {flight.lateBookingSurchargeAmount ? (
          <div className="flight-booking-window-price">
            <span>{translate('flights.surchargeAmount')}</span>
            <strong>{`${flight.lateBookingSurchargeAmount} ${flight.lateBookingSurchargeCurrency || flight.currency}`}</strong>
          </div>
        ) : null}

        <div className="flex flex-wrap items-center gap-3">
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>
      </div>
    </div>
  )
}



import type { FlightResponse } from '@/lib/mvp-types/flights'

type FlightBookingWindowDialogProps = {
  flight: FlightResponse | null
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
    <div className="modal-backdrop" role="presentation" onClick={onClose}>
      <div
        className="modal-card flight-booking-window-dialog"
        role="dialog"
        aria-modal="true"
        aria-label={translate('flights.surchargeDialogTitle')}
        onClick={event => event.stopPropagation()}
      >
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('flights.surchargeDialogEyebrow')}</p>
            <h3>{translate('flights.surchargeDialogTitle')}</h3>
          </div>
          <button type="button" className="secondary-button modal-close-button" onClick={onClose}>
            ×
          </button>
        </div>

        <p className="hero-copy">{localizedNotice}</p>

        {flight.lateBookingSurchargeAmount ? (
          <div className="flight-booking-window-price">
            <span>{translate('flights.surchargeAmount')}</span>
            <strong>{`${flight.lateBookingSurchargeAmount} ${flight.lateBookingSurchargeCurrency || flight.currency}`}</strong>
          </div>
        ) : null}

        <div className="action-row">
          <button type="button" className="secondary-button" onClick={onClose}>
            {translate('tourGroups.cancel')}
          </button>
        </div>
      </div>
    </div>
  )
}



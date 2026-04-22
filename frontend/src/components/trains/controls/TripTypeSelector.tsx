import type { TrainTripType } from '../trainBookingModel'

type TripTypeSelectorProps = {
  value: TrainTripType
  translate: (translationKey: string) => string
  onChange: (value: TrainTripType) => void
}

const tripTypeOptions: TrainTripType[] = ['oneWay', 'roundTrip']

export function TripTypeSelector({ value, translate, onChange }: TripTypeSelectorProps) {
  return (
    <div className="resource-trip-type-selector" role="tablist" aria-label={translate('trains.tripType')}>
      {tripTypeOptions.map(option => (
        <button
          key={option}
          type="button"
          className={`resource-chip-button ${value === option ? 'is-active' : ''}`}
          onClick={() => onChange(option)}
        >
          {translate(`trains.tripType.${option}`)}
        </button>
      ))}
    </div>
  )
}

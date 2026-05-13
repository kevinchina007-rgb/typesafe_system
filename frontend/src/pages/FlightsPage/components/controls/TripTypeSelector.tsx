import type { TripType } from '@/app/stores/models/flights'

type TripTypeSelectorProps = {
  value: TripType
  translate: (translationKey: string) => string
  onChange: (value: TripType) => void
}

const tripTypeOptions: TripType[] = ['oneWay', 'roundTrip', 'multiCity']

export function TripTypeSelector({ value, translate, onChange }: TripTypeSelectorProps) {
  return (
    <div className="flight-trip-type-selector" role="tablist" aria-label={translate('flights.tripType')}>
      {tripTypeOptions.map(option => (
        <button
          key={option}
          type="button"
          className={`flight-chip-button ${value === option ? 'is-active' : ''}`}
          onClick={() => onChange(option)}
        >
          {translate(`flights.tripType.${option}`)}
        </button>
      ))}
    </div>
  )
}

export type { TripType }

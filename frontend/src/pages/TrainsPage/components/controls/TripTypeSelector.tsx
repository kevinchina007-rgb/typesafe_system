import type { TrainTripType } from '@/app/stores/models/train-booking-model'

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
          className={`inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 ${value === option ? 'border-black bg-black text-white' : ''}`}
          onClick={() => onChange(option)}
        >
          {translate(`trains.tripType.${option}`)}
        </button>
      ))}
    </div>
  )
}

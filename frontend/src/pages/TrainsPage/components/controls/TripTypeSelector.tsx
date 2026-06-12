import type { TrainTripType } from '@/app/stores/models/train-booking-model'

// 火车行程类型切换控件参数，只负责单程和往返两个按钮。
type TripTypeSelectorProps = {
  value: TrainTripType
  translate: (translationKey: string) => string
  onChange: (value: TrainTripType) => void
}

// 行程类型候选项，只负责按钮顺序。
const tripTypeOptions: TrainTripType[] = ['oneWay', 'roundTrip']

// 火车行程类型切换器，只负责把当前值渲染成按钮组。
export function TripTypeSelector({ value, translate, onChange }: TripTypeSelectorProps) {
  return (
    <div className="flex flex-wrap items-center gap-3" role="tablist" aria-label={translate('trains.tripType')}>
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

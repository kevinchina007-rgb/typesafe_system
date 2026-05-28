import type { TrainSortMode } from '@/app/stores/models/train-booking-model'

type TrainFilterBarProps = {
  currentSortMode: TrainSortMode
  translate: (translationKey: string) => string
  onSortModeChange: (sortMode: TrainSortMode) => void
}

const sortModes: Array<{ mode: TrainSortMode; labelKey: string }> = [
  { mode: 'highSpeedPriority', labelKey: 'trains.sort.highSpeedPriority' },
  { mode: 'lowPricePriority', labelKey: 'trains.sort.lowPricePriority' },
  { mode: 'departureTimeEarly', labelKey: 'trains.sort.departureTimeEarly' },
]

export function TrainFilterBar({ currentSortMode, translate, onSortModeChange }: TrainFilterBarProps) {
  return (
    <section className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <p className="text-sm font-bold text-slate-500">{translate('trains.sortTitle')}</p>
      <div className="flex flex-wrap items-center gap-3">
        {sortModes.map(sortMode => {
          const isActive = sortMode.mode === currentSortMode
          return (
            <button
              key={sortMode.mode}
              type="button"
              aria-pressed={isActive}
              className={[
                'inline-flex min-h-10 items-center justify-center border px-4 py-2 text-sm font-semibold shadow-none transition disabled:cursor-not-allowed disabled:opacity-55',
                isActive
                  ? 'border-sky-500 bg-sky-500 text-white'
                  : 'border-slate-300 bg-white text-slate-950 hover:border-sky-500 hover:bg-sky-50 hover:text-sky-700',
              ].join(' ')}
              onClick={() => onSortModeChange(sortMode.mode)}
            >
              {translate(sortMode.labelKey)}
            </button>
          )
        })}
      </div>
    </section>
  )
}

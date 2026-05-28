import { trainFilterOptions } from '@/app/stores/models/train-booking-model'

type TrainFilterBarProps = {
  translate: (translationKey: string) => string
}

export function TrainFilterBar({ translate }: TrainFilterBarProps) {
  return (
    <section className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <p className="text-sm font-bold text-slate-500">{translate('trains.filterTitle')}</p>
      <div className="flex flex-wrap items-center gap-3">
        {trainFilterOptions.map(filterKey => (
          <button
            key={filterKey}
            type="button"
            className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-sky-500 hover:bg-sky-500 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          >
            {translate(`trains.filter.${filterKey}`)}
          </button>
        ))}
      </div>
    </section>
  )
}

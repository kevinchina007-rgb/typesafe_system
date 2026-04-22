import { trainFilterOptions } from '../trainBookingModel'

type TrainFilterBarProps = {
  translate: (translationKey: string) => string
}

export function TrainFilterBar({ translate }: TrainFilterBarProps) {
  return (
    <section className="resource-filter-bar panel-card">
      <p className="eyebrow-label">{translate('trains.filterTitle')}</p>
      <div className="resource-filter-chip-row">
        {trainFilterOptions.map(filterKey => (
          <button key={filterKey} type="button" className="resource-filter-chip">
            {translate(`trains.filter.${filterKey}`)}
          </button>
        ))}
      </div>
    </section>
  )
}

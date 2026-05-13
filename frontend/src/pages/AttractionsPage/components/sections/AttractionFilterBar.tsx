import { attractionFilterOptions } from '@/app/stores/models/attraction-booking-model'

type AttractionFilterBarProps = {
  translate: (translationKey: string) => string
}

export function AttractionFilterBar({ translate }: AttractionFilterBarProps) {
  return (
    <section className="resource-filter-bar panel-card">
      <p className="eyebrow-label">{translate('attractions.filterTitle')}</p>
      <div className="resource-filter-chip-row">
        {attractionFilterOptions.map(filterKey => (
          <button key={filterKey} type="button" className="resource-filter-chip">
            {translate(`attractions.filter.${filterKey}`)}
          </button>
        ))}
      </div>
    </section>
  )
}

import { hotelFilterOptions } from '../hotelBookingModel'

type HotelFilterBarProps = {
  translate: (translationKey: string) => string
}

export function HotelFilterBar({ translate }: HotelFilterBarProps) {
  return (
    <section className="resource-filter-bar panel-card">
      <p className="eyebrow-label">{translate('hotels.filterTitle')}</p>
      <div className="resource-filter-chip-row">
        {hotelFilterOptions.map(filterKey => (
          <button key={filterKey} type="button" className="resource-filter-chip">
            {translate(`hotels.filter.${filterKey}`)}
          </button>
        ))}
      </div>
    </section>
  )
}

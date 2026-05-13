type PassengerSelectorProps = {
  adults: number
  childrenCount: number
  translate: (translationKey: string) => string
  onAdultsChange: (value: number) => void
  onChildrenChange: (value: number) => void
}

export function PassengerSelector({
  adults,
  childrenCount,
  translate,
  onAdultsChange,
  onChildrenChange,
}: PassengerSelectorProps) {
  return (
    <div className="flight-inline-field flight-passenger-selector">
      <label className="flight-search-label">
        <span>{translate('flights.passengers')}</span>
        <div className="flight-inline-field-grid">
          <select value={adults} onChange={event => onAdultsChange(Number(event.target.value))}>
            {[1, 2, 3, 4, 5, 6].map(value => (
              <option key={value} value={value}>
                {translate('flights.adultsCount').replace('{count}', String(value))}
              </option>
            ))}
          </select>
          <select value={childrenCount} onChange={event => onChildrenChange(Number(event.target.value))}>
            {[0, 1, 2, 3, 4].map(value => (
              <option key={value} value={value}>
                {translate('flights.childrenCount').replace('{count}', String(value))}
              </option>
            ))}
          </select>
        </div>
      </label>
    </div>
  )
}

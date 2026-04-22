type PassengerSelectorProps = {
  passengerCount: number
  translate: (translationKey: string) => string
  onChange: (value: number) => void
}

export function PassengerSelector({ passengerCount, translate, onChange }: PassengerSelectorProps) {
  return (
    <label className="resource-search-label resource-inline-field">
      <span>{translate('trains.passengers')}</span>
      <select value={passengerCount} onChange={event => onChange(Number(event.target.value))}>
        {[1, 2, 3, 4, 5, 6].map(value => (
          <option key={value} value={value}>
            {translate('trains.passengerCount').replace('{count}', String(value))}
          </option>
        ))}
      </select>
    </label>
  )
}

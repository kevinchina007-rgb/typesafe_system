type CabinSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

const cabinOptions = ['Economy', 'PremiumEconomy', 'Business', 'First']

export function CabinSelector({ value, translate, onChange }: CabinSelectorProps) {
  return (
    <label className="flight-search-label flight-inline-field">
      <span>{translate('flights.cabinPreference')}</span>
      <select value={value} onChange={event => onChange(event.target.value)}>
        {cabinOptions.map(option => (
          <option key={option} value={option}>
            {translate(`flights.cabinOption.${option}`)}
          </option>
        ))}
      </select>
    </label>
  )
}

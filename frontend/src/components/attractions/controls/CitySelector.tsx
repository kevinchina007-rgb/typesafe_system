type CitySelectorProps = {
  value: string
  translate: (translationKey: string) => string
  suggestions: string[]
  onChange: (value: string) => void
}

export function CitySelector({ value, translate, suggestions, onChange }: CitySelectorProps) {
  return (
    <label className="resource-search-label">
      <span>{translate('attractions.city')}</span>
      <input list="attraction-city-suggestions" value={value} onChange={event => onChange(event.target.value)} />
      <datalist id="attraction-city-suggestions">
        {suggestions.map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </label>
  )
}

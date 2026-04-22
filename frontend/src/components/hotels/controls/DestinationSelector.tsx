type DestinationSelectorProps = {
  value: string
  placeholder: string
  translate: (translationKey: string) => string
  suggestions: string[]
  onChange: (value: string) => void
}

export function DestinationSelector({ value, placeholder, translate, suggestions, onChange }: DestinationSelectorProps) {
  return (
    <label className="resource-search-label">
      <span>{translate('hotels.location')}</span>
      <input list="hotel-destination-suggestions" value={value} placeholder={placeholder} onChange={event => onChange(event.target.value)} />
      <datalist id="hotel-destination-suggestions">
        {suggestions.map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </label>
  )
}

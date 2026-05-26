type DestinationSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  suggestions: string[]
  onChange: (value: string) => void
}

export function DestinationSelector({ value, translate, suggestions, onChange }: DestinationSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('hotels.location')}</span>
      <input list="hotel-destination-suggestions" value={value} onChange={event => onChange(event.target.value)} />
      <datalist id="hotel-destination-suggestions">
        {suggestions.map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </label>
  )
}

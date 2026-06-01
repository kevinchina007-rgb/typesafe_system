import type { CitySelectorProps } from '../../objects'

export function CitySelector({ value, translate, suggestions, onChange }: CitySelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.city')}</span>
      <input list="attraction-city-suggestions" value={value} placeholder={translate('attractions.cityPlaceholder')} onChange={event => onChange(event.target.value)} />
      <datalist id="attraction-city-suggestions">
        {suggestions.map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </label>
  )
}

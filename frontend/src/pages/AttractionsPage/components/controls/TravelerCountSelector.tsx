import type { TravelerCountSelectorProps } from '../../objects'

export function TravelerCountSelector({ value, translate, onChange }: TravelerCountSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.travelerCount')}</span>
      <select value={value} onChange={event => onChange(Number(event.target.value))}>
        {[1, 2, 3, 4, 5, 6].map(option => (
          <option key={option} value={option}>
            {translate('attractions.travelerCountValue').replace('{count}', String(option))}
          </option>
        ))}
      </select>
    </label>
  )
}


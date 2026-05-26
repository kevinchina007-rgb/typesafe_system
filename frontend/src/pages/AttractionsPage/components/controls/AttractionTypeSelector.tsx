import type { AttractionTypePreference } from '@/app/stores/models/attraction-booking-model'
﻿import { attractionTypeOptions } from '@/app/stores/models/attraction-booking-model'

type AttractionTypeSelectorProps = {
  value: AttractionTypePreference
  translate: (translationKey: string) => string
  onChange: (value: AttractionTypePreference) => void
}

export function AttractionTypeSelector({ value, translate, onChange }: AttractionTypeSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600 grid gap-2">
      <span>{translate('attractions.attractionType')}</span>
      <select value={value} onChange={event => onChange(event.target.value as AttractionTypePreference)}>
        {attractionTypeOptions.map(option => (
          <option key={option} value={option}>
            {translate(`attractions.attractionType.${option}`)}
          </option>
        ))}
      </select>
    </label>
  )
}

type DateSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

export function DateSelector({ value, translate, onChange }: DateSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.useDate')}</span>
      <input type="date" value={value} onChange={event => onChange(event.target.value)} />
    </label>
  )
}

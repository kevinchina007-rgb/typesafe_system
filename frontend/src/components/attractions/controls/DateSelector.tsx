type DateSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

export function DateSelector({ value, translate, onChange }: DateSelectorProps) {
  return (
    <label className="resource-search-label">
      <span>{translate('attractions.useDate')}</span>
      <input type="date" value={value} onChange={event => onChange(event.target.value)} />
    </label>
  )
}

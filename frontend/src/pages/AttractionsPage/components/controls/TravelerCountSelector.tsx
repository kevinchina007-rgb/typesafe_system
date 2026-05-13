type TravelerCountSelectorProps = {
  value: number
  translate: (translationKey: string) => string
  onChange: (value: number) => void
}

export function TravelerCountSelector({ value, translate, onChange }: TravelerCountSelectorProps) {
  return (
    <label className="resource-search-label resource-inline-field">
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

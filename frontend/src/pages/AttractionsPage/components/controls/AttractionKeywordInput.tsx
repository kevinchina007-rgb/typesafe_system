type AttractionKeywordInputProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

export function AttractionKeywordInput({ value, translate, onChange }: AttractionKeywordInputProps) {
  return (
    <label className="grid gap-2 text-sm font-medium text-slate-600">
      <span>{translate('attractions.keyword')}</span>
      <input value={value} onChange={event => onChange(event.target.value)} />
    </label>
  )
}

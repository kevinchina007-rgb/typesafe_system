type AttractionKeywordInputProps = {
  value: string
  translate: (translationKey: string) => string
  onChange: (value: string) => void
}

export function AttractionKeywordInput({ value, translate, onChange }: AttractionKeywordInputProps) {
  return (
    <label className="resource-search-label">
      <span>{translate('attractions.keyword')}</span>
      <input value={value} placeholder={translate('attractions.keywordPlaceholder')} onChange={event => onChange(event.target.value)} />
    </label>
  )
}

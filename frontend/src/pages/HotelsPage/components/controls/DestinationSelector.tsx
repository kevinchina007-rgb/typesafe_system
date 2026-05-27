type DestinationSelectorProps = {
  value: string
  translate: (translationKey: string) => string
  suggestions: string[]
  onChange: (value: string) => void
}

export function DestinationSelector({ value, translate, suggestions, onChange }: DestinationSelectorProps) {
  return (
    <label className="grid gap-2 text-sm font-black text-slate-700">
      <span className="text-base uppercase tracking-[0.12em] text-sky-600">{translate('hotels.location')}</span>
      <input
        list="hotel-destination-suggestions"
        value={value}
        onChange={event => onChange(event.target.value)}
        className="min-h-14 border-2 border-sky-200 bg-sky-50/70 px-5 text-xl font-semibold text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-sky-500 focus:bg-white"
      />
      <datalist id="hotel-destination-suggestions">
        {suggestions.map(item => (
          <option key={item} value={item} />
        ))}
      </datalist>
    </label>
  )
}

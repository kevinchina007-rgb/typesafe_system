export function SelectionCard({
  name,
  value,
  checked,
  label,
  marker,
  onChange,
}: {
  name: string
  value: string
  checked: boolean
  label: string
  marker: string
  onChange: () => void
}) {
  return (
    <label
      className={`flex cursor-pointer items-center gap-4 border px-4 py-4 text-base font-semibold transition ${
        checked ? 'border-sky-500 bg-sky-50 text-slate-950' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-400'
      }`}
    >
      <input type="radio" name={name} value={value} checked={checked} onChange={onChange} />
      <span className="inline-flex h-11 w-11 items-center justify-center border border-slate-300 bg-slate-50 text-lg font-black text-slate-700">{marker}</span>
      <span className="truncate">{label}</span>
    </label>
  )
}

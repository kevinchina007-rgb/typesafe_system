export function OrderMeta({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid gap-1 border border-slate-200 bg-white p-3">
      <span className="text-sm font-semibold text-slate-500">{label}</span>
      <strong className="break-words text-lg font-black text-slate-950">{value}</strong>
    </div>
  )
}

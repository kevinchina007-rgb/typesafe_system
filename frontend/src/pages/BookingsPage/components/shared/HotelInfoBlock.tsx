// 酒店订单里的信息块，只负责展示一个字段和值。
export function HotelInfoBlock({ label, value }: { label: string; value: string }) {
  return (
    <div className="grid gap-1 border border-cyan-100 bg-cyan-50/40 p-3">
      <span className="text-sm font-semibold text-cyan-700">{label}</span>
      <strong className="break-words text-lg font-black text-slate-950">{value}</strong>
    </div>
  )
}

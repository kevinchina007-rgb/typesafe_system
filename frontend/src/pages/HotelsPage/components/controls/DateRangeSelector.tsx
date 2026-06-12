// 入住和离店日期选择器参数。
type DateRangeSelectorProps = {
  checkInDate: string
  checkOutDate: string
  translate: (translationKey: string) => string
  onCheckInDateChange: (value: string) => void
  onCheckOutDateChange: (value: string) => void
}

// 日期范围选择器，负责入住和离店日期。
export function DateRangeSelector({
  checkInDate,
  checkOutDate,
  translate,
  onCheckInDateChange,
  onCheckOutDateChange,
}: DateRangeSelectorProps) {
  return (
    <div className="grid gap-4 sm:grid-cols-2">
      <label className="grid gap-2 text-sm font-black text-slate-700">
        <span className="text-base uppercase tracking-[0.12em] text-emerald-600">{translate('hotels.checkInDate')}</span>
        <input
          type="date"
          value={checkInDate}
          onChange={event => onCheckInDateChange(event.target.value)}
          className="min-h-14 border-2 border-emerald-200 bg-emerald-50/70 px-5 text-xl font-semibold text-slate-950 outline-none transition focus:border-emerald-500 focus:bg-white"
        />
      </label>
      <label className="grid gap-2 text-sm font-black text-slate-700">
        <span className="text-base uppercase tracking-[0.12em] text-violet-600">{translate('hotels.checkOutDate')}</span>
        <input
          type="date"
          value={checkOutDate}
          onChange={event => onCheckOutDateChange(event.target.value)}
          className="min-h-14 border-2 border-violet-200 bg-violet-50/70 px-5 text-xl font-semibold text-slate-950 outline-none transition focus:border-violet-500 focus:bg-white"
        />
      </label>
    </div>
  )
}

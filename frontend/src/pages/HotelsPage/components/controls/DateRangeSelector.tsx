type DateRangeSelectorProps = {
  checkInDate: string
  checkOutDate: string
  translate: (translationKey: string) => string
  onCheckInDateChange: (value: string) => void
  onCheckOutDateChange: (value: string) => void
}

export function DateRangeSelector({
  checkInDate,
  checkOutDate,
  translate,
  onCheckInDateChange,
  onCheckOutDateChange,
}: DateRangeSelectorProps) {
  return (
    <>
      <label className="grid gap-2 text-sm font-medium text-slate-600">
        <span>{translate('hotels.checkInDate')}</span>
        <input type="date" value={checkInDate} onChange={event => onCheckInDateChange(event.target.value)} />
      </label>
      <label className="grid gap-2 text-sm font-medium text-slate-600">
        <span>{translate('hotels.checkOutDate')}</span>
        <input type="date" value={checkOutDate} onChange={event => onCheckOutDateChange(event.target.value)} />
      </label>
    </>
  )
}

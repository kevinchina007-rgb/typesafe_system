type GuestRoomSelectorProps = {
  roomCount: number
  guestCount: number
  translate: (translationKey: string) => string
  onRoomCountChange: (value: number) => void
  onGuestCountChange: (value: number) => void
}

export function GuestRoomSelector({
  roomCount,
  guestCount,
  translate,
  onRoomCountChange,
  onGuestCountChange,
}: GuestRoomSelectorProps) {
  return (
    <div className="grid gap-3 md:grid-cols-2 md:grid-cols-2">
      <label className="grid gap-2 text-sm font-medium text-slate-600 grid gap-2">
        <span>{translate('hotels.roomCount')}</span>
        <select value={roomCount} onChange={event => onRoomCountChange(Number(event.target.value))}>
          {[1, 2, 3, 4].map(value => (
            <option key={value} value={value}>
              {translate('hotels.roomCountValue').replace('{count}', String(value))}
            </option>
          ))}
        </select>
      </label>
      <label className="grid gap-2 text-sm font-medium text-slate-600 grid gap-2">
        <span>{translate('hotels.guestCount')}</span>
        <select value={guestCount} onChange={event => onGuestCountChange(Number(event.target.value))}>
          {[1, 2, 3, 4, 5, 6].map(value => (
            <option key={value} value={value}>
              {translate('hotels.guestCountValue').replace('{count}', String(value))}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}

import type { HotelPreference } from '@/app/stores/models/hotel-booking-model'
import { hotelNearbyOptions, hotelPreferenceOptions } from '@/app/stores/models/hotel-booking-model'

type HotelPreferenceSelectorProps = {
  hotelPreference: HotelPreference
  nearbyPreference: string
  translate: (translationKey: string) => string
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
  disabled?: boolean
}

export function HotelPreferenceSelector({
  hotelPreference,
  nearbyPreference,
  translate,
  onHotelPreferenceChange,
  onNearbyPreferenceChange,
  disabled = false,
}: HotelPreferenceSelectorProps) {
  return (
    <div className="grid gap-4 md:grid-cols-2">
      <label className="grid gap-2 text-sm font-black text-slate-700">
        <span className="text-base uppercase tracking-[0.12em] text-amber-600">{translate('hotels.hotelPreference')}</span>
        <select
          value={hotelPreference}
          onChange={event => onHotelPreferenceChange(event.target.value as HotelPreference)}
          className="min-h-14 border-2 border-amber-200 bg-amber-50/70 px-4 text-lg font-semibold text-slate-950 outline-none transition focus:border-amber-500 focus:bg-white"
          disabled={disabled}
        >
          {hotelPreferenceOptions.map(option => (
            <option key={option} value={option}>
              {translate(`hotels.preference.${option}`)}
            </option>
          ))}
        </select>
      </label>
      <label className="grid gap-2 text-sm font-black text-slate-700">
        <span className="text-base uppercase tracking-[0.12em] text-cyan-600">{translate('hotels.nearbyPreference')}</span>
        <select
          value={nearbyPreference}
          onChange={event => onNearbyPreferenceChange(event.target.value)}
          className="min-h-14 border-2 border-cyan-200 bg-cyan-50/70 px-4 text-lg font-semibold text-slate-950 outline-none transition focus:border-cyan-500 focus:bg-white"
          disabled={disabled}
        >
          {hotelNearbyOptions.map(option => (
            <option key={option} value={option}>
              {translate(`hotels.nearby.${option}`)}
            </option>
          ))}
        </select>
      </label>
    </div>
  )
}

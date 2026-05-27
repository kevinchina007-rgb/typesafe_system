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
      <label className="grid gap-2 text-sm font-medium text-slate-600">
        <span>{translate('hotels.hotelPreference')}</span>
        <select
          value={hotelPreference}
          onChange={event => onHotelPreferenceChange(event.target.value as HotelPreference)}
          className="min-h-12 border-2 border-slate-300 bg-white px-4 text-base text-slate-950 outline-none transition focus:border-sky-500"
          disabled={disabled}
        >
          {hotelPreferenceOptions.map(option => (
            <option key={option} value={option}>
              {translate(`hotels.preference.${option}`)}
            </option>
          ))}
        </select>
      </label>
      <label className="grid gap-2 text-sm font-medium text-slate-600">
        <span>{translate('hotels.nearbyPreference')}</span>
        <select
          value={nearbyPreference}
          onChange={event => onNearbyPreferenceChange(event.target.value)}
          className="min-h-12 border-2 border-slate-300 bg-white px-4 text-base text-slate-950 outline-none transition focus:border-sky-500"
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

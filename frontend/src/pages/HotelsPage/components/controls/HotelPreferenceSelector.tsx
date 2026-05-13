import type { HotelPreference } from '@/app/stores/models/hotel-booking-model'
﻿import { hotelNearbyOptions, hotelPreferenceOptions } from '@/app/stores/models/hotel-booking-model'

type HotelPreferenceSelectorProps = {
  hotelPreference: HotelPreference
  nearbyPreference: string
  translate: (translationKey: string) => string
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
}

export function HotelPreferenceSelector({
  hotelPreference,
  nearbyPreference,
  translate,
  onHotelPreferenceChange,
  onNearbyPreferenceChange,
}: HotelPreferenceSelectorProps) {
  return (
    <div className="resource-inline-field-grid hotel-preference-grid">
      <label className="resource-search-label resource-inline-field">
        <span>{translate('hotels.hotelPreference')}</span>
        <select value={hotelPreference} onChange={event => onHotelPreferenceChange(event.target.value as HotelPreference)}>
          {hotelPreferenceOptions.map(option => (
            <option key={option} value={option}>
              {translate(`hotels.preference.${option}`)}
            </option>
          ))}
        </select>
      </label>
      <label className="resource-search-label resource-inline-field">
        <span>{translate('hotels.nearbyPreference')}</span>
        <select value={nearbyPreference} onChange={event => onNearbyPreferenceChange(event.target.value)}>
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

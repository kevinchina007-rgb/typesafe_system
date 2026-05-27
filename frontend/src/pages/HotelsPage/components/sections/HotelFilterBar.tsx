import { GuestRoomSelector } from '@/pages/HotelsPage/components/controls/GuestRoomSelector'
import { HotelPreferenceSelector } from '@/pages/HotelsPage/components/controls/HotelPreferenceSelector'
import type { HotelPreference } from '@/app/stores/models/hotel-booking-model'

type HotelFilterBarProps = {
  roomCount: number
  guestCount: number
  hotelPreference: HotelPreference
  nearbyPreference: string
  isBusy: boolean
  translate: (translationKey: string) => string
  onRoomCountChange: (value: number) => void
  onGuestCountChange: (value: number) => void
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
}

export function HotelFilterBar({
  roomCount,
  guestCount,
  hotelPreference,
  nearbyPreference,
  isBusy,
  translate,
  onRoomCountChange,
  onGuestCountChange,
  onHotelPreferenceChange,
  onNearbyPreferenceChange,
}: HotelFilterBarProps) {
  return (
    <section className="grid gap-5 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <p className="text-sm font-bold text-slate-500">{translate('hotels.filterTitle')}</p>
      </div>

      <div className="grid gap-4 xl:grid-cols-[1fr_1.2fr]">
        <GuestRoomSelector
          roomCount={roomCount}
          guestCount={guestCount}
          translate={translate}
          onRoomCountChange={onRoomCountChange}
          onGuestCountChange={onGuestCountChange}
        />
        <HotelPreferenceSelector
          hotelPreference={hotelPreference}
          nearbyPreference={nearbyPreference}
          translate={translate}
          onHotelPreferenceChange={onHotelPreferenceChange}
          onNearbyPreferenceChange={onNearbyPreferenceChange}
          disabled={isBusy}
        />
      </div>
    </section>
  )
}

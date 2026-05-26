import type { HotelPreference, HotelQuickDatePreset } from '@/app/stores/models/hotel-booking-model'
﻿import { DateRangeSelector } from '@/pages/HotelsPage/components/controls/DateRangeSelector'
import { DestinationSelector } from '@/pages/HotelsPage/components/controls/DestinationSelector'
import { GuestRoomSelector } from '@/pages/HotelsPage/components/controls/GuestRoomSelector'
import { HotelPreferenceSelector } from '@/pages/HotelsPage/components/controls/HotelPreferenceSelector'
import { HotDestinations } from '@/pages/HotelsPage/components/controls/HotDestinations'
import { applyHotelQuickDatePreset, countHotelStayNights } from '@/app/stores/models/hotel-booking-model'

const quickDatePresets: HotelQuickDatePreset[] = ['tonight', 'weekend', 'nextWeek', 'holiday']

type HotelSearchCardProps = {
  isBusy: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  roomCount: number
  guestCount: number
  hotelPreference: HotelPreference
  nearbyPreference: string
  selectedQuickDatePreset: HotelQuickDatePreset | null
  hotDestinations: string[]
  recentSearches: string[]
  averagePriceInsight: string
  translate: (translationKey: string) => string
  onSearchLocationChange: (value: string) => void
  onSearchCheckInDateChange: (value: string) => void
  onSearchCheckOutDateChange: (value: string) => void
  onRoomCountChange: (value: number) => void
  onGuestCountChange: (value: number) => void
  onHotelPreferenceChange: (value: HotelPreference) => void
  onNearbyPreferenceChange: (value: string) => void
  onSelectQuickDatePreset: (preset: HotelQuickDatePreset, nextDates: { checkInDate: string; checkOutDate: string }) => void
  onSelectDestination: (value: string) => void
  onSearch: () => void
}

export function HotelSearchCard({
  isBusy,
  searchLocation,
  searchCheckInDate,
  searchCheckOutDate,
  roomCount,
  guestCount,
  hotelPreference,
  nearbyPreference,
  selectedQuickDatePreset,
  hotDestinations,
  recentSearches,
  averagePriceInsight,
  translate,
  onSearchLocationChange,
  onSearchCheckInDateChange,
  onSearchCheckOutDateChange,
  onRoomCountChange,
  onGuestCountChange,
  onHotelPreferenceChange,
  onNearbyPreferenceChange,
  onSelectQuickDatePreset,
  onSelectDestination,
  onSearch,
}: HotelSearchCardProps) {
  const stayNights = countHotelStayNights(searchCheckInDate, searchCheckOutDate)

  return (
    <section className="grid gap-5 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('nav.hotels')}</p>
          <h2 className="m-0 text-4xl font-bold leading-tight text-slate-950">{translate('hotels.searchModuleTitle')}</h2>
        </div>
        <div className="grid gap-3 md:grid-cols-2">
          <div className="border border-slate-200 bg-white p-4 text-sm leading-6 text-slate-600">
            <span>{translate('hotels.averagePrice')}</span>
            <strong>{averagePriceInsight}</strong>
          </div>
          <div className="border border-slate-200 bg-white p-4 text-sm leading-6 text-slate-600">
            <span>{translate('hotels.stayNights')}</span>
            <strong>{translate('hotels.stayNightsValue').replace('{count}', String(stayNights))}</strong>
          </div>
        </div>
      </div>

      <HotDestinations destinations={hotDestinations} translate={translate} onSelectDestination={onSelectDestination} />

      <div className="grid gap-4 md:grid-cols-3 hotel-search-grid-primary">
        <DestinationSelector
          value={searchLocation}
          translate={translate}
          suggestions={[...recentSearches, ...hotDestinations]}
          onChange={onSearchLocationChange}
        />
        <DateRangeSelector
          checkInDate={searchCheckInDate}
          checkOutDate={searchCheckOutDate}
          translate={translate}
          onCheckInDateChange={onSearchCheckInDateChange}
          onCheckOutDateChange={onSearchCheckOutDateChange}
        />
      </div>

      <div className="grid gap-2">
        <span className="text-sm font-medium text-slate-500">{translate('hotels.quickDate')}</span>
        <div className="flex flex-wrap items-center gap-3">
          {quickDatePresets.map(preset => (
            <button
              key={preset}
              type="button"
              className={`inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 ${selectedQuickDatePreset === preset ? 'border-black bg-black text-white' : ''}`}
              onClick={() => onSelectQuickDatePreset(preset, applyHotelQuickDatePreset(preset))}
            >
              {translate(`hotels.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-[1fr_1fr_auto] items-end hotel-search-grid-secondary">
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
        />
        <button type="button" className="inline-flex min-h-12 items-center justify-center border border-sky-400 bg-sky-400 px-5 py-2 font-bold text-slate-950 shadow-xl shadow-sky-200/70 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onSearch} disabled={isBusy}>
          {translate('hotels.search')}
        </button>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('hotels.recentSearches')}</span>
          <div className="flex flex-wrap items-center gap-3">
            {recentSearches.map(item => (
              <span key={item} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{item}</span>
            ))}
          </div>
        </div>
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('hotels.promoHint')}</span>
          <div className="flex flex-wrap items-center gap-3">
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{translate('hotels.promoValueOne')}</span>
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{translate('hotels.promoValueTwo')}</span>
          </div>
        </div>
      </div>
    </section>
  )
}

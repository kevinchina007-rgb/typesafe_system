import { Search } from 'lucide-react'

import { HotDestinations } from '@/pages/HotelsPage/components/controls/HotDestinations'
import { DateRangeSelector } from '@/pages/HotelsPage/components/controls/DateRangeSelector'
import { DestinationSelector } from '@/pages/HotelsPage/components/controls/DestinationSelector'

type HotelSearchCardProps = {
  isBusy: boolean
  searchLocation: string
  searchCheckInDate: string
  searchCheckOutDate: string
  hotDestinations: string[]
  translate: (translationKey: string) => string
  onSearchLocationChange: (value: string) => void
  onSearchCheckInDateChange: (value: string) => void
  onSearchCheckOutDateChange: (value: string) => void
  onSelectDestination: (value: string) => void
  onSearch: () => void
}

export function HotelSearchCard({
  isBusy,
  searchLocation,
  searchCheckInDate,
  searchCheckOutDate,
  hotDestinations,
  translate,
  onSearchLocationChange,
  onSearchCheckInDateChange,
  onSearchCheckOutDateChange,
  onSelectDestination,
  onSearch,
}: HotelSearchCardProps) {
  return (
    <section className="grid gap-6 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="grid gap-1">
        <p className="text-sm font-bold text-slate-500">{translate('nav.hotels')}</p>
        <h2 className="m-0 text-4xl font-bold leading-tight text-slate-950">{translate('hotels.searchModuleTitle')}</h2>
      </div>

      <div className="grid gap-4 lg:grid-cols-[1.2fr_1fr_1fr_auto]">
        <DestinationSelector
          value={searchLocation}
          translate={translate}
          suggestions={hotDestinations}
          onChange={onSearchLocationChange}
        />
        <DateRangeSelector
          checkInDate={searchCheckInDate}
          checkOutDate={searchCheckOutDate}
          translate={translate}
          onCheckInDateChange={onSearchCheckInDateChange}
          onCheckOutDateChange={onSearchCheckOutDateChange}
        />
        <div className="flex items-end">
          <button
            type="button"
            className="inline-flex min-h-14 w-full items-center justify-center gap-2 bg-gradient-to-r from-amber-400 to-orange-500 px-6 py-3 text-lg font-bold text-white shadow-xl shadow-orange-200/70 transition hover:from-amber-500 hover:to-orange-600 disabled:cursor-not-allowed disabled:opacity-55"
            onClick={onSearch}
            disabled={isBusy}
          >
            <Search className="h-5 w-5" />
            {translate('hotels.search')}
          </button>
        </div>
      </div>

      <HotDestinations destinations={hotDestinations} translate={translate} onSelectDestination={onSelectDestination} />
    </section>
  )
}

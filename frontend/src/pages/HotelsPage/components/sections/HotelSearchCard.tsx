import { Search } from 'lucide-react'

import { HotDestinations } from '@/pages/HotelsPage/components/controls/HotDestinations'
import { DateRangeSelector } from '@/pages/HotelsPage/components/controls/DateRangeSelector'
import { DestinationSelector } from '@/pages/HotelsPage/components/controls/DestinationSelector'
import type { HotelSearchCardProps } from '@/pages/HotelsPage/objects'

// 酒店搜索卡片，负责目的地、日期和搜索按钮。
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
    <section className="grid gap-6 border border-rose-100 bg-gradient-to-br from-white via-rose-50 to-sky-50 p-6 text-slate-950 shadow-lg shadow-sky-100/50">
      <div className="grid gap-4 rounded-2xl border border-slate-200 bg-white/90 p-4 lg:grid-cols-[1.2fr_1fr_1fr_auto]">
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
            className="inline-flex min-h-14 w-full items-center justify-center gap-2 bg-gradient-to-r from-fuchsia-500 via-pink-500 to-orange-500 px-6 py-3 text-lg font-black text-white shadow-xl shadow-pink-200/70 transition hover:from-fuchsia-600 hover:via-pink-600 hover:to-orange-600 disabled:cursor-not-allowed disabled:opacity-55"
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

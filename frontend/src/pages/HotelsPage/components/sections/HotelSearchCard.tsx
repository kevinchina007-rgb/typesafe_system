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
    <section className="grid gap-6 border border-rose-100 bg-gradient-to-br from-white via-rose-50 to-sky-50 p-6 text-slate-950 shadow-lg shadow-sky-100/50">
      <div className="grid gap-2">
        <div className="flex flex-wrap items-center gap-3">
          <p className="m-0 text-sm font-black uppercase tracking-[0.18em] text-sky-600">{translate('nav.hotels')}</p>
          <span className="inline-flex w-fit bg-rose-500 px-2 py-1 text-xs font-bold text-white">目的地 + 日期</span>
          <span className="inline-flex w-fit bg-amber-100 px-2 py-1 text-xs font-bold text-amber-700">智能筛选</span>
        </div>
        <h2 className="m-0 text-5xl font-black leading-tight text-slate-950">{translate('hotels.searchModuleTitle')}</h2>
        <p className="m-0 max-w-4xl text-lg leading-8 text-slate-600">先用一组核心条件锁定入住，再展开偏好筛选和房源结果。</p>
      </div>

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

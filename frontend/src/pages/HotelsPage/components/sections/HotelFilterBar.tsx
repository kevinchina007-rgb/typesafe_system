import { HotelPreferenceSelector } from '@/pages/HotelsPage/components/controls/HotelPreferenceSelector'
import type { HotelFilterBarProps } from '@/pages/HotelsPage/objects'

export function HotelFilterBar({
  hotelPreference,
  nearbyPreference,
  isBusy,
  translate,
  onHotelPreferenceChange,
  onNearbyPreferenceChange,
}: HotelFilterBarProps) {
  return (
    <section className="grid gap-5 border border-cyan-100 bg-gradient-to-r from-white via-cyan-50 to-emerald-50 p-5 text-slate-950 shadow-sm shadow-cyan-100/50">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <div className="grid gap-1">
          <p className="text-sm font-black uppercase tracking-[0.18em] text-cyan-600">{translate('hotels.filterTitle')}</p>
          <h3 className="m-0 text-2xl font-black text-slate-950">偏好筛选</h3>
        </div>
        <span className="inline-flex w-fit bg-emerald-100 px-3 py-1 text-xs font-bold text-emerald-700">房型 / 周边 / 品牌</span>
      </div>
      <div className="grid gap-4">
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

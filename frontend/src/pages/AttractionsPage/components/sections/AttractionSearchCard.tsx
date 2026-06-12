import { applyAttractionQuickDatePreset } from '@/app/stores/models/attraction-booking-model'
import { AttractionKeywordInput } from '@/pages/AttractionsPage/components/controls/AttractionKeywordInput'
import { CitySelector } from '@/pages/AttractionsPage/components/controls/CitySelector'
import { DateSelector } from '@/pages/AttractionsPage/components/controls/DateSelector'
import { HotAttractions } from '@/pages/AttractionsPage/components/controls/HotAttractions'
import { ATTRACTION_QUICK_DATE_PRESETS, type AttractionSearchCardProps } from '../../objects'

// AttractionsPage 的搜索卡片，用于填写城市、关键字和日期。
export function AttractionSearchCard({
  isBusy,
  searchCity,
  keyword,
  useDateDraft,
  selectedQuickDatePreset,
  hotAttractions,
  recentSearches,
  translate,
  onSearchCityChange,
  onKeywordChange,
  onUseDateChange,
  onSelectQuickDatePreset,
  onSelectHotAttraction,
  onSearch,
}: AttractionSearchCardProps) {
  return (
    <section className="grid gap-5 border border-sky-200 bg-gradient-to-br from-slate-50 via-cyan-50 to-indigo-50 p-6 text-slate-950 shadow-lg shadow-cyan-100/50">
      <div className="grid gap-5 lg:grid-cols-[1.2fr_1fr]">
        <HotAttractions items={hotAttractions} translate={translate} onSelect={onSelectHotAttraction} />

        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('attractions.quickDate')}</span>
          <div className="flex flex-wrap items-center gap-3">
            {ATTRACTION_QUICK_DATE_PRESETS.map(preset => (
              <button
                key={preset}
                type="button"
                className={`inline-flex min-h-10 items-center justify-center border border-sky-200 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-sky-500 hover:bg-sky-100 hover:text-slate-950 disabled:cursor-not-allowed disabled:opacity-55 ${selectedQuickDatePreset === preset ? 'border-sky-700 bg-sky-600 text-white shadow-md shadow-sky-200/70' : ''}`}
                onClick={() => onSelectQuickDatePreset(preset, applyAttractionQuickDatePreset(preset))}
              >
                {translate(`attractions.quickDate.${preset}`)}
              </button>
            ))}
          </div>
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <CitySelector value={searchCity} translate={translate} suggestions={[...hotAttractions, ...recentSearches]} onChange={onSearchCityChange} />
        <AttractionKeywordInput value={keyword} translate={translate} onChange={onKeywordChange} />
        <DateSelector value={useDateDraft} translate={translate} onChange={onUseDateChange} />
      </div>

      <div className="grid gap-4 md:grid-cols-[1fr_auto]">
        <button
          type="button"
          className="inline-flex min-h-12 items-center justify-center border border-sky-500 bg-sky-500 px-5 py-2 font-bold text-white shadow-xl shadow-sky-200/70 transition hover:border-sky-700 hover:bg-sky-700 disabled:cursor-not-allowed disabled:opacity-55"
          onClick={onSearch}
          disabled={isBusy}
        >
          {translate('attractions.search')}
        </button>
      </div>
    </section>
  )
}

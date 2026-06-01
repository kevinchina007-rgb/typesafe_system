import { applyAttractionQuickDatePreset } from '@/app/stores/models/attraction-booking-model'
import { AttractionKeywordInput } from '@/pages/AttractionsPage/components/controls/AttractionKeywordInput'
import { CitySelector } from '@/pages/AttractionsPage/components/controls/CitySelector'
import { DateSelector } from '@/pages/AttractionsPage/components/controls/DateSelector'
import { HotAttractions } from '@/pages/AttractionsPage/components/controls/HotAttractions'
import { ATTRACTION_QUICK_DATE_PRESETS, type AttractionSearchCardProps } from '../../objects'

export function AttractionSearchCard({
  isBusy,
  searchCity,
  keyword,
  useDateDraft,
  selectedQuickDatePreset,
  hotAttractions,
  recentSearches,
  insight,
  translate,
  onSearchCityChange,
  onKeywordChange,
  onUseDateChange,
  onSelectQuickDatePreset,
  onSelectHotAttraction,
  onSearch,
}: AttractionSearchCardProps) {
  return (
    <section className="grid gap-5 border border-sky-100 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-6 text-slate-950 shadow-lg shadow-cyan-100/40">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('nav.attractions')}</p>
          <h2 className="m-0 text-4xl font-bold leading-tight text-slate-950">{translate('attractions.searchModuleTitle')}</h2>
          <p className="m-0 mt-2 max-w-3xl text-base leading-7 text-slate-600">先按城市锁定景点，再用关键词挑出最贴近的票型和场次，像酒店一样把搜索和下单连起来。</p>
        </div>
        <div className="grid gap-3 md:grid-cols-2">
          <div className="border border-slate-200 bg-white p-4 text-sm leading-6 text-slate-600">
            <span>{translate('attractions.hotTrend')}</span>
            <strong>{insight}</strong>
          </div>
          <div className="border border-slate-200 bg-white p-4 text-sm leading-6 text-slate-600">
            <span>{translate('attractions.ticketReminder')}</span>
            <strong>{translate('attractions.ticketReminderValue')}</strong>
          </div>
        </div>
      </div>

      <HotAttractions items={hotAttractions} translate={translate} onSelect={onSelectHotAttraction} />

      <div className="grid gap-4 md:grid-cols-2">
        <CitySelector value={searchCity} translate={translate} suggestions={[...hotAttractions, ...recentSearches]} onChange={onSearchCityChange} />
        <AttractionKeywordInput value={keyword} translate={translate} onChange={onKeywordChange} />
        <DateSelector value={useDateDraft} translate={translate} onChange={onUseDateChange} />
      </div>

      <div className="inline-flex w-fit items-center gap-2 border border-slate-200 bg-white px-3 py-1 text-xs font-semibold uppercase tracking-[0.14em] text-slate-500">
        <span>城市 + 关键词</span>
        <span className="text-slate-300">/</span>
        <span>使用日期</span>
        <span className="text-slate-300">/</span>
        <span>出行人规则</span>
      </div>

      <div className="grid gap-2">
        <span className="text-sm font-medium text-slate-500">{translate('attractions.quickDate')}</span>
        <div className="flex flex-wrap items-center gap-3">
          {ATTRACTION_QUICK_DATE_PRESETS.map(preset => (
            <button
              key={preset}
              type="button"
              className={`inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 ${selectedQuickDatePreset === preset ? 'border-black bg-black text-white' : ''}`}
              onClick={() => onSelectQuickDatePreset(preset, applyAttractionQuickDatePreset(preset))}
            >
              {translate(`attractions.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 md:grid-cols-[1fr_auto]">
        <button
          type="button"
          className="inline-flex min-h-12 items-center justify-center border border-sky-400 bg-sky-400 px-5 py-2 font-bold text-slate-950 shadow-xl shadow-sky-200/70 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
          onClick={onSearch}
          disabled={isBusy}
        >
          {translate('attractions.search')}
        </button>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('attractions.recentSearches')}</span>
          <div className="flex flex-wrap items-center gap-3">
            {recentSearches.map(item => (
              <span key={item} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                {item}
              </span>
            ))}
          </div>
        </div>
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('attractions.promoHint')}</span>
          <div className="flex flex-wrap items-center gap-3">
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
              {translate('attractions.promoValueOne')}
            </span>
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
              {translate('attractions.promoValueTwo')}
            </span>
          </div>
        </div>
      </div>
    </section>
  )
}

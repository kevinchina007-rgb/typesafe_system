import type { AttractionQuickDatePreset, AttractionSortPreference, AttractionTypePreference } from '@/app/stores/models/attraction-booking-model'
﻿import { AttractionKeywordInput } from '@/pages/AttractionsPage/components/controls/AttractionKeywordInput'
import { AttractionTypeSelector } from '@/pages/AttractionsPage/components/controls/AttractionTypeSelector'
import { CitySelector } from '@/pages/AttractionsPage/components/controls/CitySelector'
import { DateSelector } from '@/pages/AttractionsPage/components/controls/DateSelector'
import { HotAttractions } from '@/pages/AttractionsPage/components/controls/HotAttractions'
import { TravelerCountSelector } from '@/pages/AttractionsPage/components/controls/TravelerCountSelector'
import { applyAttractionQuickDatePreset, attractionSortOptions } from '@/app/stores/models/attraction-booking-model'

const quickDatePresets: AttractionQuickDatePreset[] = ['today', 'tomorrow', 'weekend', 'holiday']

type AttractionSearchCardProps = {
  isBusy: boolean
  searchCity: string
  keyword: string
  useDateDraft: string
  travelerCount: number
  attractionType: AttractionTypePreference
  sortPreference: AttractionSortPreference
  selectedQuickDatePreset: AttractionQuickDatePreset | null
  hotAttractions: string[]
  recentSearches: string[]
  insight: string
  translate: (translationKey: string) => string
  onSearchCityChange: (value: string) => void
  onKeywordChange: (value: string) => void
  onUseDateChange: (value: string) => void
  onTravelerCountChange: (value: number) => void
  onAttractionTypeChange: (value: AttractionTypePreference) => void
  onSortPreferenceChange: (value: AttractionSortPreference) => void
  onSelectQuickDatePreset: (preset: AttractionQuickDatePreset, nextDate: string) => void
  onSelectHotAttraction: (value: string) => void
  onSearch: () => void
}

export function AttractionSearchCard({
  isBusy,
  searchCity,
  keyword,
  useDateDraft,
  travelerCount,
  attractionType,
  sortPreference,
  selectedQuickDatePreset,
  hotAttractions,
  recentSearches,
  insight,
  translate,
  onSearchCityChange,
  onKeywordChange,
  onUseDateChange,
  onTravelerCountChange,
  onAttractionTypeChange,
  onSortPreferenceChange,
  onSelectQuickDatePreset,
  onSelectHotAttraction,
  onSearch,
}: AttractionSearchCardProps) {
  return (
    <section className="grid gap-5 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('nav.attractions')}</p>
          <h2 className="m-0 text-4xl font-bold leading-tight text-slate-950">{translate('attractions.searchModuleTitle')}</h2>
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

      <div className="grid gap-4 md:grid-cols-3 md:grid-cols-3">
        <CitySelector value={searchCity} translate={translate} suggestions={[...hotAttractions, ...recentSearches]} onChange={onSearchCityChange} />
        <AttractionKeywordInput value={keyword} translate={translate} onChange={onKeywordChange} />
        <DateSelector value={useDateDraft} translate={translate} onChange={onUseDateChange} />
      </div>

      <div className="grid gap-2">
        <span className="text-sm font-medium text-slate-500">{translate('attractions.quickDate')}</span>
        <div className="flex flex-wrap items-center gap-3">
          {quickDatePresets.map(preset => (
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

      <div className="grid gap-4 md:grid-cols-[1fr_1fr_auto] items-end md:grid-cols-[1fr_auto]">
        <TravelerCountSelector value={travelerCount} translate={translate} onChange={onTravelerCountChange} />
        <AttractionTypeSelector value={attractionType} translate={translate} onChange={onAttractionTypeChange} />
        <label className="grid gap-2 text-sm font-medium text-slate-600 grid gap-2">
          <span>{translate('attractions.sortPreference')}</span>
          <select value={sortPreference} onChange={event => onSortPreferenceChange(event.target.value as AttractionSortPreference)}>
            {attractionSortOptions.map(option => (
              <option key={option} value={option}>
                {translate(`attractions.sortPreference.${option}`)}
              </option>
            ))}
          </select>
        </label>
        <button type="button" className="inline-flex min-h-12 items-center justify-center border border-sky-400 bg-sky-400 px-5 py-2 font-bold text-slate-950 shadow-xl shadow-sky-200/70 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" onClick={onSearch} disabled={isBusy}>
          {translate('attractions.search')}
        </button>
      </div>

      <div className="grid gap-4 md:grid-cols-2">
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('attractions.recentSearches')}</span>
          <div className="flex flex-wrap items-center gap-3">
            {recentSearches.map(item => (
              <span key={item} className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{item}</span>
            ))}
          </div>
        </div>
        <div className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('attractions.promoHint')}</span>
          <div className="flex flex-wrap items-center gap-3">
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{translate('attractions.promoValueOne')}</span>
            <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{translate('attractions.promoValueTwo')}</span>
          </div>
        </div>
      </div>
    </section>
  )
}

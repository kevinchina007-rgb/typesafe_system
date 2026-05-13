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
    <section className="resource-search-card app-card">
      <div className="resource-search-card-head">
        <div>
          <p className="eyebrow-label">{translate('nav.attractions')}</p>
          <h2 className="resource-search-card-title">{translate('attractions.searchModuleTitle')}</h2>
        </div>
        <div className="resource-search-card-tip-grid">
          <div className="resource-search-card-tip">
            <span>{translate('attractions.hotTrend')}</span>
            <strong>{insight}</strong>
          </div>
          <div className="resource-search-card-tip">
            <span>{translate('attractions.ticketReminder')}</span>
            <strong>{translate('attractions.ticketReminderValue')}</strong>
          </div>
        </div>
      </div>

      <HotAttractions items={hotAttractions} translate={translate} onSelect={onSelectHotAttraction} />

      <div className="resource-search-grid resource-search-grid-primary attraction-search-grid-primary">
        <CitySelector value={searchCity} translate={translate} suggestions={[...hotAttractions, ...recentSearches]} onChange={onSearchCityChange} />
        <AttractionKeywordInput value={keyword} translate={translate} onChange={onKeywordChange} />
        <DateSelector value={useDateDraft} translate={translate} onChange={onUseDateChange} />
      </div>

      <div className="resource-quick-date-row">
        <span className="resource-quick-date-label">{translate('attractions.quickDate')}</span>
        <div className="resource-quick-date-list">
          {quickDatePresets.map(preset => (
            <button
              key={preset}
              type="button"
              className={`resource-chip-button ${selectedQuickDatePreset === preset ? 'is-active' : ''}`}
              onClick={() => onSelectQuickDatePreset(preset, applyAttractionQuickDatePreset(preset))}
            >
              {translate(`attractions.quickDate.${preset}`)}
            </button>
          ))}
        </div>
      </div>

      <div className="resource-search-grid resource-search-grid-secondary attraction-search-grid-secondary">
        <TravelerCountSelector value={travelerCount} translate={translate} onChange={onTravelerCountChange} />
        <AttractionTypeSelector value={attractionType} translate={translate} onChange={onAttractionTypeChange} />
        <label className="resource-search-label resource-inline-field">
          <span>{translate('attractions.sortPreference')}</span>
          <select value={sortPreference} onChange={event => onSortPreferenceChange(event.target.value as AttractionSortPreference)}>
            {attractionSortOptions.map(option => (
              <option key={option} value={option}>
                {translate(`attractions.sortPreference.${option}`)}
              </option>
            ))}
          </select>
        </label>
        <button type="button" className="resource-search-submit-button" onClick={onSearch} disabled={isBusy}>
          {translate('attractions.search')}
        </button>
      </div>

      <div className="resource-search-assist">
        <div className="resource-search-assist-block">
          <span className="resource-search-assist-label">{translate('attractions.recentSearches')}</span>
          <div className="resource-search-assist-tags">
            {recentSearches.map(item => (
              <span key={item} className="resource-tag-muted">{item}</span>
            ))}
          </div>
        </div>
        <div className="resource-search-assist-block">
          <span className="resource-search-assist-label">{translate('attractions.promoHint')}</span>
          <div className="resource-search-assist-tags">
            <span className="resource-tag-muted">{translate('attractions.promoValueOne')}</span>
            <span className="resource-tag-muted">{translate('attractions.promoValueTwo')}</span>
          </div>
        </div>
      </div>
    </section>
  )
}

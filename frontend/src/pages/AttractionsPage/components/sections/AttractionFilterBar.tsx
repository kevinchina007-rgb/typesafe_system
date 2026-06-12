import { attractionFilterOptions, attractionSortOptions } from '@/app/stores/models/attraction-booking-model'
import type { AttractionFilterBarProps } from '../../objects'

// 景点结果筛选条，只负责切换排序方式。
export function AttractionFilterBar({ hasSearchedAttractions, sortPreference, translate, onSortPreferenceChange }: AttractionFilterBarProps) {
  return (
    <section className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div className="grid gap-4">
          <p className="text-sm font-bold text-slate-500">{translate('attractions.filterTitle')}</p>
          <div className="flex flex-wrap items-center gap-3">
            {attractionFilterOptions.map(filterKey => (
              <button key={filterKey} type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55">
                {translate(`attractions.filter.${filterKey}`)}
              </button>
            ))}
          </div>
        </div>

        {hasSearchedAttractions ? (
          <label className="grid gap-2 text-sm font-medium text-slate-600">
            <span>{translate('attractions.sortPreference')}</span>
            <select
              value={sortPreference}
              onChange={event => onSortPreferenceChange(event.target.value as AttractionFilterBarProps['sortPreference'])}
              className="min-w-48"
            >
              {attractionSortOptions.map(option => (
                <option key={option} value={option}>
                  {translate(`attractions.sortPreference.${option}`)}
                </option>
              ))}
            </select>
          </label>
        ) : null}
      </div>
    </section>
  )
}

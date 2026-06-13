import type { Dispatch, SetStateAction } from 'react'

import { flightCityOptions, timeWindows } from './AdvertisementSubmissionWorkspaceUtils'

export type AdvertisementSubmissionWorkspaceFlightSearchPanelProps = {
  defaultTargetResourceType: 'Flight' | 'Hotel' | 'Train' | 'Attraction'
  flightSearchResults: Array<{
    value: string
    departureCity?: string
    arrivalCity?: string
  }>
  hasSearchedFlights: boolean
  hyperlinkEnabled: boolean
  resourceOptions: Array<{ value: string; label: string }>
  searchDraft: {
    departureCity: string
    arrivalCity: string
    departureDate: string
    timeRange: string
  }
  targetResourceId: string
  translate: (key: string) => string
  updateSearchDraftField: (nextField: Partial<{
    departureCity: string
    arrivalCity: string
    departureDate: string
    timeRange: string
  }>) => void
  runFlightSearch: () => void
  setTargetResourceId: Dispatch<SetStateAction<string>>
}

export function AdvertisementSubmissionWorkspaceFlightSearchPanel({
  defaultTargetResourceType,
  flightSearchResults,
  hasSearchedFlights,
  hyperlinkEnabled,
  resourceOptions,
  searchDraft,
  targetResourceId,
  translate,
  updateSearchDraftField,
  runFlightSearch,
  setTargetResourceId,
}: AdvertisementSubmissionWorkspaceFlightSearchPanelProps) {
  return (
    <>
      {hyperlinkEnabled ? (
        defaultTargetResourceType === 'Flight' ? (
          <section className="grid gap-4">
            <div className="grid gap-4 xl:grid-cols-[1fr_1fr_1fr_1fr_auto]">
              <label className="grid gap-2">
                <span>{translate('advertising.flight.departureCity')}</span>
                <select value={searchDraft.departureCity} onChange={event => updateSearchDraftField({ departureCity: event.target.value })}>
                  <option value="">{translate('advertising.flight.allCities')}</option>
                  {flightCityOptions.map(city => <option key={city} value={city}>{city}</option>)}
                </select>
              </label>
              <label className="grid gap-2">
                <span>{translate('advertising.flight.arrivalCity')}</span>
                <select value={searchDraft.arrivalCity} onChange={event => updateSearchDraftField({ arrivalCity: event.target.value })}>
                  <option value="">{translate('advertising.flight.allCities')}</option>
                  {flightCityOptions.map(city => <option key={city} value={city}>{city}</option>)}
                </select>
              </label>
              <label className="grid gap-2">
                <span>{translate('advertising.flight.departureDate')}</span>
                <input type="date" value={searchDraft.departureDate} onChange={event => updateSearchDraftField({ departureDate: event.target.value })} />
              </label>
              <label className="grid gap-2">
                <span>{translate('advertising.flight.timeRange')}</span>
                <select value={searchDraft.timeRange} onChange={event => updateSearchDraftField({ timeRange: event.target.value })}>
                  <option value="all">{translate('advertising.flight.allDay')}</option>
                  {timeWindows.map(window => <option key={window} value={window}>{window}</option>)}
                </select>
              </label>
              <button
                type="button"
                className="inline-flex min-h-11 items-center justify-center self-end border border-slate-950 bg-slate-950 px-4 py-2 text-sm font-semibold text-white"
                onClick={runFlightSearch}
              >
                搜索航班
              </button>
            </div>

            {!hasSearchedFlights ? (
              <p className="m-0 text-sm leading-6 text-slate-500">先筛选条件，再点“搜索航班”生成可选航班。</p>
            ) : flightSearchResults.length === 0 ? (
              <p className="m-0 text-sm leading-6 text-slate-500">没有找到符合条件的航班。</p>
            ) : (
              <div className="grid gap-3">
                {flightSearchResults.map(option => (
                  <button
                    key={option.value}
                    type="button"
                    className={`grid gap-1 border p-4 text-left transition ${targetResourceId === option.value ? 'border-pink-500 bg-pink-50' : 'border-slate-200 bg-white hover:border-slate-950'}`}
                    onClick={() => setTargetResourceId(option.value)}
                  >
                    <span className="text-sm font-semibold text-slate-950">{option.departureCity ?? ''} → {option.arrivalCity ?? ''}</span>
                  </button>
                ))}
              </div>
            )}
          </section>
        ) : (
          <label className="grid gap-2">
            <span>{translate('advertising.field.targetResource')}</span>
            <select value={targetResourceId} onChange={event => setTargetResourceId(event.target.value)}>
              {resourceOptions.map(option => <option key={option.value} value={option.value}>{option.label}</option>)}
            </select>
          </label>
        )
      ) : (
        <p className="m-0 text-sm leading-6 text-slate-500">当前广告不绑定资源，保存后只展示创意内容。</p>
      )}
    </>
  )
}

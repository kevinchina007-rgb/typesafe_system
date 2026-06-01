import type { TrainSearchCardProps } from '@/pages/TrainsPage/objects'

export function TrainSearchCard({
  isBusy,
  searchDate,
  searchFromStation,
  searchToStation,
  translate,
  onSearchDateChange,
  onSearchFromStationChange,
  onSearchToStationChange,
  onSearch,
}: TrainSearchCardProps) {
  return (
    <form
      className="grid gap-5 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
      onSubmit={async event => {
        event.preventDefault()
        await onSearch()
      }}
    >
      <div className="grid gap-1">
        <p className="text-sm font-bold text-slate-500">{translate('trains.searchModuleTitle')}</p>
        <h3 className="m-0 text-2xl font-black text-slate-950">{translate('trains.title')}</h3>
        <p className="m-0 text-sm leading-6 text-slate-500">{translate('trains.searchHint')}</p>
      </div>

      <div className="grid gap-4 xl:grid-cols-[1fr_1fr_220px_auto]">
        <label className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('trains.departureStation')}</span>
          <input
            type="text"
            value={searchFromStation}
            disabled={isBusy}
            onChange={event => onSearchFromStationChange(event.target.value)}
            placeholder={translate('trains.departureStationPlaceholder')}
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('trains.arrivalStation')}</span>
          <input
            type="text"
            value={searchToStation}
            disabled={isBusy}
            onChange={event => onSearchToStationChange(event.target.value)}
            placeholder={translate('trains.arrivalStationPlaceholder')}
          />
        </label>

        <label className="grid gap-2">
          <span className="text-sm font-medium text-slate-500">{translate('trains.departureDate')}</span>
          <input
            type="date"
            value={searchDate}
            disabled={isBusy}
            onChange={event => onSearchDateChange(event.target.value)}
          />
        </label>

        <button
          className="inline-flex min-h-11 items-center justify-center border border-sky-500 bg-sky-500 px-5 py-2 text-sm font-semibold text-white shadow-none transition hover:border-sky-600 hover:bg-sky-600 disabled:cursor-not-allowed disabled:opacity-55"
          type="button"
          disabled={isBusy}
          onClick={async () => {
            await onSearch()
          }}
        >
          {translate('trains.search')}
        </button>
      </div>
    </form>
  )
}

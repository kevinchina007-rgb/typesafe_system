import type { FlightSortMode } from '../../objects'

export function FlightResultsSorting({
  sortMode,
  onSortModeChange,
}: {
  sortMode: FlightSortMode
  onSortModeChange: (value: FlightSortMode) => void
}) {
  return (
    <div className="flex flex-wrap items-center gap-5 text-base font-medium">
      <button
        type="button"
        className={sortMode === 'price' ? 'text-sky-600' : 'text-slate-800'}
        onClick={() => onSortModeChange('price')}
      >
        低价优先
      </button>
      <button
        type="button"
        className={sortMode === 'departureTime' ? 'text-sky-600' : 'text-slate-800'}
        onClick={() => onSortModeChange('departureTime')}
      >
        起飞时间
      </button>
    </div>
  )
}

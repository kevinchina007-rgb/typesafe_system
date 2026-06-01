import type { TravelerResponse } from '@/lib/mvp-types/index'

type TravelerSelectionPanelProps = {
  title: string
  hint: string
  travelers: TravelerResponse[]
  selectedTravelerIds: string[]
  onToggleTravelerSelection: (travelerId: string) => void
  renderTravelerLabel: (traveler: TravelerResponse) => string
  emptySelectionMessage: string
}

export function TravelerSelectionPanel({
  title,
  hint,
  travelers,
  selectedTravelerIds,
  onToggleTravelerSelection,
  renderTravelerLabel,
  emptySelectionMessage,
}: TravelerSelectionPanelProps) {
  if (travelers.length === 0) {
    return null
  }

  return (
    <section className="grid gap-4 border border-slate-200 bg-white px-6 py-5">
      <div className="flex flex-wrap items-center justify-between gap-3">
        <h3 className="m-0 text-2xl font-bold text-slate-950">{title}</h3>
        <p className="m-0 text-sm font-medium text-slate-500">{hint}</p>
      </div>
      <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-3">
        {travelers.map(traveler => {
          const isSelected = selectedTravelerIds.includes(traveler.travelerId)
          return (
            <label
              key={traveler.travelerId}
              className={`flex cursor-pointer items-center gap-4 border px-4 py-4 text-base font-semibold transition ${
                isSelected ? 'border-sky-500 bg-sky-50 text-slate-950' : 'border-slate-200 bg-white text-slate-600 hover:border-slate-400'
              }`}
            >
              <input type="checkbox" checked={isSelected} onChange={() => onToggleTravelerSelection(traveler.travelerId)} />
              <span className="inline-flex h-11 w-11 items-center justify-center border border-slate-300 bg-slate-50 text-lg font-black text-slate-700">
                {renderTravelerLabel(traveler).slice(0, 1)}
              </span>
              <span className="truncate">{renderTravelerLabel(traveler)}</span>
            </label>
          )
        })}
      </div>
      {selectedTravelerIds.length === 0 ? <p className="m-0 text-sm font-medium text-rose-600">{emptySelectionMessage}</p> : null}
    </section>
  )
}

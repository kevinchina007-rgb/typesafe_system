import type { TravelerResponse } from '@/lib/mvp-types/index'
import { formatTravelerIdentity } from '@/pages/BookingsPage/functions'

export function FlightTravelerBadges({ travelerIds, travelers }: { travelerIds: string[]; travelers: TravelerResponse[] }) {
  if (travelerIds.length === 0) {
    return null
  }

  return (
    <div className="grid min-w-44 gap-2 justify-self-start md:justify-self-end">
      {travelerIds.map(travelerId => {
        const label = formatTravelerIdentity(travelers, travelerId)
        return (
          <div key={travelerId} className="flex items-center gap-2 text-sm font-semibold text-slate-600">
            <span className="inline-flex h-9 w-9 items-center justify-center border border-slate-300 bg-slate-50 text-base font-black text-slate-700">{label.slice(0, 1)}</span>
            <span className="max-w-32 truncate">{label}</span>
          </div>
        )
      })}
    </div>
  )
}

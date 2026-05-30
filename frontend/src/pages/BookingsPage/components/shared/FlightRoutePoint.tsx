import { formatFlightAirportLabel } from '@/app/stores/models/flights/flightConstants'

export function FlightRoutePoint({ airportCode, city }: { airportCode: string; city: string }) {
  return (
    <div className="grid gap-2">
      <span className="text-6xl font-black tracking-normal text-slate-950">{city}</span>
      <span className="text-2xl font-medium leading-tight text-slate-600">{formatFlightAirportLabel(airportCode)}</span>
    </div>
  )
}

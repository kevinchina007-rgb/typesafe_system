import type { HotRoute } from '@/app/stores/models/flights'

export type { HotRoute }

type HotRoutesProps = {
  routes: HotRoute[]
  translate: (translationKey: string) => string
  onSelectRoute: (route: HotRoute) => void
}

export function HotRoutes({ routes, translate, onSelectRoute }: HotRoutesProps) {
  return (
    <div className="flight-hot-routes">
      <span className="flight-hot-routes-label">{translate('flights.hotRoutes')}</span>
      <div className="flight-hot-routes-list">
        {routes.map(route => (
          <button key={route.id} type="button" className="flight-tag-button" onClick={() => onSelectRoute(route)}>
            {route.departureLabel} <span aria-hidden="true">?</span> {route.arrivalLabel}
          </button>
        ))}
      </div>
    </div>
  )
}

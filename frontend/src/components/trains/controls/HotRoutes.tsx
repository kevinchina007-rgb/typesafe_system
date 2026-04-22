export type TrainHotRoute = {
  id: string
  departureLabel: string
  arrivalLabel: string
}
type HotRoutesProps = {
  routes: TrainHotRoute[]
  translate: (translationKey: string) => string
  onSelectRoute: (route: TrainHotRoute) => void
}
export function HotRoutes({ routes, translate, onSelectRoute }: HotRoutesProps) {
  return (
    <div className="resource-hot-routes">
      <span className="resource-hot-routes-label">{translate('trains.hotRoutes')}</span>
      <div className="resource-hot-routes-list">
        {routes.map(route => (
          <button key={route.id} type="button" className="resource-tag-button" onClick={() => onSelectRoute(route)}>
            {route.departureLabel} <span aria-hidden="true">→</span> {route.arrivalLabel}
          </button>
        ))}
      </div>
    </div>
  )
}

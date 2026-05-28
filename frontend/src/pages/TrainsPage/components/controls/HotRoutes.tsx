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
    <div className="grid gap-2">
      <span className="text-sm font-medium text-slate-500">{translate('trains.hotRoutes')}</span>
      <div className="flex flex-wrap items-center gap-3">
        {routes.map(route => (
          <button
            key={route.id}
            type="button"
            className="inline-flex min-h-10 items-center justify-center border border-sky-200 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-sky-500 hover:bg-sky-500 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
            onClick={() => onSelectRoute(route)}
          >
            {route.departureLabel} <span aria-hidden="true">→</span> {route.arrivalLabel}
          </button>
        ))}
      </div>
    </div>
  )
}

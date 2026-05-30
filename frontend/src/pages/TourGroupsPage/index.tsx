import { TourGroupsPageShell } from './components'
import { useTourGroupsPageController } from './hooks'
import { TOUR_GROUPS_PAGE_REGIONS, type TourGroupsPageProps } from './objects'

export function TourGroupsPage(props: TourGroupsPageProps) {
  const controller = useTourGroupsPageController(props)

  return (
    <main className="grid min-h-screen gap-0 bg-slate-50 text-slate-950">
      <section className="grid gap-4 border-b border-slate-200 bg-white px-6 py-5">
        <p className="text-sm font-semibold text-slate-500">{controller.translate('nav.tourGroups')}</p>
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">{controller.translate('tourGroups.title')}</h1>
        <p className="max-w-4xl text-sm leading-6 text-slate-600">{controller.translate('tourGroups.description')}</p>
      </section>

      <section className="grid gap-3 border-b border-slate-200 bg-white px-6 py-4">
        <div className="grid gap-2">
          <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">页面分区</p>
          <div className="grid gap-3 lg:grid-cols-4">
            {TOUR_GROUPS_PAGE_REGIONS.map(region => (
              <article key={region.key} className="grid gap-1 border border-slate-200 bg-slate-50 p-3">
                <p className="text-sm font-semibold text-slate-900">{region.title}</p>
                <p className="text-xs leading-5 text-slate-600">{region.description}</p>
              </article>
            ))}
          </div>
        </div>
      </section>

      <TourGroupsPageShell controller={controller} />
    </main>
  )
}

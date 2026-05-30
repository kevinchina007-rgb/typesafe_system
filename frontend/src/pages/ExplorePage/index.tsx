import { ExplorePageShell } from './components'
import { useExplorePageController } from './hooks'
import { EXPLORE_PAGE_REGIONS, type ExplorePageProps } from './objects'

export function ExplorePage(props: ExplorePageProps) {
  const controller = useExplorePageController(props)

  return (
    <main className="grid min-h-screen gap-0 bg-slate-50 text-slate-950">
      <section className="grid gap-4 border-b border-slate-200 bg-white px-6 py-5">
        <p className="text-sm font-semibold text-slate-500">{props.translate('nav.explore')}</p>
        <h1 className="text-3xl font-bold tracking-tight text-slate-950">{props.translate('explore.title')}</h1>
        <p className="max-w-4xl text-sm leading-6 text-slate-600">{props.translate('explore.description')}</p>
      </section>

      <section className="grid gap-3 border-b border-slate-200 bg-white px-6 py-4">
        <p className="text-xs font-semibold uppercase tracking-[0.2em] text-slate-500">页面分区</p>
        <div className="grid gap-3 lg:grid-cols-4">
          {EXPLORE_PAGE_REGIONS.map(region => (
            <article key={region.key} className="grid gap-1 border border-slate-200 bg-slate-50 p-3">
              <p className="text-sm font-semibold text-slate-900">{region.title}</p>
              <p className="text-xs leading-5 text-slate-600">{region.description}</p>
            </article>
          ))}
        </div>
      </section>

      <ExplorePageShell controller={controller} pageProps={props} />
    </main>
  )
}

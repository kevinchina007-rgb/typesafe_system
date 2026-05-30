import { TravelersPageShell } from './components'
import { useTravelersPageController } from './hooks'
import type { TravelersPageProps } from './objects'

export function TravelersPage(props: TravelersPageProps) {
  const controller = useTravelersPageController(props)

  return (
    <main className="grid min-h-screen gap-0 bg-slate-50 text-slate-950">
      <TravelersPageShell controller={controller} />
    </main>
  )
}

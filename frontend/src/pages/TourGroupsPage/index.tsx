import { useMemo, useState } from 'react'

import { TourGroupsPageShell } from './components'
import { useTourGroupsPageController } from './hooks'
import type { TourGroupsPageProps } from './objects'

export function TourGroupsPage(props: TourGroupsPageProps) {
  const controller = useTourGroupsPageController(props)
  const [activeSection, setActiveSection] = useState<'home' | 'mine'>('home')

  const sectionMeta = useMemo(() => {
    if (activeSection === 'home') {
      return {
        title: '主页',
        description: '浏览其他用户创建的旅游团，搜索合适的目的地并查看行程与详情。',
      }
    }
    return {
      title: '我的',
      description: '查看我创建的旅游团和我加入的旅游团，并继续管理行程或参与协作。',
    }
  }, [activeSection])

  return (
    <main className="grid min-h-screen items-start gap-0 bg-slate-50 text-slate-950 lg:grid-cols-[15rem_minmax(0,1fr)]">
      <aside className="grid h-fit self-start gap-4 border-r border-slate-200 bg-white px-4 py-6">
        <div className="grid gap-2">
          <p className="text-sm font-semibold text-slate-500">{controller.translate('nav.tourGroups')}</p>
          <h1 className="text-2xl font-bold tracking-tight text-slate-950">{controller.translate('tourGroups.title')}</h1>
          <p className="text-sm leading-6 text-slate-600">{controller.translate('tourGroups.description')}</p>
        </div>

        <nav className="grid gap-2">
          <button
            type="button"
            className={`inline-flex min-h-12 items-center justify-start border px-4 py-2 text-sm font-semibold transition ${
              activeSection === 'home'
                ? 'border-black bg-black text-white'
                : 'border-slate-300 bg-white text-slate-950 hover:border-black hover:bg-black hover:text-white'
            }`}
            onClick={() => setActiveSection('home')}
          >
            主页
          </button>
          <button
            type="button"
            className={`inline-flex min-h-12 items-center justify-start border px-4 py-2 text-sm font-semibold transition ${
              activeSection === 'mine'
                ? 'border-black bg-black text-white'
                : 'border-slate-300 bg-white text-slate-950 hover:border-black hover:bg-black hover:text-white'
            }`}
            onClick={() => setActiveSection('mine')}
          >
            我的
          </button>
        </nav>

      </aside>

      <section className="grid min-w-0 gap-4">
        <section className="grid gap-4 border-b border-slate-200 bg-white px-6 py-5">
          <p className="text-sm font-semibold text-slate-500">{controller.translate('nav.tourGroups')}</p>
          <h1 className="text-3xl font-bold tracking-tight text-slate-950">{sectionMeta.title}</h1>
          <p className="max-w-4xl text-sm leading-6 text-slate-600">{sectionMeta.description}</p>
        </section>

        <TourGroupsPageShell controller={{ ...controller, pageMode: activeSection }} />
      </section>
    </main>
  )
}

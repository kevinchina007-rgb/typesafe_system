import { clearAppNotice, useAppShellStore } from '@/app/stores/app-shell-store'
import { ToastNotice } from '@/pages/shared/base/ToastNotice'
import { AppPageShell } from './components/AppPageShell'
import { useAppPageController } from './hooks'
import { APP_PAGE_REGIONS, type AppPageController } from './objects'

// App 级页面入口，根据当前路由状态渲染主壳层和通知区。
export function MvpApp() {
  const currentNotice = useAppShellStore(state => state.currentNotice)
  const controller = useAppPageController()
  const loadingRegion = APP_PAGE_REGIONS[0]
  const shellRegion = APP_PAGE_REGIONS[1]
  const noticeRegion = APP_PAGE_REGIONS[2]

  if (!controller.hasResolvedPrincipalState) {
    return (
      <main className="grid min-h-screen bg-slate-50 text-slate-950" aria-label={loadingRegion.title}>
        <section className="grid w-full gap-6">
          <section className="grid place-items-center gap-3 border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
            <strong>正在恢复工作台状态</strong>
          </section>
        </section>
      </main>
    )
  }

  return (
    <>
      <main className="grid min-h-screen bg-slate-50 text-slate-950" aria-label={shellRegion.title}>
        <section className="grid w-full gap-6">
          <section className="grid gap-0 border-b border-slate-300 bg-white">
            <AppPageShell controller={controller as AppPageController} />
          </section>
        </section>
      </main>

      <section aria-label={noticeRegion.title}>
        <ToastNotice notice={currentNotice} onDismiss={clearAppNotice} />
      </section>
    </>
  )
}

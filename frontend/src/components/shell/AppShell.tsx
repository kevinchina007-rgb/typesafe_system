import type { ComponentProps, ReactNode } from 'react'

import { Sidebar } from './Sidebar'
import { TopNavBar } from './TopNavBar'

type AppShellProps = {
  topNav: ComponentProps<typeof TopNavBar>
  sidebar?: ComponentProps<typeof Sidebar>
  children: ReactNode
}

export function AppShell({ topNav, sidebar, children }: AppShellProps) {
  return (
    <main className="layout-shell">
      <TopNavBar {...topNav} />

      <section className={`app-body-shell ${sidebar ? 'has-sidebar' : 'no-sidebar'}`}>
        {sidebar ? <Sidebar {...sidebar} /> : null}

        <section className="content-shell">
          {children}
        </section>
      </section>
    </main>
  )
}

// 本文件定义应用主壳层，负责页面顶栏、侧边栏和内容区布局。

import type { ComponentProps, ReactNode } from 'react'

import { TopNavBar } from '@/app/shell/TopNavBar'

type AppShellProps = {
  topNav: ComponentProps<typeof TopNavBar>
  isHomePage?: boolean
  children: ReactNode
}

const formSurfaceClassName = [
  "[&_label]:!text-lg",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!min-h-14",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!w-full",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!border-2",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!border-slate-400",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!bg-white",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!px-5",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!py-3",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!text-lg",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!font-medium",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!text-slate-950",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:!outline-none",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:placeholder:!text-transparent",
  "[&_input:not([type='checkbox']):not([type='radio']):not([type='file'])]:focus:!border-black",
  "[&_input[type='checkbox']]:!h-5",
  "[&_input[type='checkbox']]:!w-5",
  "[&_select]:!min-h-14",
  "[&_select]:!w-full",
  "[&_select]:!border-2",
  "[&_select]:!border-slate-400",
  "[&_select]:!bg-white",
  "[&_select]:!px-5",
  "[&_select]:!py-3",
  "[&_select]:!text-lg",
  "[&_select]:!font-medium",
  "[&_select]:!text-slate-950",
  "[&_select]:!outline-none",
  "[&_select]:focus:!border-black",
  "[&_textarea]:!min-h-32",
  "[&_textarea]:!w-full",
  "[&_textarea]:!border-2",
  "[&_textarea]:!border-slate-400",
  "[&_textarea]:!bg-white",
  "[&_textarea]:!px-5",
  "[&_textarea]:!py-3",
  "[&_textarea]:!text-lg",
  "[&_textarea]:!font-medium",
  "[&_textarea]:!text-slate-950",
  "[&_textarea]:!outline-none",
  "[&_textarea]:placeholder:!text-transparent",
  "[&_textarea]:focus:!border-black",
].join(' ')

export function AppShell({ topNav, isHomePage = false, children }: AppShellProps) {
  return (
    <main className={isHomePage ? 'relative min-h-screen' : 'min-h-screen bg-slate-50'}>
      <TopNavBar {...topNav} isOverlay={isHomePage} />

      <section className={isHomePage ? 'min-h-screen' : 'grid gap-6  px-6 py-6 md:px-14'}>
        <section className={`grid w-full gap-6 text-lg ${formSurfaceClassName}`}>
          {children}
        </section>
      </section>
    </main>
  )
}

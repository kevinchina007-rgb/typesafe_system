import type { ReactNode } from 'react'

type SectionHeaderProps = {
  eyebrow: string
  title: string
  actions?: ReactNode
}

export function SectionHeader({ eyebrow, title, actions }: SectionHeaderProps) {
  return (
    <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
      <div>
        <p className="mb-1 text-xs font-bold uppercase tracking-wider text-slate-500">{eyebrow}</p>
        <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950 md:text-3xl">{title}</h2>
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
    </div>
  )
}

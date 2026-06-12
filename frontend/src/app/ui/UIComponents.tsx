// 本文件定义应用级通用 UI 组件，供页面壳层和业务模块复用。

import type { ButtonHTMLAttributes, HTMLAttributes, ReactNode } from 'react'

type AppCardProps = {
  children: ReactNode
  className?: string
} & HTMLAttributes<HTMLElement>

export function AppCard({ children, className = '', ...props }: AppCardProps) {
  return (
    <section className={`grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 ${className}`.trim()} {...props}>
      {children}
    </section>
  )
}

type SectionHeaderProps = {
  eyebrow: string
  title: string
  subtitle?: string
  actions?: ReactNode
}

export function SectionHeader({ eyebrow, title, subtitle, actions }: SectionHeaderProps) {
  return (
    <div className="flex flex-wrap items-start justify-between gap-4">
      <div>
        <p className="text-sm font-bold text-slate-500">{eyebrow}</p>
        <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{title}</h2>
        {subtitle ? <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{subtitle}</p> : null}
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
    </div>
  )
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>

export function PrimaryButton({ className = '', ...props }: ButtonProps) {
  return <button className={`inline-flex min-h-11 items-center justify-center border border-black bg-black px-4 py-2 text-sm font-semibold text-white shadow-none transition hover:bg-white hover:text-black disabled:cursor-not-allowed disabled:opacity-55 ${className}`.trim()} {...props} />
}

export function SecondaryButton({ className = '', ...props }: ButtonProps) {
  return <button className={`inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55  ${className}`.trim()} {...props} />
}

type SearchPanelProps = {
  label: string
  actions?: ReactNode
  children?: ReactNode
}

export function SearchPanel({ label, actions, children }: SearchPanelProps) {
  return (
    <AppCard className="grid gap-4">
      <label className="grid gap-2 text-sm font-medium text-slate-700">
        <span>{label}</span>
        <input type="search" readOnly />
      </label>
      {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
      {children}
    </AppCard>
  )
}

type EmptyStateProps = {
  title: string
  description: string
  action?: ReactNode
}

export function EmptyState({ title, description, action }: EmptyStateProps) {
  return (
    <div className="grid place-items-center gap-3 border border-dashed border-slate-300 bg-white p-8 text-center text-slate-500">
      <strong>{title}</strong>
      <p className="text-sm leading-6 text-slate-500">{description}</p>
      {action}
    </div>
  )
}

type StatCardProps = {
  label: string
  value: string | number
  detail?: string
}

export function StatCard({ label, value, detail }: StatCardProps) {
  return (
    <div className="grid gap-2 border border-slate-200 bg-white p-4 text-slate-950" role="group" aria-label={label}>
      <span className="text-sm font-medium text-slate-500">{label}</span>
      <div className="flex items-end gap-2">
        <strong className="text-2xl font-bold text-slate-950">{value}</strong>
        {detail ? <small className="text-sm text-slate-500">{detail}</small> : null}
      </div>
    </div>
  )
}

type ActionBarProps = {
  children: ReactNode
}

export function ActionBar({ children }: ActionBarProps) {
  return <div className="flex flex-wrap items-center gap-3">{children}</div>
}

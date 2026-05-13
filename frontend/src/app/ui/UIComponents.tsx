import type { ButtonHTMLAttributes, HTMLAttributes, ReactNode } from 'react'

type AppCardProps = {
  children: ReactNode
  className?: string
} & HTMLAttributes<HTMLElement>

export function AppCard({ children, className = '', ...props }: AppCardProps) {
  return (
    <section className={`app-card ${className}`.trim()} {...props}>
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
    <div className="section-header">
      <div>
        <p className="eyebrow-label">{eyebrow}</p>
        <h2 className="section-title">{title}</h2>
        {subtitle ? <p className="hero-copy">{subtitle}</p> : null}
      </div>
      {actions ? <div className="section-header-actions">{actions}</div> : null}
    </div>
  )
}

type ButtonProps = ButtonHTMLAttributes<HTMLButtonElement>

export function PrimaryButton({ className = '', ...props }: ButtonProps) {
  return <button className={`primary-button ${className}`.trim()} {...props} />
}

export function SecondaryButton({ className = '', ...props }: ButtonProps) {
  return <button className={`secondary-button ui-secondary-button ${className}`.trim()} {...props} />
}

type SearchPanelProps = {
  label: string
  placeholder: string
  actions?: ReactNode
  children?: ReactNode
}

export function SearchPanel({ label, placeholder, actions, children }: SearchPanelProps) {
  return (
    <AppCard className="search-panel">
      <label className="search-panel-label">
        <span>{label}</span>
        <input type="search" placeholder={placeholder} readOnly />
      </label>
      {actions ? <div className="action-bar">{actions}</div> : null}
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
    <div className="empty-state-panel">
      <strong>{title}</strong>
      <p className="empty-state">{description}</p>
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
    <div className="stat-card" role="group" aria-label={label}>
      <span className="stat-card-label">{label}</span>
      <div className="stat-card-body">
        <strong className="stat-card-value">{value}</strong>
        {detail ? <small className="stat-card-detail">{detail}</small> : null}
      </div>
    </div>
  )
}

type ActionBarProps = {
  children: ReactNode
}

export function ActionBar({ children }: ActionBarProps) {
  return <div className="action-bar">{children}</div>
}

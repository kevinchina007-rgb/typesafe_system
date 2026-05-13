import type { ReactNode } from 'react'

type HeroBackgroundProps = {
  title: string
  subtitle: string
  eyebrow: string
  actions?: ReactNode
  stats?: ReactNode
}

export function HeroBackground({ title, subtitle, eyebrow, actions, stats }: HeroBackgroundProps) {
  return (
    <section className="hero-background app-card workspace-overview-hero">
      <div className="hero-background-orbit hero-background-orbit-left" />
      <div className="hero-background-orbit hero-background-orbit-right" />
      <div className="hero-background-grid" />
      <div className="hero-background-routes">
        <span className="route route-one" />
        <span className="route route-two" />
        <span className="route route-three" />
      </div>
      <div className="hero-background-glow hero-background-glow-a" />
      <div className="hero-background-glow hero-background-glow-b" />

      <div className="hero-background-content workspace-overview-hero-content">
        <p className="eyebrow-label">{eyebrow}</p>
        <h1 className="hero-title">{title}</h1>
        <p className="hero-copy hero-subtitle">{subtitle}</p>
        {actions ? <div className="workspace-overview-hero-actions">{actions}</div> : null}
        {stats ? <div className="hero-stats-grid">{stats}</div> : null}
      </div>
    </section>
  )
}

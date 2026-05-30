import type { AttractionPageHeroProps } from '../../objects'

export function AttractionPageHero({ title, description }: AttractionPageHeroProps) {
  return (
    <section className="grid gap-2">
      <p className="text-sm font-bold text-slate-500">{title}</p>
      <h2 className="m-0 text-4xl font-bold leading-tight text-slate-950">{title}</h2>
      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{description}</p>
    </section>
  )
}


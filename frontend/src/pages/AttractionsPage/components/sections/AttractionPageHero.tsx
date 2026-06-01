import type { AttractionPageHeroProps } from '../../objects'

export function AttractionPageHero({ title, description }: AttractionPageHeroProps) {
  return (
    <section className="grid gap-3 rounded-2xl border border-sky-100 bg-gradient-to-br from-sky-50 via-white to-cyan-50 p-6 shadow-sm shadow-sky-100/40">
      <p className="text-sm font-bold uppercase tracking-[0.18em] text-sky-600">{title}</p>
      <h2 className="m-0 text-4xl font-black leading-tight text-slate-950">{title}</h2>
      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{description}</p>
    </section>
  )
}

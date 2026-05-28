type TrainPageHeroProps = {
  eyebrow: string
  title: string
  description: string
}

export function TrainPageHero({ eyebrow, title, description }: TrainPageHeroProps) {
  return (
    <section className="grid gap-4 border border-sky-100 bg-gradient-to-r from-white via-sky-50 to-indigo-50 p-6 text-slate-950 shadow-sm shadow-sky-100/50">
      <div className="grid gap-2">
        <p className="text-sm font-bold text-sky-600">{eyebrow}</p>
        <h2 className="m-0 text-4xl font-black leading-tight text-slate-950">{title}</h2>
      </div>
      <p className="m-0 max-w-4xl text-base leading-7 text-slate-600">{description}</p>
    </section>
  )
}

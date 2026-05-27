type HotelPageHeroProps = {
  title: string
  description: string
}

export function HotelPageHero({ title, description }: HotelPageHeroProps) {
  return (
    <section className="grid gap-2">
      <h2 className="m-0 text-3xl font-black text-slate-950">{title}</h2>
      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{description}</p>
    </section>
  )
}

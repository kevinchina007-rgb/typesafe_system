type HotelPageHeroProps = {
  title: string
  description: string
}

export function HotelPageHero({ title, description }: HotelPageHeroProps) {
  return (
    <>
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{title}</p>
          <h2>{title}</h2>
        </div>
      </div>

      <p className="hero-copy">{description}</p>
    </>
  )
}

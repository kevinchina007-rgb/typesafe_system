type AttractionPageHeroProps = {
  title: string
  description: string
}

export function AttractionPageHero({ title, description }: AttractionPageHeroProps) {
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

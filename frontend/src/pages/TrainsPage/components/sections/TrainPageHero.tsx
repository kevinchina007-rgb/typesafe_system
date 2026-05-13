type TrainPageHeroProps = {
  title: string
  description: string
}

export function TrainPageHero({ title, description }: TrainPageHeroProps) {
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

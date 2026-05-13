type FlightPageHeroProps = {
  translate: (translationKey: string) => string
}

export function FlightPageHero({ translate }: FlightPageHeroProps) {
  return (
    <div className="flight-page-header">
      <div>
        <p className="eyebrow-label">{translate('nav.flights')}</p>
        <h2>{translate('flights.title')}</h2>
        <p className="hero-copy">{translate('flights.description')}</p>
      </div>
    </div>
  )
}

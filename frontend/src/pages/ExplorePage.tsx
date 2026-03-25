type ExplorePageProps = {
  translate: (translationKey: string) => string
}

export function ExplorePage({ translate }: ExplorePageProps) {
  return (
    <section className="page-card">
      <p className="eyebrow-label">{translate('guest.badge')}</p>
      <h2>{translate('explore.title')}</h2>
      <p className="hero-copy">{translate('explore.description')}</p>
      <div className="list-surface">
        <p>{translate('guest.description')}</p>
      </div>
    </section>
  )
}

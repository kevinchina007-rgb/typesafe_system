import type { AdvertisementResponse } from '../../../lib/api-dtos/advertising'

type AdvertisementCardRailProps = {
  advertisements: AdvertisementResponse[]
  translate: (translationKey: string) => string
  onOpenAdvertisement: (advertisement: AdvertisementResponse) => void
}

export function AdvertisementCardRail({
  advertisements,
  translate,
  onOpenAdvertisement,
}: AdvertisementCardRailProps) {
  if (advertisements.length === 0) {
    return null
  }

  return (
    <section className="page-card advertising-card-rail">
      <div className="section-header">
        <div>
          <p className="eyebrow-label">{translate('advertising.deliveryEyebrow')}</p>
          <h3 className="section-title">{translate('advertising.deliveryTitle')}</h3>
        </div>
      </div>

      <div className="advertising-card-grid">
        {advertisements.map(advertisement => (
          <button
            key={advertisement.advertisementId}
            type="button"
            className="advertising-display-card"
            onClick={() => onOpenAdvertisement(advertisement)}
          >
            {advertisement.imageUrl ? (
              <img src={advertisement.imageUrl} alt={advertisement.title} className="advertising-display-image" />
            ) : null}
            <span className="advertising-display-badge">{translate('advertising.badge')}</span>
            <strong className="advertising-display-title">{advertisement.title}</strong>
            <span className="advertising-display-subtitle">{advertisement.subtitle}</span>
            <span className="advertising-display-resource">{advertisement.resourceSummaryTitle}</span>
            <span className="advertising-display-cta">{advertisement.ctaLabel}</span>
          </button>
        ))}
      </div>
    </section>
  )
}

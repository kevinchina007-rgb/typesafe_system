import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

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
  const slotNumbers = [1, 2, 3, 4]
  const advertisementsBySlot = new Map(
    advertisements
      .filter(advertisement => advertisement.slotIndex !== null)
      .map(advertisement => [advertisement.slotIndex as number, advertisement] as const),
  )

  return (
    <section className="page-card advertising-card-rail">
      <div className="section-header">
        <div>
          <p className="eyebrow-label">{translate('advertising.deliveryEyebrow')}</p>
          <h3 className="section-title">{translate('advertising.deliveryTitle')}</h3>
        </div>
      </div>

      <div className="advertising-card-grid">
        {slotNumbers.map(slotIndex => {
          const advertisement = advertisementsBySlot.get(slotIndex) ?? null
          if (!advertisement) {
            return (
              <article
                key={`advertising-slot-${slotIndex}`}
                className="advertising-display-card advertising-display-card--empty"
              >
                <span className="advertising-display-badge">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
                <strong className="advertising-display-title">{translate('advertising.slotEmpty')}</strong>
                <span className="advertising-display-subtitle">{translate('advertising.slotEmptyDescription')}</span>
              </article>
            )
          }

          return (
            <button
              key={advertisement.advertisementId}
              type="button"
              className="advertising-display-card"
              onClick={() => onOpenAdvertisement(advertisement)}
            >
              <BackendAssetImage
                assetUrl={advertisement.imageUrl}
                alt={advertisement.title}
                className="advertising-display-image"
                fallbackContent={advertisement.title}
              />
              <span className="advertising-display-badge">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
              <strong className="advertising-display-title">{advertisement.title}</strong>
              <span className="advertising-display-subtitle">{advertisement.subtitle}</span>
              <span className="advertising-display-resource">{advertisement.resourceSummaryTitle}</span>
              <span className="advertising-display-cta">{advertisement.ctaLabel}</span>
            </button>
          )
        })}
      </div>
    </section>
  )
}

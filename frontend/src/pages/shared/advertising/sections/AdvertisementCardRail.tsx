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
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.deliveryEyebrow')}</p>
          <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.deliveryTitle')}</h3>
        </div>
      </div>

      <div className="grid gap-4 lg:grid-cols-2">
        {slotNumbers.map(slotIndex => {
          const advertisement = advertisementsBySlot.get(slotIndex) ?? null
          if (!advertisement) {
            return (
              <article
                key={`advertising-slot-${slotIndex}`}
                className="grid gap-3 border border-slate-200 bg-white p-4 border-dashed bg-slate-50"
              >
                <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
                <strong className="m-0 text-xl font-bold text-slate-950">{translate('advertising.slotEmpty')}</strong>
                <span className="text-sm text-slate-500">{translate('advertising.slotEmptyDescription')}</span>
              </article>
            )
          }

          return (
            <button
              key={advertisement.advertisementId}
              type="button"
              className="grid gap-3 border border-slate-200 bg-white p-4"
              onClick={() => onOpenAdvertisement(advertisement)}
            >
              <BackendAssetImage
                assetUrl={advertisement.imageUrl}
                alt={advertisement.title}
                className="aspect-video w-full object-cover"
                fallbackContent={advertisement.title}
              />
              <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
              <strong className="m-0 text-xl font-bold text-slate-950">{advertisement.title}</strong>
              <span className="text-sm text-slate-500">{advertisement.subtitle}</span>
              <span className="text-sm text-slate-600">{advertisement.resourceSummaryTitle}</span>
              <span className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55">{advertisement.ctaLabel}</span>
            </button>
          )
        })}
      </div>
    </section>
  )
}

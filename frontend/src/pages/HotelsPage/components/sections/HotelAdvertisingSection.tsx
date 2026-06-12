import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { HotelAdvertisingSectionProps } from '@/pages/HotelsPage/objects'

// 酒店广告区，负责展示推荐广告和当前选中的广告。
export function HotelAdvertisingSection({
  featuredAdvertisement,
  selectedAdvertisement,
  translate,
  onOpenAdvertisement,
}: HotelAdvertisingSectionProps) {
  return (
    <section className="grid gap-4 border border-indigo-100 bg-gradient-to-r from-white via-indigo-50 to-cyan-50 p-6 text-slate-950 shadow-lg shadow-indigo-100/50">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-black uppercase tracking-[0.18em] text-indigo-600">{translate('advertising.deliveryEyebrow')}</p>
          <h3 className="m-0 text-3xl font-black leading-tight text-slate-950">{translate('advertising.deliveryTitle')}</h3>
        </div>
      </div>

      {featuredAdvertisement ? (
        <div className="grid gap-4 border border-sky-100 bg-white p-4 shadow-sm shadow-sky-100/40 md:grid-cols-[18rem_1fr]">
          <button
            type="button"
            className="grid gap-3 text-left"
            onClick={() => void onOpenAdvertisement(featuredAdvertisement.advertisementId)}
          >
            <BackendAssetImage
              assetUrl={featuredAdvertisement.imageUrl}
              alt={featuredAdvertisement.title}
              className="aspect-[16/10] w-full object-cover"
              fallbackContent={featuredAdvertisement.title}
            />
            <strong className="text-2xl font-black text-slate-950">{featuredAdvertisement.title}</strong>
          </button>

          <div className="grid gap-2">
            <span className="inline-flex w-fit bg-indigo-600 px-2 py-1 text-xs font-bold text-white">
              {translate('advertising.deliveryEyebrow')}
            </span>
            <p className="text-base font-semibold text-slate-700">{featuredAdvertisement.subtitle}</p>
            <p className="text-sm leading-6 text-slate-600">{featuredAdvertisement.description}</p>
            <span className="text-sm font-bold text-slate-500">{featuredAdvertisement.resourceSummaryTitle}</span>
            <button
              type="button"
              className="mt-2 inline-flex min-h-11 w-fit items-center justify-center border border-transparent bg-gradient-to-r from-sky-500 to-cyan-500 px-4 py-2 text-sm font-black text-white transition hover:from-sky-600 hover:to-cyan-600"
              onClick={() => void onOpenAdvertisement(featuredAdvertisement.advertisementId)}
            >
              {featuredAdvertisement.ctaLabel}
            </button>
          </div>
        </div>
      ) : (
        <div className="border border-dashed border-slate-200 bg-slate-50 p-6 text-sm leading-6 text-slate-500">
          {translate('advertising.slotEmptyDescription')}
        </div>
      )}

      {selectedAdvertisement ? (
        <div className="grid gap-2 border border-sky-100 bg-white p-4 text-sm text-slate-600 shadow-sm shadow-sky-100/40">
          <p className="text-sm font-black uppercase tracking-[0.16em] text-sky-600">{translate('advertising.selectedEyebrow')}</p>
          <p>{selectedAdvertisement.title}</p>
          <p>{selectedAdvertisement.subtitle}</p>
          <p>{selectedAdvertisement.description}</p>
        </div>
      ) : null}
    </section>
  )
}

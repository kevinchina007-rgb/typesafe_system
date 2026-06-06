import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { ChevronLeft, ChevronRight } from 'lucide-react'
import { useEffect, useMemo, useState } from 'react'

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
  const orderedAdvertisements = useMemo(
    () =>
      [...advertisements].sort((left, right) => {
        const slotDelta = (left.slotIndex ?? 999) - (right.slotIndex ?? 999)
        if (slotDelta !== 0) return slotDelta
        return Date.parse(right.updatedAt) - Date.parse(left.updatedAt)
      }),
    [advertisements],
  )
  const [activeIndex, setActiveIndex] = useState(0)
  const activeAdvertisement = orderedAdvertisements[activeIndex] ?? null

  useEffect(() => {
    setActiveIndex(0)
  }, [orderedAdvertisements.length])

  useEffect(() => {
    if (orderedAdvertisements.length <= 1) {
      return undefined
    }

    const timerId = window.setInterval(() => {
      setActiveIndex(currentIndex => (currentIndex + 1) % orderedAdvertisements.length)
    }, 5000)
    return () => window.clearInterval(timerId)
  }, [orderedAdvertisements.length])

  const showPrevious = () => {
    setActiveIndex(currentIndex =>
      currentIndex === 0 ? orderedAdvertisements.length - 1 : currentIndex - 1,
    )
  }

  const showNext = () => {
    setActiveIndex(currentIndex => (currentIndex + 1) % orderedAdvertisements.length)
  }

  return (
    <section className="grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      {activeAdvertisement ? (
        <div className="grid gap-3">
          <div className="group relative overflow-hidden border border-slate-200 bg-white">
            <button
              type="button"
              className="block w-full text-left"
              onClick={() => onOpenAdvertisement(activeAdvertisement)}
            >
              <BackendAssetImage
                assetUrl={activeAdvertisement.imageUrl}
                alt={activeAdvertisement.title}
                className="aspect-[4/1] w-full object-cover transition group-hover:scale-[1.01]"
                fallbackContent={activeAdvertisement.title}
              />
              <span className="absolute left-3 top-3 inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">{`${translate('advertising.slotBadge')} ${activeAdvertisement.slotIndex ?? activeIndex + 1}`}</span>
            </button>

            {orderedAdvertisements.length > 1 ? (
              <>
                <button
                  type="button"
                  className="absolute left-3 top-1/2 inline-flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-white/90 text-slate-950 shadow-sm transition hover:bg-white"
                  aria-label="上一张广告"
                  onClick={showPrevious}
                >
                  <ChevronLeft className="h-5 w-5" aria-hidden="true" />
                </button>
                <button
                  type="button"
                  className="absolute right-3 top-1/2 inline-flex h-10 w-10 -translate-y-1/2 items-center justify-center rounded-full bg-white/90 text-slate-950 shadow-sm transition hover:bg-white"
                  aria-label="下一张广告"
                  onClick={showNext}
                >
                  <ChevronRight className="h-5 w-5" aria-hidden="true" />
                </button>
              </>
            ) : null}
          </div>

          {orderedAdvertisements.length > 1 ? (
            <div className="flex items-center justify-center gap-2">
              {orderedAdvertisements.map((advertisement, index) => (
                <button
                  key={advertisement.advertisementId}
                  type="button"
                  className={`h-2.5 w-8 border transition ${
                    index === activeIndex ? 'border-slate-950 bg-slate-950' : 'border-slate-300 bg-white'
                  }`}
                  aria-label={`切换到第 ${index + 1} 张广告`}
                  onClick={() => setActiveIndex(index)}
                />
              ))}
            </div>
          ) : null}
        </div>
      ) : (
        <article className="grid gap-3 border border-dashed border-slate-200 bg-slate-50 p-4">
          <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">{`${translate('advertising.slotBadge')} 1`}</span>
          <strong className="m-0 text-xl font-bold text-slate-950">{translate('advertising.slotEmpty')}</strong>
          <span className="text-sm text-slate-500">{translate('advertising.slotEmptyDescription')}</span>
        </article>
      )}
    </section>
  )
}

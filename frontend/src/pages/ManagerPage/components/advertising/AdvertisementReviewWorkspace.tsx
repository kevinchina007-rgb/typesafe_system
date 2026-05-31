import { useEffect, useMemo, useState } from 'react'

import { useAdvertisingStore } from '@/app/stores/advertising-store'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

type AdvertisingBusinessModule = 'flight' | 'hotel' | 'train' | 'attraction'
type PlacementValue = 'FlightBookingPage' | 'HotelBookingPage' | 'TrainBookingPage' | 'AttractionBookingPage'

type AdvertisementReviewWorkspaceProps = {
  businessModule: AdvertisingBusinessModule
  translate: (translationKey: string) => string
}

const moduleConfig: Record<AdvertisingBusinessModule, {
  placement: PlacementValue
  ownerType: string
  targetResourceType: string
  titleKey: string
  descriptionKey: string
}> = {
  flight: {
    placement: 'FlightBookingPage',
    ownerType: 'Airline',
    targetResourceType: 'Flight',
    titleKey: 'advertising.slotBoard.flightTitle',
    descriptionKey: 'advertising.slotBoard.flightDescription',
  },
  hotel: {
    placement: 'HotelBookingPage',
    ownerType: 'HotelManager',
    targetResourceType: 'Hotel',
    titleKey: 'advertising.slotBoard.hotelTitle',
    descriptionKey: 'advertising.slotBoard.hotelDescription',
  },
  train: {
    placement: 'TrainBookingPage',
    ownerType: 'Train',
    targetResourceType: 'Train',
    titleKey: 'advertising.slotBoard.trainTitle',
    descriptionKey: 'advertising.slotBoard.trainDescription',
  },
  attraction: {
    placement: 'AttractionBookingPage',
    ownerType: 'AttractionManager',
    targetResourceType: 'Attraction',
    titleKey: 'advertising.slotBoard.attractionTitle',
    descriptionKey: 'advertising.slotBoard.attractionDescription',
  },
}

function normalizeReviewedAdvertisements(advertisements: AdvertisementResponse[]) {
  const byId = new Map<string, AdvertisementResponse>()
  advertisements.forEach(advertisement => byId.set(advertisement.advertisementId, advertisement))
  return [...byId.values()]
}

function advertisementBelongsToModule(advertisement: AdvertisementResponse, config: (typeof moduleConfig)[AdvertisingBusinessModule]) {
  return advertisement.placement === config.placement ||
    advertisement.targetResourceType === config.targetResourceType ||
    advertisement.ownerType === config.ownerType
}

export function AdvertisementReviewWorkspace({ businessModule, translate }: AdvertisementReviewWorkspaceProps) {
  const pendingReviewAdvertisements = useAdvertisingStore(state => state.pendingReviewAdvertisements)
  const reviewedAdvertisements = useAdvertisingStore(state => state.reviewedAdvertisements)
  const loadPendingReviewAdvertisements = useAdvertisingStore(state => state.loadPendingReviewAdvertisements)
  const loadReviewedAdvertisements = useAdvertisingStore(state => state.loadReviewedAdvertisements)
  const approveAdvertisement = useAdvertisingStore(state => state.approveAdvertisement)
  const rejectAdvertisement = useAdvertisingStore(state => state.rejectAdvertisement)
  const assignAdvertisementSlot = useAdvertisingStore(state => state.assignAdvertisementSlot)

  const [draggingAdvertisementId, setDraggingAdvertisementId] = useState<string | null>(null)
  const [reviewNoteByAdvertisementId, setReviewNoteByAdvertisementId] = useState<Record<string, string>>({})

  useEffect(() => {
    void Promise.all([loadPendingReviewAdvertisements(), loadReviewedAdvertisements()])
  }, [loadPendingReviewAdvertisements, loadReviewedAdvertisements])

  const config = moduleConfig[businessModule]
  const pendingModuleAdvertisements = useMemo(
    () => pendingReviewAdvertisements.filter(advertisement => advertisementBelongsToModule(advertisement, config)),
    [config, pendingReviewAdvertisements],
  )
  const normalizedReviewedAdvertisements = useMemo(
    () => normalizeReviewedAdvertisements(reviewedAdvertisements).filter(advertisement => advertisementBelongsToModule(advertisement, config)),
    [config, reviewedAdvertisements],
  )
  const approvedAdvertisements = useMemo(
    () => normalizedReviewedAdvertisements.filter(advertisement => advertisement.reviewStatus === 'Approved' && advertisement.placement === config.placement),
    [config.placement, normalizedReviewedAdvertisements],
  )

  async function handleAssignSlot(advertisementId: string, slotIndex: number) {
    await assignAdvertisementSlot(advertisementId, { slotIndex })
    setDraggingAdvertisementId(null)
  }

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.reviewEyebrow')}</p>
          <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.reviewTitle')}</h2>
        </div>

        {pendingModuleAdvertisements.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('advertising.reviewEmpty')}</p>
        ) : (
          <div className="grid gap-3">
            {pendingModuleAdvertisements.map(advertisement => (
              <article key={advertisement.advertisementId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 md:grid-cols-[180px_1fr]">
                {advertisement.imageUrl ? (
                  <img src={advertisement.imageUrl} alt={advertisement.title} className="aspect-video w-full object-cover" />
                ) : (
                  <div className="flex aspect-video items-center justify-center bg-slate-100 text-sm font-bold text-slate-500">{translate('advertising.badge')}</div>
                )}
                <div className="grid gap-3">
                  <div className="grid gap-1">
                    <strong className="text-xl">{advertisement.title}</strong>
                    <span className="text-sm text-slate-600">{advertisement.subtitle}</span>
                    <span className="text-sm text-slate-500">{advertisement.resourceSummaryTitle}</span>
                  </div>
                  <label>
                    {translate('advertising.reviewNote')}
                    <textarea
                      rows={2}
                      value={reviewNoteByAdvertisementId[advertisement.advertisementId] ?? ''}
                      onChange={event => setReviewNoteByAdvertisementId(current => ({ ...current, [advertisement.advertisementId]: event.target.value }))}
                    />
                  </label>
                  <div className="flex flex-wrap items-center gap-3">
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-black bg-black px-4 py-2 text-sm font-semibold text-white shadow-none transition hover:bg-white hover:text-black"
                      onClick={() => {
                        void approveAdvertisement(advertisement.advertisementId, {
                          reviewNote: reviewNoteByAdvertisementId[advertisement.advertisementId] ?? null,
                        })
                      }}
                    >
                      {translate('advertising.approve')}
                    </button>
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
                      onClick={() => {
                        void rejectAdvertisement(advertisement.advertisementId, {
                          reviewNote: reviewNoteByAdvertisementId[advertisement.advertisementId] ?? null,
                        })
                      }}
                    >
                      {translate('advertising.reject')}
                    </button>
                  </div>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <AdvertisementPlacementBoard
        placement={config.placement}
        title={translate(config.titleKey)}
        description={translate(config.descriptionKey)}
        approvedAdvertisements={approvedAdvertisements}
        draggingAdvertisementId={draggingAdvertisementId}
        translate={translate}
        onAssignSlot={handleAssignSlot}
        onDragStart={setDraggingAdvertisementId}
        onDragEnd={() => setDraggingAdvertisementId(null)}
      />

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.historyEyebrow')}</p>
          <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.historyTitle')}</h3>
        </div>
        {normalizedReviewedAdvertisements.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('advertising.historyEmpty')}</p>
        ) : (
          <div className="grid gap-3 md:grid-cols-2">
            {normalizedReviewedAdvertisements.map(advertisement => (
              <article key={advertisement.advertisementId} className="grid gap-3 border border-slate-200 bg-white p-4">
                <strong>{advertisement.title}</strong>
                <span className="text-sm text-slate-500">{advertisement.resourceSummaryTitle}</span>
                <span className="text-sm font-semibold text-slate-700">
                  {advertisement.reviewStatus} / {advertisement.slotIndex ? `${translate('advertising.slotBadge')} ${advertisement.slotIndex}` : translate('advertising.slotUnassigned')}
                </span>
              </article>
            ))}
          </div>
        )}
      </section>
    </section>
  )
}

type AdvertisementPlacementBoardProps = {
  placement: PlacementValue
  title: string
  description: string
  approvedAdvertisements: AdvertisementResponse[]
  draggingAdvertisementId: string | null
  translate: (translationKey: string) => string
  onAssignSlot: (advertisementId: string, slotIndex: number) => Promise<void>
  onDragStart: (advertisementId: string) => void
  onDragEnd: () => void
}

function AdvertisementPlacementBoard({
  title,
  description,
  approvedAdvertisements,
  draggingAdvertisementId,
  translate,
  onAssignSlot,
  onDragStart,
  onDragEnd,
}: AdvertisementPlacementBoardProps) {
  const assignedBySlot = new Map(approvedAdvertisements.filter(advertisement => advertisement.slotIndex !== null).map(advertisement => [advertisement.slotIndex, advertisement]))
  const unassignedAdvertisements = approvedAdvertisements.filter(advertisement => advertisement.slotIndex === null)

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div>
        <p className="text-sm font-bold text-slate-500">{translate('advertising.slotBoard.eyebrow')}</p>
        <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{title}</h3>
        <p className="m-0 max-w-3xl text-sm leading-6 text-slate-500">{description}</p>
      </div>

      <div className="grid gap-3 md:grid-cols-4">
        {[1, 2, 3, 4].map(slotIndex => {
          const assignedAdvertisement = assignedBySlot.get(slotIndex) ?? null
          const isDropTarget = draggingAdvertisementId !== null

          return (
            <button
              key={slotIndex}
              type="button"
              className={`grid min-h-64 gap-3 border border-dashed p-4 text-left transition ${isDropTarget ? 'border-sky-400 bg-sky-50' : 'border-slate-200 bg-slate-50'}`}
              onDragOver={event => {
                if (draggingAdvertisementId) event.preventDefault()
              }}
              onDrop={event => {
                event.preventDefault()
                if (draggingAdvertisementId) void onAssignSlot(draggingAdvertisementId, slotIndex)
              }}
              onClick={() => {
                const firstAvailable = unassignedAdvertisements[0]
                if (firstAvailable) void onAssignSlot(firstAvailable.advertisementId, slotIndex)
              }}
            >
              <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
              {assignedAdvertisement ? (
                <>
                  {assignedAdvertisement.imageUrl ? <img src={assignedAdvertisement.imageUrl} alt={assignedAdvertisement.title} className="aspect-video w-full object-cover" /> : null}
                  <strong className="text-lg">{assignedAdvertisement.title}</strong>
                  <span className="text-sm text-slate-500">{assignedAdvertisement.resourceSummaryTitle}</span>
                </>
              ) : (
                <>
                  <strong className="text-lg">{translate('advertising.slotEmpty')}</strong>
                  <span className="text-sm text-slate-500">{translate('advertising.slotEmptyDescription')}</span>
                </>
              )}
            </button>
          )
        })}
      </div>

      <div className="grid gap-3">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.approvedPoolEyebrow')}</p>
          <h4 className="m-0 text-lg font-bold text-slate-950">{translate('advertising.approvedPoolTitle')}</h4>
        </div>
        {approvedAdvertisements.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('advertising.approvedPoolEmpty')}</p>
        ) : (
          <div className="grid gap-3 md:grid-cols-2">
            {approvedAdvertisements.map(advertisement => (
              <article
                key={advertisement.advertisementId}
                className="grid cursor-grab gap-3 border border-slate-200 bg-white p-4"
                draggable
                onDragStart={() => onDragStart(advertisement.advertisementId)}
                onDragEnd={onDragEnd}
              >
                <strong>{advertisement.title}</strong>
                <span className="text-sm text-slate-500">{advertisement.resourceSummaryTitle}</span>
                <span className="text-sm font-semibold text-slate-700">
                  {advertisement.slotIndex ? `${translate('advertising.slotBadge')} ${advertisement.slotIndex}` : translate('advertising.slotUnassigned')}
                </span>
              </article>
            ))}
          </div>
        )}
        {unassignedAdvertisements.length > 0 ? (
          <p className="m-0 text-sm leading-6 text-amber-700">{translate('advertising.unassignedWarning')} {unassignedAdvertisements.map(advertisement => advertisement.title).join(' / ')}</p>
        ) : null}
      </div>
    </section>
  )
}

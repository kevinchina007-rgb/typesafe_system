import { useEffect, useMemo, useState } from 'react'

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { useAdvertisingStore } from '@/app/stores/advertising-store'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

type AdvertisementReviewWorkspaceProps = {
  translate: (translationKey: string) => string
}

type PlacementValue = 'HotelBookingPage' | 'AttractionBookingPage'

const slotNumbers = [1, 2, 3, 4] as const

function normalizeReviewedAdvertisements(advertisements: AdvertisementResponse[]) {
  return [...advertisements].sort((left, right) => {
    if (left.placement !== right.placement) {
      return left.placement.localeCompare(right.placement)
    }

    if ((left.slotIndex ?? 99) !== (right.slotIndex ?? 99)) {
      return (left.slotIndex ?? 99) - (right.slotIndex ?? 99)
    }

    return right.priority - left.priority
  })
}

export function AdvertisementReviewWorkspace({ translate }: AdvertisementReviewWorkspaceProps) {
  const pendingReviewAdvertisements = useAdvertisingStore(state => state.pendingReviewAdvertisements)
  const reviewedAdvertisements = useAdvertisingStore(state => state.reviewedAdvertisements)
  const loadPendingReviewAdvertisements = useAdvertisingStore(state => state.loadPendingReviewAdvertisements)
  const loadReviewedAdvertisements = useAdvertisingStore(state => state.loadReviewedAdvertisements)
  const approveAdvertisement = useAdvertisingStore(state => state.approveAdvertisement)
  const rejectAdvertisement = useAdvertisingStore(state => state.rejectAdvertisement)
  const assignAdvertisementSlot = useAdvertisingStore(state => state.assignAdvertisementSlot)
  const [reviewNotes, setReviewNotes] = useState<Record<string, string>>({})
  const [draggingAdvertisementId, setDraggingAdvertisementId] = useState<string | null>(null)
  const [slotDropTarget, setSlotDropTarget] = useState<string | null>(null)

  useEffect(() => {
    void Promise.all([loadPendingReviewAdvertisements(), loadReviewedAdvertisements()])
  }, [loadPendingReviewAdvertisements, loadReviewedAdvertisements])

  const normalizedReviewedAdvertisements = useMemo(
    () => normalizeReviewedAdvertisements(reviewedAdvertisements),
    [reviewedAdvertisements],
  )

  const hotelApprovedAdvertisements = useMemo(
    () =>
      normalizedReviewedAdvertisements.filter(
        advertisement =>
          advertisement.reviewStatus === 'Approved' && advertisement.placement === 'HotelBookingPage',
      ),
    [normalizedReviewedAdvertisements],
  )

  const attractionApprovedAdvertisements = useMemo(
    () =>
      normalizedReviewedAdvertisements.filter(
        advertisement =>
          advertisement.reviewStatus === 'Approved' && advertisement.placement === 'AttractionBookingPage',
      ),
    [normalizedReviewedAdvertisements],
  )

  const handleAssignSlot = async (advertisementId: string, slotIndex: number) => {
    await assignAdvertisementSlot(advertisementId, { slotIndex })
    setDraggingAdvertisementId(null)
    setSlotDropTarget(null)
  }

  return (
    <section className="grid gap-5">
      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('advertising.reviewEyebrow')}</p>
            <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.reviewTitle')}</h2>
          </div>
        </div>

        {pendingReviewAdvertisements.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('advertising.reviewEmpty')}</p>
        ) : (
          <div className="grid gap-3">
            {pendingReviewAdvertisements.map(advertisement => (
              <article key={advertisement.advertisementId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-3 border border-slate-200 bg-white p-4">
                <BackendAssetImage
                  assetUrl={advertisement.imageUrl}
                  alt={advertisement.title}
                  className="aspect-video w-full object-cover"
                  fallbackContent={advertisement.title}
                />
                <div className="grid gap-1">
                  <strong>{advertisement.title}</strong>
                  <span>{advertisement.subtitle}</span>
                  <span>{`${advertisement.ownerDisplayName} 路 ${advertisement.resourceSummaryTitle}`}</span>
                  <span>{`${advertisement.placement} 路 P${advertisement.priority}`}</span>
                </div>
                <label className="grid gap-2 text-sm font-medium text-slate-600">
                  {translate('advertising.reviewNote')}
                  <textarea
                    rows={3}
                    value={reviewNotes[advertisement.advertisementId] ?? ''}
                    onChange={event =>
                      setReviewNotes(current => ({ ...current, [advertisement.advertisementId]: event.target.value }))
                    }
                  />
                </label>
                <div className="flex flex-wrap items-center gap-3">
                  <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                    type="button"
                    onClick={() =>
                      void approveAdvertisement(advertisement.advertisementId, {
                        reviewNote: reviewNotes[advertisement.advertisementId]?.trim() || null,
                      })
                    }
                  >
                    {translate('advertising.approve')}
                  </button>
                  <button
                    type="button"
                    className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                    onClick={() =>
                      void rejectAdvertisement(advertisement.advertisementId, {
                        reviewNote: reviewNotes[advertisement.advertisementId]?.trim() || null,
                      })
                    }
                  >
                    {translate('advertising.reject')}
                  </button>
                </div>
              </article>
            ))}
          </div>
        )}
      </section>

      <AdvertisementPlacementBoard
        placement="HotelBookingPage"
        title={translate('advertising.slotBoard.hotelTitle')}
        description={translate('advertising.slotBoard.hotelDescription')}
        approvedAdvertisements={hotelApprovedAdvertisements}
        draggingAdvertisementId={draggingAdvertisementId}
        slotDropTarget={slotDropTarget}
        translate={translate}
        onAssignSlot={handleAssignSlot}
        onDragStart={setDraggingAdvertisementId}
        onDragEnd={() => {
          setDraggingAdvertisementId(null)
          setSlotDropTarget(null)
        }}
        onSlotDragEnter={setSlotDropTarget}
        onSlotDragLeave={slotKey => {
          setSlotDropTarget(current => (current === slotKey ? null : current))
        }}
      />

      <AdvertisementPlacementBoard
        placement="AttractionBookingPage"
        title={translate('advertising.slotBoard.attractionTitle')}
        description={translate('advertising.slotBoard.attractionDescription')}
        approvedAdvertisements={attractionApprovedAdvertisements}
        draggingAdvertisementId={draggingAdvertisementId}
        slotDropTarget={slotDropTarget}
        translate={translate}
        onAssignSlot={handleAssignSlot}
        onDragStart={setDraggingAdvertisementId}
        onDragEnd={() => {
          setDraggingAdvertisementId(null)
          setSlotDropTarget(null)
        }}
        onSlotDragEnter={setSlotDropTarget}
        onSlotDragLeave={slotKey => {
          setSlotDropTarget(current => (current === slotKey ? null : current))
        }}
      />

      <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
        <div className="flex flex-wrap items-start justify-between gap-4">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('advertising.historyEyebrow')}</p>
            <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{translate('advertising.historyTitle')}</h3>
          </div>
        </div>

        {normalizedReviewedAdvertisements.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('advertising.historyEmpty')}</p>
        ) : (
          <div className="grid gap-3">
            {normalizedReviewedAdvertisements.map(advertisement => (
              <article key={advertisement.advertisementId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50 grid gap-3 border border-slate-200 bg-white p-4">
                <BackendAssetImage
                  assetUrl={advertisement.imageUrl}
                  alt={advertisement.title}
                  className="aspect-video w-full object-cover"
                  fallbackContent={advertisement.title}
                />
                <div className="grid gap-1">
                  <strong>{advertisement.title}</strong>
                  <span>{advertisement.resourceSummaryTitle}</span>
                  <span>{`${advertisement.reviewStatus} / ${advertisement.deliveryStatus}`}</span>
                  <span>
                    {advertisement.slotIndex
                      ? `${translate('advertising.slotBadge')} ${advertisement.slotIndex}`
                      : translate('advertising.slotUnassigned')}
                  </span>
                  {advertisement.reviews[0]?.reviewNote ? (
                    <span className="text-sm font-medium text-slate-500">{advertisement.reviews[0].reviewNote}</span>
                  ) : null}
                </div>
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
  slotDropTarget: string | null
  translate: (translationKey: string) => string
  onAssignSlot: (advertisementId: string, slotIndex: number) => void | Promise<void>
  onDragStart: (advertisementId: string) => void
  onDragEnd: () => void
  onSlotDragEnter: (slotKey: string) => void
  onSlotDragLeave: (slotKey: string) => void
}

function AdvertisementPlacementBoard({
  placement,
  title,
  description,
  approvedAdvertisements,
  draggingAdvertisementId,
  slotDropTarget,
  translate,
  onAssignSlot,
  onDragStart,
  onDragEnd,
  onSlotDragEnter,
  onSlotDragLeave,
}: AdvertisementPlacementBoardProps) {
  const assignedBySlot = new Map(
    approvedAdvertisements
      .filter(advertisement => advertisement.slotIndex !== null)
      .map(advertisement => [advertisement.slotIndex as number, advertisement] as const),
  )
  const unassignedAdvertisements = approvedAdvertisements.filter(advertisement => advertisement.slotIndex === null)

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40 grid gap-4">
      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.slotBoard.eyebrow')}</p>
          <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">{title}</h3>
        </div>
      </div>
      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{description}</p>

      <div className="grid gap-4 lg:grid-cols-2">
        {slotNumbers.map(slotIndex => {
          const slotKey = `${placement}-${slotIndex}`
          const assignedAdvertisement = assignedBySlot.get(slotIndex) ?? null
          const isDropTarget = slotDropTarget === slotKey

          return (
            <article
              key={slotKey}
              className={`grid gap-3 border border-slate-200 bg-white p-4 border-dashed${assignedAdvertisement ? '' : ' border-dashed bg-slate-50'}${isDropTarget ? ' border-sky-400 bg-sky-50' : ''}`}
              onDragOver={event => {
                if (!draggingAdvertisementId) {
                  return
                }
                event.preventDefault()
              }}
              onDragEnter={event => {
                if (!draggingAdvertisementId) {
                  return
                }
                event.preventDefault()
                onSlotDragEnter(slotKey)
              }}
              onDragLeave={() => onSlotDragLeave(slotKey)}
              onDrop={event => {
                if (!draggingAdvertisementId) {
                  return
                }
                event.preventDefault()
                void onAssignSlot(draggingAdvertisementId, slotIndex)
              }}
            >
              <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
              {assignedAdvertisement ? (
                <>
                  <BackendAssetImage
                    assetUrl={assignedAdvertisement.imageUrl}
                    alt={assignedAdvertisement.title}
                    className="aspect-video w-full object-cover"
                    fallbackContent={assignedAdvertisement.title}
                  />
                  <strong className="m-0 text-xl font-bold text-slate-950">{assignedAdvertisement.title}</strong>
                  <span className="text-sm text-slate-500">{assignedAdvertisement.subtitle}</span>
                  <span className="text-sm text-slate-600">{assignedAdvertisement.resourceSummaryTitle}</span>
                  <span className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55">{assignedAdvertisement.ctaLabel}</span>
                </>
              ) : (
                <>
                  <strong className="m-0 text-xl font-bold text-slate-950">{translate('advertising.slotEmpty')}</strong>
                  <span className="text-sm text-slate-500">{translate('advertising.slotEmptyDescription')}</span>
                </>
              )}
              <span className="text-sm text-slate-500">
                {draggingAdvertisementId
                  ? translate('advertising.slotDropHint')
                  : translate('advertising.slotDropIdleHint')}
              </span>
            </article>
          )
        })}
      </div>

      <div className="flex flex-wrap items-start justify-between gap-4">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('advertising.approvedPoolEyebrow')}</p>
          <h4 className="m-0 text-2xl font-bold leading-tight text-slate-950 text-lg font-bold text-slate-950">{translate('advertising.approvedPoolTitle')}</h4>
        </div>
      </div>

      {approvedAdvertisements.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('advertising.approvedPoolEmpty')}</p>
      ) : (
        <div className="grid gap-4 lg:grid-cols-2">
          {approvedAdvertisements.map(advertisement => (
            <button
              key={advertisement.advertisementId}
              type="button"
              draggable
              className={`grid gap-3 border border-slate-200 bg-white p-4 cursor-grab${draggingAdvertisementId === advertisement.advertisementId ? ' is-dragging' : ''}`}
              onDragStart={event => {
                event.dataTransfer.effectAllowed = 'move'
                event.dataTransfer.setData('text/plain', advertisement.advertisementId)
                onDragStart(advertisement.advertisementId)
              }}
              onDragEnd={onDragEnd}
              onClick={() => {
                const firstEmptySlot = slotNumbers.find(slotIndex => !assignedBySlot.has(slotIndex))
                if (firstEmptySlot) {
                  void onAssignSlot(advertisement.advertisementId, firstEmptySlot)
                }
              }}
            >
              <BackendAssetImage
                assetUrl={advertisement.imageUrl}
                alt={advertisement.title}
                className="aspect-video w-full object-cover"
                fallbackContent={advertisement.title}
              />
              <span className="inline-flex w-fit bg-slate-950 px-2 py-1 text-xs font-bold text-white">
                {advertisement.slotIndex
                  ? `${translate('advertising.slotBadge')} ${advertisement.slotIndex}`
                  : translate('advertising.slotUnassigned')}
              </span>
              <strong className="m-0 text-xl font-bold text-slate-950">{advertisement.title}</strong>
              <span className="text-sm text-slate-500">{advertisement.subtitle}</span>
              <span className="text-sm text-slate-600">{advertisement.resourceSummaryTitle}</span>
              <span className="text-sm text-slate-500">
                {advertisement.slotIndex
                  ? translate('advertising.slotDragMoveHint')
                  : translate('advertising.slotDragAssignHint')}
              </span>
            </button>
          ))}
        </div>
      )}

      {unassignedAdvertisements.length > 0 ? (
        <p className="text-sm font-medium text-slate-500">
          {translate('advertising.unassignedWarning')} {unassignedAdvertisements.map(advertisement => advertisement.title).join(' 路 ')}
        </p>
      ) : null}
    </section>
  )
}

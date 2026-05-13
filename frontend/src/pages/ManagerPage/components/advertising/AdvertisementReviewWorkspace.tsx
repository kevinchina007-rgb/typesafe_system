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
    <section className="page-stack">
      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('advertising.reviewEyebrow')}</p>
            <h2 className="section-title">{translate('advertising.reviewTitle')}</h2>
          </div>
        </div>

        {pendingReviewAdvertisements.length === 0 ? (
          <p className="empty-state">{translate('advertising.reviewEmpty')}</p>
        ) : (
          <div className="advertising-admin-list">
            {pendingReviewAdvertisements.map(advertisement => (
              <article key={advertisement.advertisementId} className="panel-card advertising-admin-card">
                <BackendAssetImage
                  assetUrl={advertisement.imageUrl}
                  alt={advertisement.title}
                  className="advertising-admin-image"
                  fallbackContent={advertisement.title}
                />
                <div className="advertising-admin-copy">
                  <strong>{advertisement.title}</strong>
                  <span>{advertisement.subtitle}</span>
                  <span>{`${advertisement.ownerDisplayName} 路 ${advertisement.resourceSummaryTitle}`}</span>
                  <span>{`${advertisement.placement} 路 P${advertisement.priority}`}</span>
                </div>
                <label className="advertising-review-note">
                  {translate('advertising.reviewNote')}
                  <textarea
                    rows={3}
                    value={reviewNotes[advertisement.advertisementId] ?? ''}
                    onChange={event =>
                      setReviewNotes(current => ({ ...current, [advertisement.advertisementId]: event.target.value }))
                    }
                  />
                </label>
                <div className="advertising-admin-actions">
                  <button
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
                    className="secondary-button"
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

      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('advertising.historyEyebrow')}</p>
            <h3 className="section-title">{translate('advertising.historyTitle')}</h3>
          </div>
        </div>

        {normalizedReviewedAdvertisements.length === 0 ? (
          <p className="empty-state">{translate('advertising.historyEmpty')}</p>
        ) : (
          <div className="advertising-admin-list">
            {normalizedReviewedAdvertisements.map(advertisement => (
              <article key={advertisement.advertisementId} className="panel-card advertising-admin-card">
                <BackendAssetImage
                  assetUrl={advertisement.imageUrl}
                  alt={advertisement.title}
                  className="advertising-admin-image"
                  fallbackContent={advertisement.title}
                />
                <div className="advertising-admin-copy">
                  <strong>{advertisement.title}</strong>
                  <span>{advertisement.resourceSummaryTitle}</span>
                  <span>{`${advertisement.reviewStatus} / ${advertisement.deliveryStatus}`}</span>
                  <span>
                    {advertisement.slotIndex
                      ? `${translate('advertising.slotBadge')} ${advertisement.slotIndex}`
                      : translate('advertising.slotUnassigned')}
                  </span>
                  {advertisement.reviews[0]?.reviewNote ? (
                    <span className="detail-label">{advertisement.reviews[0].reviewNote}</span>
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
    <section className="page-card advertising-placement-board">
      <div className="section-header">
        <div>
          <p className="eyebrow-label">{translate('advertising.slotBoard.eyebrow')}</p>
          <h3 className="section-title">{title}</h3>
        </div>
      </div>
      <p className="hero-copy">{description}</p>

      <div className="advertising-card-grid">
        {slotNumbers.map(slotIndex => {
          const slotKey = `${placement}-${slotIndex}`
          const assignedAdvertisement = assignedBySlot.get(slotIndex) ?? null
          const isDropTarget = slotDropTarget === slotKey

          return (
            <article
              key={slotKey}
              className={`advertising-display-card advertising-display-card--slot${assignedAdvertisement ? '' : ' advertising-display-card--empty'}${isDropTarget ? ' is-drop-target' : ''}`}
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
              <span className="advertising-display-badge">{`${translate('advertising.slotBadge')} ${slotIndex}`}</span>
              {assignedAdvertisement ? (
                <>
                  <BackendAssetImage
                    assetUrl={assignedAdvertisement.imageUrl}
                    alt={assignedAdvertisement.title}
                    className="advertising-display-image"
                    fallbackContent={assignedAdvertisement.title}
                  />
                  <strong className="advertising-display-title">{assignedAdvertisement.title}</strong>
                  <span className="advertising-display-subtitle">{assignedAdvertisement.subtitle}</span>
                  <span className="advertising-display-resource">{assignedAdvertisement.resourceSummaryTitle}</span>
                  <span className="advertising-display-cta">{assignedAdvertisement.ctaLabel}</span>
                </>
              ) : (
                <>
                  <strong className="advertising-display-title">{translate('advertising.slotEmpty')}</strong>
                  <span className="advertising-display-subtitle">{translate('advertising.slotEmptyDescription')}</span>
                </>
              )}
              <span className="advertising-slot-hint">
                {draggingAdvertisementId
                  ? translate('advertising.slotDropHint')
                  : translate('advertising.slotDropIdleHint')}
              </span>
            </article>
          )
        })}
      </div>

      <div className="section-header">
        <div>
          <p className="eyebrow-label">{translate('advertising.approvedPoolEyebrow')}</p>
          <h4 className="section-title advertising-subsection-title">{translate('advertising.approvedPoolTitle')}</h4>
        </div>
      </div>

      {approvedAdvertisements.length === 0 ? (
        <p className="empty-state">{translate('advertising.approvedPoolEmpty')}</p>
      ) : (
        <div className="advertising-card-grid">
          {approvedAdvertisements.map(advertisement => (
            <button
              key={advertisement.advertisementId}
              type="button"
              draggable
              className={`advertising-display-card advertising-display-card--draggable${draggingAdvertisementId === advertisement.advertisementId ? ' is-dragging' : ''}`}
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
                className="advertising-display-image"
                fallbackContent={advertisement.title}
              />
              <span className="advertising-display-badge">
                {advertisement.slotIndex
                  ? `${translate('advertising.slotBadge')} ${advertisement.slotIndex}`
                  : translate('advertising.slotUnassigned')}
              </span>
              <strong className="advertising-display-title">{advertisement.title}</strong>
              <span className="advertising-display-subtitle">{advertisement.subtitle}</span>
              <span className="advertising-display-resource">{advertisement.resourceSummaryTitle}</span>
              <span className="advertising-slot-hint">
                {advertisement.slotIndex
                  ? translate('advertising.slotDragMoveHint')
                  : translate('advertising.slotDragAssignHint')}
              </span>
            </button>
          ))}
        </div>
      )}

      {unassignedAdvertisements.length > 0 ? (
        <p className="detail-label">
          {translate('advertising.unassignedWarning')} {unassignedAdvertisements.map(advertisement => advertisement.title).join(' 路 ')}
        </p>
      ) : null}
    </section>
  )
}

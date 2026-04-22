import { useEffect, useState } from 'react'

import { BackendAssetImage } from '../../BackendAssetImage'

import { useAdvertisingStore } from '../../../app/stores/advertising-store'

type AdvertisementReviewWorkspaceProps = {
  translate: (translationKey: string) => string
}

export function AdvertisementReviewWorkspace({ translate }: AdvertisementReviewWorkspaceProps) {
  const pendingReviewAdvertisements = useAdvertisingStore(state => state.pendingReviewAdvertisements)
  const reviewedAdvertisements = useAdvertisingStore(state => state.reviewedAdvertisements)
  const loadPendingReviewAdvertisements = useAdvertisingStore(state => state.loadPendingReviewAdvertisements)
  const loadReviewedAdvertisements = useAdvertisingStore(state => state.loadReviewedAdvertisements)
  const approveAdvertisement = useAdvertisingStore(state => state.approveAdvertisement)
  const rejectAdvertisement = useAdvertisingStore(state => state.rejectAdvertisement)
  const [reviewNotes, setReviewNotes] = useState<Record<string, string>>({})

  useEffect(() => {
    void Promise.all([loadPendingReviewAdvertisements(), loadReviewedAdvertisements()])
  }, [loadPendingReviewAdvertisements, loadReviewedAdvertisements])

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
                  <span>{`${advertisement.ownerDisplayName} · ${advertisement.resourceSummaryTitle}`}</span>
                  <span>{`${advertisement.placement} · P${advertisement.priority}`}</span>
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

      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('advertising.historyEyebrow')}</p>
            <h3 className="section-title">{translate('advertising.historyTitle')}</h3>
          </div>
        </div>

        {reviewedAdvertisements.length === 0 ? (
          <p className="empty-state">{translate('advertising.historyEmpty')}</p>
        ) : (
          <div className="advertising-admin-list">
            {reviewedAdvertisements.map(advertisement => (
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


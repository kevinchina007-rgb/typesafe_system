import { useEffect, useMemo, useState } from 'react'

import { useAdvertisingStore } from '@/app/stores/advertising-store'
import type { AdvertisementImageUploadResponse } from '@/microservices/advertising/objects/AdvertisementImageUploadResponse'
import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'

type AdvertisementSubmissionWorkspaceProps = {
  defaultPlacement: 'HotelBookingPage' | 'AttractionBookingPage'
  defaultTargetResourceType: 'Hotel' | 'Attraction'
  resourceOptions: Array<{ value: string; label: string }>
  translate: (translationKey: string) => string
  onOpenResource: (resourceId: string) => void
  onShowNotice?: (kind: 'success' | 'error', title: string, description: string) => void
}

type AdvertisingFormState = {
  title: string
  subtitle: string
  description: string
  imageUrl: string
  ctaLabel: string
  targetResourceId: string
  priority: number
  startAt: string
  endAt: string
}

function buildInitialFormState(resourceOptions: Array<{ value: string; label: string }>): AdvertisingFormState {
  return {
    title: '',
    subtitle: '',
    description: '',
    imageUrl: '',
    ctaLabel: '',
    targetResourceId: resourceOptions[0]?.value ?? '',
    priority: 50,
    startAt: '',
    endAt: '',
  }
}

export function AdvertisementSubmissionWorkspace({
  defaultPlacement,
  defaultTargetResourceType,
  resourceOptions,
  translate,
  onOpenResource,
  onShowNotice,
}: AdvertisementSubmissionWorkspaceProps) {
  const ownerAdvertisements = useAdvertisingStore(state => state.ownerAdvertisements)
  const loadOwnerAdvertisements = useAdvertisingStore(state => state.loadOwnerAdvertisements)
  const createAdvertisement = useAdvertisingStore(state => state.createAdvertisement)
  const uploadAdvertisementImage = useAdvertisingStore(state => state.uploadAdvertisementImage)
  const submitAdvertisementForReview = useAdvertisingStore(state => state.submitAdvertisementForReview)
  const pauseAdvertisement = useAdvertisingStore(state => state.pauseAdvertisement)
  const isLoading = useAdvertisingStore(state => state.isLoading)
  const [formState, setFormState] = useState<AdvertisingFormState>(() => buildInitialFormState(resourceOptions))
  const [isUploadingImage, setIsUploadingImage] = useState(false)
  const [uploadedImage, setUploadedImage] = useState<AdvertisementImageUploadResponse | null>(null)
  const [selectedImagePreviewUrl, setSelectedImagePreviewUrl] = useState<string | null>(null)
  const [selectedImageName, setSelectedImageName] = useState<string>('')
  const [latestCreatedAdvertisementId, setLatestCreatedAdvertisementId] = useState<string | null>(null)

  useEffect(() => {
    void loadOwnerAdvertisements()
  }, [loadOwnerAdvertisements])

  useEffect(() => {
    if (!formState.targetResourceId && resourceOptions[0]) {
      setFormState(current => ({ ...current, targetResourceId: resourceOptions[0].value }))
    }
  }, [formState.targetResourceId, resourceOptions])

  useEffect(() => () => {
    if (selectedImagePreviewUrl) {
      URL.revokeObjectURL(selectedImagePreviewUrl)
    }
  }, [selectedImagePreviewUrl])

  const reviewQueue = useMemo(
    () => ownerAdvertisements.filter(item => item.reviewStatus === 'PendingReview'),
    [ownerAdvertisements],
  )
  const latestCreatedAdvertisement = useMemo(
    () => ownerAdvertisements.find(item => item.advertisementId === latestCreatedAdvertisementId) ?? null,
    [latestCreatedAdvertisementId, ownerAdvertisements],
  )

  return (
    <section className="page-stack">
      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('advertising.submitEyebrow')}</p>
            <h2 className="section-title">{translate('advertising.submitTitle')}</h2>
          </div>
        </div>

        <form
          className="advertising-form-grid"
          onSubmit={async event => {
            event.preventDefault()
            const createdAdvertisement = await createAdvertisement({
              title: formState.title.trim(),
              subtitle: formState.subtitle.trim(),
              description: formState.description.trim(),
              imageUrl: formState.imageUrl.trim() || null,
              ctaLabel: formState.ctaLabel.trim(),
              targetResourceType: defaultTargetResourceType,
              targetResourceId: formState.targetResourceId,
              placement: defaultPlacement,
              priority: Number(formState.priority),
              startAt: new Date(formState.startAt).toISOString(),
              endAt: new Date(formState.endAt).toISOString(),
            })
            setLatestCreatedAdvertisementId(createdAdvertisement.advertisementId)
            await loadOwnerAdvertisements()
            setFormState(buildInitialFormState(resourceOptions))
            setUploadedImage(null)
            if (selectedImagePreviewUrl) {
              URL.revokeObjectURL(selectedImagePreviewUrl)
            }
            setSelectedImagePreviewUrl(null)
            setSelectedImageName('')
            onShowNotice?.(
              'success',
              translate('advertising.createSuccess'),
              translate('advertising.createSuccessDescription')
            )
          }}
        >
          <label>
            {translate('advertising.field.title')}
            <input
              value={formState.title}
              onChange={event => setFormState(current => ({ ...current, title: event.target.value }))}
              required
              disabled={isLoading}
            />
          </label>
          <label>
            {translate('advertising.field.subtitle')}
            <input
              value={formState.subtitle}
              onChange={event => setFormState(current => ({ ...current, subtitle: event.target.value }))}
              required
              disabled={isLoading}
            />
          </label>
          <label className="advertising-form-span-two">
            {translate('advertising.field.description')}
            <textarea
              value={formState.description}
              onChange={event => setFormState(current => ({ ...current, description: event.target.value }))}
              rows={4}
              required
              disabled={isLoading}
            />
          </label>
          <label>
            {translate('advertising.field.ctaLabel')}
            <input
              value={formState.ctaLabel}
              onChange={event => setFormState(current => ({ ...current, ctaLabel: event.target.value }))}
              required
              disabled={isLoading}
            />
          </label>
          <label>
            {translate('advertising.field.priority')}
            <input
              type="number"
              min={0}
              max={100}
              value={formState.priority}
              onChange={event => setFormState(current => ({ ...current, priority: Number(event.target.value) }))}
              required
              disabled={isLoading}
            />
          </label>
          <label className="advertising-form-span-two">
            {translate('advertising.field.imageFile')}
            <input
              type="file"
              accept="image/png,image/jpeg,image/webp"
              disabled={isLoading || isUploadingImage}
              onChange={async event => {
                const imageFile = event.target.files?.[0]
                if (!imageFile) {
                  return
                }

                if (selectedImagePreviewUrl) {
                  URL.revokeObjectURL(selectedImagePreviewUrl)
                }
                setSelectedImagePreviewUrl(URL.createObjectURL(imageFile))
                setSelectedImageName(imageFile.name)
                setIsUploadingImage(true)
                try {
                  const uploadedImageResponse = await uploadAdvertisementImage(imageFile)
                  setUploadedImage(uploadedImageResponse)
                  setFormState(current => ({ ...current, imageUrl: uploadedImageResponse.publicUrl }))
                } finally {
                  setIsUploadingImage(false)
                  event.target.value = ''
                }
              }}
            />
            {isUploadingImage ? <span className="detail-label">{translate('advertising.imageUploading')}</span> : null}
            {selectedImagePreviewUrl ? (
              <div className="advertising-upload-preview">
                <img src={selectedImagePreviewUrl} alt={selectedImageName || translate('advertising.field.imageFile')} className="advertising-upload-preview-image" />
                <div className="advertising-upload-preview-copy">
                  <strong>{isUploadingImage ? translate('advertising.imageUploading') : translate('advertising.field.imageFile')}</strong>
                  <span>{selectedImageName}</span>
                </div>
              </div>
            ) : null}
            {uploadedImage ? (
              <div className="advertising-upload-preview">
                <img src={uploadedImage.publicUrl} alt={uploadedImage.originalFileName} className="advertising-upload-preview-image" />
                <div className="advertising-upload-preview-copy">
                  <strong>{translate('advertising.imageUploaded')}</strong>
                  <span>{uploadedImage.originalFileName}</span>
                </div>
              </div>
            ) : null}
          </label>
          <label>
            {translate('advertising.field.targetResource')}
            {resourceOptions.length > 0 ? (
              <select
                value={formState.targetResourceId}
                onChange={event => setFormState(current => ({ ...current, targetResourceId: event.target.value }))}
                required
                disabled={isLoading}
              >
                {resourceOptions.map(option => (
                  <option key={option.value} value={option.value}>
                    {option.label}
                  </option>
                ))}
              </select>
            ) : (
              <input
                value={formState.targetResourceId}
                onChange={event => setFormState(current => ({ ...current, targetResourceId: event.target.value }))}
                placeholder={translate('advertising.field.targetResource')}
                required
                disabled={isLoading}
              />
            )}
          </label>
          <label>
            {translate('advertising.field.startAt')}
            <input
              type="datetime-local"
              value={formState.startAt}
              onChange={event => setFormState(current => ({ ...current, startAt: event.target.value }))}
              required
              disabled={isLoading}
            />
          </label>
          <label>
            {translate('advertising.field.endAt')}
            <input
              type="datetime-local"
              value={formState.endAt}
              onChange={event => setFormState(current => ({ ...current, endAt: event.target.value }))}
              required
              disabled={isLoading}
            />
          </label>

          <div className="advertising-form-actions advertising-form-span-two">
            <button type="submit" disabled={isLoading || isUploadingImage}>
              {translate('advertising.create')}
            </button>
          </div>
        </form>
      </section>

      {latestCreatedAdvertisement ? (
        <section className="page-card">
          <div className="section-header">
            <div>
              <p className="eyebrow-label">{translate('advertising.latestCreatedEyebrow')}</p>
              <h3 className="section-title">{translate('advertising.latestCreatedTitle')}</h3>
            </div>
          </div>
          <AdvertisementAdminCard
            advertisement={latestCreatedAdvertisement}
            translate={translate}
            onOpenResource={onOpenResource}
            onPause={pauseAdvertisement}
            onSubmitReview={submitAdvertisementForReview}
            isHighlighted
          />
        </section>
      ) : null}

      <section className="page-card">
        <div className="section-header">
          <div>
            <p className="eyebrow-label">{translate('advertising.myListEyebrow')}</p>
            <h3 className="section-title">{translate('advertising.myListTitle')}</h3>
          </div>
        </div>

        {ownerAdvertisements.length === 0 ? (
          <p className="empty-state">{translate('advertising.empty')}</p>
        ) : (
          <div className="advertising-admin-list">
            {ownerAdvertisements.map(advertisement => (
              <AdvertisementAdminCard
                key={advertisement.advertisementId}
                advertisement={advertisement}
                translate={translate}
                onOpenResource={onOpenResource}
                onPause={pauseAdvertisement}
                onSubmitReview={submitAdvertisementForReview}
              />
            ))}
          </div>
        )}
      </section>

      {reviewQueue.length > 0 ? (
        <section className="page-card">
          <div className="section-header">
            <div>
              <p className="eyebrow-label">{translate('advertising.pendingEyebrow')}</p>
              <h3 className="section-title">{translate('advertising.pendingTitle')}</h3>
            </div>
          </div>
          <div className="advertising-admin-list">
            {reviewQueue.map(advertisement => (
              <AdvertisementAdminCard
                key={advertisement.advertisementId}
                advertisement={advertisement}
                translate={translate}
                onOpenResource={onOpenResource}
                onPause={pauseAdvertisement}
                onSubmitReview={submitAdvertisementForReview}
              />
            ))}
          </div>
        </section>
      ) : null}
    </section>
  )
}

type AdvertisementAdminCardProps = {
  advertisement: AdvertisementResponse
  translate: (translationKey: string) => string
  onOpenResource: (resourceId: string) => void
  onPause: (advertisementId: string) => Promise<AdvertisementResponse>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
  isHighlighted?: boolean
}

function AdvertisementAdminCard({
  advertisement,
  translate,
  onOpenResource,
  onPause,
  onSubmitReview,
  isHighlighted = false,
}: AdvertisementAdminCardProps) {
  return (
    <article className={`panel-card advertising-admin-card${isHighlighted ? ' is-highlighted' : ''}`}>
      {advertisement.imageUrl ? (
        <img src={advertisement.imageUrl} alt={advertisement.title} className="advertising-admin-image" />
      ) : null}
      <div className="advertising-admin-copy">
        <strong>{advertisement.title}</strong>
        <span>{advertisement.subtitle}</span>
        <span>{advertisement.resourceSummaryTitle}</span>
        <span>{`${advertisement.reviewStatus} / ${advertisement.deliveryStatus}`}</span>
        {advertisement.rejectionNote ? (
          <span className="detail-label">{`${translate('advertising.rejectionNote')}: ${advertisement.rejectionNote}`}</span>
        ) : null}
      </div>
      <div className="advertising-admin-actions">
        <button type="button" className="secondary-button" onClick={() => onOpenResource(advertisement.targetResourceId)}>
          {translate('advertising.openResource')}
        </button>
        {advertisement.reviewStatus === 'Draft' || advertisement.reviewStatus === 'Rejected' ? (
          <button type="button" onClick={() => void onSubmitReview(advertisement.advertisementId)}>
            {translate('advertising.submitReview')}
          </button>
        ) : null}
        {advertisement.deliveryStatus === 'Active' ? (
          <button type="button" className="secondary-button" onClick={() => void onPause(advertisement.advertisementId)}>
            {translate('advertising.pause')}
          </button>
        ) : null}
      </div>
    </article>
  )
}

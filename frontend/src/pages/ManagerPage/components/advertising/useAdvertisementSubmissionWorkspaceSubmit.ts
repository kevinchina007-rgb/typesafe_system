import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { buildAdvertisementPayload, validateDraftName } from './useAdvertisementSubmissionWorkspaceValidation'
import { renderCreativeSvg, svgToFile, upsertLocalAdvertisement, type AdvertisementSubmissionWorkspaceProps, type CreativeState } from './AdvertisementSubmissionWorkspaceSupport'

export function useAdvertisementSubmissionWorkspaceSubmit({
  creative,
  draftName,
  targetResourceId,
  selectedResourceLabel,
  hyperlinkEnabled,
  defaultPlacement,
  defaultTargetResourceType,
  resourceOptions,
  editingAdvertisementId,
  setEditingAdvertisementId,
  setLocalDraftAdvertisements,
  loadOwnerAdvertisements,
  uploadAdvertisementImage,
  createAdvertisement,
  updateAdvertisement,
  submitAdvertisementForReview,
  onShowNotice,
  translate,
  setWorkspaceTab,
  setIsSubmitting,
}: {
  creative: CreativeState
  draftName: string
  targetResourceId: string
  selectedResourceLabel: string
  hyperlinkEnabled: boolean
  defaultPlacement: AdvertisementSubmissionWorkspaceProps['defaultPlacement']
  defaultTargetResourceType: AdvertisementSubmissionWorkspaceProps['defaultTargetResourceType']
  resourceOptions: AdvertisementSubmissionWorkspaceProps['resourceOptions']
  editingAdvertisementId: string | null
  setEditingAdvertisementId: (value: string | null) => void
  setLocalDraftAdvertisements: (value: AdvertisementResponse[] | ((current: AdvertisementResponse[]) => AdvertisementResponse[])) => void
  loadOwnerAdvertisements: () => Promise<unknown>
  uploadAdvertisementImage: (file: File) => Promise<{ publicUrl: string }>
  createAdvertisement: (payload: any) => Promise<AdvertisementResponse>
  updateAdvertisement: (id: string, payload: any) => Promise<AdvertisementResponse>
  submitAdvertisementForReview: (advertisementId: string) => Promise<AdvertisementResponse>
  onShowNotice?: AdvertisementSubmissionWorkspaceProps['onShowNotice']
  translate: AdvertisementSubmissionWorkspaceProps['translate']
  setWorkspaceTab: (value: 'create' | 'drafts') => void
  setIsSubmitting: (value: boolean) => void
}) {
  async function saveDraft(submitForReview = false) {
    const nextDraftName = validateDraftName({ draftName, onShowNotice })
    if (!nextDraftName) {
      return
    }
    setIsSubmitting(true)
    try {
      const svg = await renderCreativeSvg(creative)
      const uploadedImage = await uploadAdvertisementImage(svgToFile(svg))
      const payload = buildAdvertisementPayload({
        creative,
        draftName: nextDraftName,
        targetResourceId,
        selectedResourceLabel,
        hyperlinkEnabled,
        defaultPlacement,
        defaultTargetResourceType,
        resourceOptions,
        uploadedImageUrl: uploadedImage.publicUrl,
      })
      const savedAdvertisement = editingAdvertisementId ? await updateAdvertisement(editingAdvertisementId, payload) : await createAdvertisement(payload)
      setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, savedAdvertisement))
      if (submitForReview) {
        const submittedAdvertisement = await submitAdvertisementForReview(savedAdvertisement.advertisementId)
        setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, submittedAdvertisement))
      }
      await loadOwnerAdvertisements().catch(error => {
        console.warn('Failed to refresh advertisements after saving draft', error)
      })
      onShowNotice?.(
        'success',
        submitForReview ? translate('advertising.createSuccess') : '草稿已保存',
        submitForReview ? translate('advertising.createSuccessDescription') : '你可以在草稿页继续编辑或直接提交。',
      )
      setWorkspaceTab('drafts')
      setEditingAdvertisementId(savedAdvertisement.advertisementId)
    } catch (error) {
      onShowNotice?.('error', '草稿保存失败', error instanceof Error ? error.message : '保存时出现未知错误')
      throw error
    } finally {
      setIsSubmitting(false)
    }
  }

  async function withdrawAdvertisement(advertisement: AdvertisementResponse) {
    const withdrawnAdvertisement = await updateAdvertisement(advertisement.advertisementId, {
      advertisementKind: advertisement.advertisementKind,
      title: advertisement.title,
      subtitle: advertisement.subtitle,
      description: advertisement.description,
      imageUrl: advertisement.imageUrl,
      ctaLabel: advertisement.ctaLabel,
      targetResourceType: advertisement.targetResourceType,
      targetResourceId: advertisement.targetResourceId,
      resourceSummaryTitle: advertisement.resourceSummaryTitle,
      landingTarget: advertisement.landingTarget,
      placement: advertisement.placement,
      creativeJson: advertisement.creativeJson,
      creativeWidth: advertisement.creativeWidth,
      creativeHeight: advertisement.creativeHeight,
      priority: advertisement.priority,
      startAt: advertisement.startAt,
      endAt: advertisement.endAt,
    })
    await loadOwnerAdvertisements().catch(error => {
      console.warn('Failed to refresh advertisements after withdrawing draft', error)
    })
    setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, withdrawnAdvertisement))
    onShowNotice?.('success', '已撤回', '你可以再次编辑该广告。')
  }

  async function submitDraftAdvertisement(advertisementId: string) {
    const submittedAdvertisement = await submitAdvertisementForReview(advertisementId)
    setLocalDraftAdvertisements(current => upsertLocalAdvertisement(current, submittedAdvertisement))
    await loadOwnerAdvertisements().catch(error => {
      console.warn('Failed to refresh advertisements after submitting draft', error)
    })
    onShowNotice?.('success', '已提交', '广告草稿已提交审核。')
    return submittedAdvertisement
  }

  return {
    saveDraft,
    withdrawAdvertisement,
    submitDraftAdvertisement,
  }
}

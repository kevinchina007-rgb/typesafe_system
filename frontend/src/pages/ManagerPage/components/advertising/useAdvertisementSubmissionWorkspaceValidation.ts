import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { defaultWindow, getPrimaryCopy, limitText, type AdvertisementSubmissionWorkspaceProps, type CreativeState, type PlacementValue, type TargetResourceType } from './AdvertisementSubmissionWorkspaceSupport'

export function validateDraftName({
  draftName,
  onShowNotice,
}: {
  draftName: string
  onShowNotice?: AdvertisementSubmissionWorkspaceProps['onShowNotice']
}) {
  const nextName = draftName.trim()
  if (!nextName) {
    onShowNotice?.('error', '请输入草稿名称', '草稿名称不能为空，请先填写后再继续。')
    return null
  }
  return nextName
}

export function buildAdvertisementPayload({
  creative,
  draftName,
  targetResourceId,
  selectedResourceLabel,
  hyperlinkEnabled,
  defaultPlacement,
  defaultTargetResourceType,
  resourceOptions,
  uploadedImageUrl,
}: {
  creative: CreativeState
  draftName: string
  targetResourceId: string
  selectedResourceLabel: string
  hyperlinkEnabled: boolean
  defaultPlacement: PlacementValue
  defaultTargetResourceType: TargetResourceType
  resourceOptions: AdvertisementSubmissionWorkspaceProps['resourceOptions']
  uploadedImageUrl: string
}) {
  const { startAt, endAt } = defaultWindow()
  const copy = getPrimaryCopy(creative)
  const advertisementName = draftName.trim() || copy.title
  const fallbackTargetId = targetResourceId || resourceOptions[0]?.value || `${defaultTargetResourceType.toLowerCase()}-draft`
  const targetId = hyperlinkEnabled ? fallbackTargetId : fallbackTargetId
  const resourceSummaryTitle = hyperlinkEnabled ? (selectedResourceLabel || advertisementName) : advertisementName
  const landingTarget = hyperlinkEnabled && defaultTargetResourceType === 'Flight' ? `flight:${fallbackTargetId}` : 'disabled'

  return {
    advertisementKind: 'ResourcePromotion',
    title: limitText(advertisementName, 120),
    subtitle: limitText(copy.subtitle, 120),
    description: limitText(`${copy.title} ${copy.subtitle}`.trim(), 1200),
    imageUrl: uploadedImageUrl,
    ctaLabel: limitText(copy.ctaLabel, 24),
    targetResourceType: defaultTargetResourceType,
    targetResourceId: targetId,
    resourceSummaryTitle: limitText(resourceSummaryTitle, 160),
    landingTarget,
    placement: defaultPlacement,
    creativeJson: JSON.stringify(creative),
    creativeWidth: creative.width,
    creativeHeight: creative.height,
    priority: 50,
    startAt,
    endAt,
  }
}

export function restoreAdvertisementCreative(advertisement: AdvertisementResponse) {
  return advertisement.creativeJson
}

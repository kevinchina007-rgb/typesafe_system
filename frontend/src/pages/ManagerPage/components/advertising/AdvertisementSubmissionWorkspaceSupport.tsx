export {
  buildImageCandidates,
  buildRemoteImageCandidates,
  buildRemoteTextCandidates,
  buildTextCandidates,
  canvasHeight,
  canvasWidth,
  cloneCreative,
  cursorForResizeDirection,
  defaultCreative,
  defaultWindow,
  escapeSvgAttribute,
  escapeSvgText,
  flightCityOptions,
  getPrimaryCopy,
  inferResourceLabel,
  limitText,
  makeEmbeddableImageSource,
  makeImageDataUrl,
  makeLocalImageFallbackLabel,
  makeTextArtDataUrl,
  mergeAdvertisements,
  parseCreativeJson,
  renderCreativeSvg,
  resizeDirectionFromPointer,
  svgToFile,
  timeWindows,
  tonePalettes,
  upsertLocalAdvertisement,
  visualStyleLabels,
} from './AdvertisementSubmissionWorkspaceUtils'
export type {
  AdvertisementSubmissionWorkspaceProps,
  CanvasContextMenuState,
  CreativeElement,
  CreativeState,
  FactoryMode,
  ImageFactoryKind,
  PlacementValue,
  ResizeDirection,
  TargetResourceType,
  ToneKey,
  VisualStyleKey,
  WorkspaceTab,
} from './AdvertisementSubmissionWorkspaceUtils'

import type { AdvertisementResponse } from '@/microservices/advertising/objects/AdvertisementResponse'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

export type AdvertisementDraftSectionProps = {
  advertisements: AdvertisementResponse[]
  translate: (translationKey: string) => string
  onEdit: (advertisement: AdvertisementResponse) => void
  onOpenResource: (resourceId: string) => void
  onWithdraw: (advertisement: AdvertisementResponse) => Promise<void>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
}

export function AdvertisementDraftSection({ advertisements, translate, onEdit, onOpenResource, onWithdraw, onSubmitReview }: AdvertisementDraftSectionProps) {
  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div>
        <p className="text-sm font-bold text-slate-500">我的广告</p>
        <h3 className="m-0 text-2xl font-bold leading-tight text-slate-950">广告草稿</h3>
      </div>
      {advertisements.length === 0 ? <p className="text-sm leading-6 text-slate-500">{translate('advertising.empty')}</p> : (
        <div className="grid gap-4">
          {advertisements.map(advertisement => (
            <AdvertisementAdminCard
              key={advertisement.advertisementId}
              advertisement={advertisement}
              onEdit={onEdit}
              onOpenResource={onOpenResource}
              onWithdraw={onWithdraw}
              onSubmitReview={onSubmitReview}
            />
          ))}
        </div>
      )}
    </section>
  )
}

type AdvertisementAdminCardProps = {
  advertisement: AdvertisementResponse
  onEdit: (advertisement: AdvertisementResponse) => void
  onOpenResource: (resourceId: string) => void
  onWithdraw: (advertisement: AdvertisementResponse) => Promise<void>
  onSubmitReview: (advertisementId: string) => Promise<AdvertisementResponse>
}

function AdvertisementAdminCard({ advertisement, onEdit, onOpenResource, onWithdraw, onSubmitReview }: AdvertisementAdminCardProps) {
  return (
    <article className="grid gap-3 border border-slate-200 bg-slate-50 p-4">
      <div className="flex flex-wrap items-start justify-between gap-3">
        <div className="grid gap-1">
          <strong>{advertisement.title}</strong>
          <span className="text-sm text-slate-500">{advertisement.resourceSummaryTitle}</span>
        </div>
        <span className="text-xs font-bold uppercase tracking-wider text-slate-500">{advertisement.reviewStatus} / {advertisement.deliveryStatus}</span>
      </div>
      {advertisement.imageUrl ? (
        <BackendAssetImage assetUrl={advertisement.imageUrl} alt={advertisement.title} className="max-h-56 w-full object-cover" />
      ) : null}
      <div className="flex flex-wrap gap-2">
        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950" onClick={() => onEdit(advertisement)}>
          编辑
        </button>
        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950" onClick={() => onOpenResource(advertisement.targetResourceId)}>
          查看资源
        </button>
        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950" onClick={() => void onWithdraw(advertisement)}>
          撤回
        </button>
        <button type="button" className="inline-flex min-h-10 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white" onClick={() => void onSubmitReview(advertisement.advertisementId)}>
          提交审核
        </button>
      </div>
    </article>
  )
}

import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

export function FeedbackConversationAvatar({
  imageUrl,
  fallback,
  alt,
  useBackendAsset = false,
}: {
  imageUrl: string | null | undefined
  fallback: string
  alt: string
  useBackendAsset?: boolean
}) {
  const className = 'flex h-11 w-11 shrink-0 items-center justify-center overflow-hidden border border-slate-200 bg-white object-contain text-sm font-bold text-slate-700'
  if (useBackendAsset) {
    return <BackendAssetImage assetUrl={imageUrl} alt={alt} className={className} fallbackContent={fallback} />
  }
  if (imageUrl) {
    return <img src={imageUrl} alt={alt} className={className} />
  }
  return <span className={className}>{fallback}</span>
}


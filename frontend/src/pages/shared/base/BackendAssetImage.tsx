import type { ReactNode } from 'react'
import { useEffect, useState } from 'react'

import { toBackendAssetUrl } from '@/lib/presenters/view-models'

type BackendAssetImageProps = {
  assetUrl: string | null | undefined
  alt: string
  className?: string
  fallbackContent?: ReactNode
}

export function BackendAssetImage({ assetUrl, alt, className, fallbackContent }: BackendAssetImageProps) {
  const [resolvedImageUrl, setResolvedImageUrl] = useState<string>('')

  useEffect(() => {
    const normalizedAssetUrl = toBackendAssetUrl(assetUrl)
    if (!normalizedAssetUrl) {
      setResolvedImageUrl('')
      return
    }

    let cancelled = false
    const preloadedImage = new Image()

    preloadedImage.onload = () => {
      if (!cancelled) {
        setResolvedImageUrl(normalizedAssetUrl)
      }
    }

    preloadedImage.onerror = () => {
      if (!cancelled) {
        setResolvedImageUrl('')
      }
    }

    preloadedImage.src = normalizedAssetUrl

    return () => {
      cancelled = true
    }
  }, [assetUrl])

  if (!resolvedImageUrl) {
    return (
      <span className={className} aria-label={alt}>
        {fallbackContent ?? alt}
      </span>
    )
  }

  return <img src={resolvedImageUrl} alt={alt} className={className} />
}

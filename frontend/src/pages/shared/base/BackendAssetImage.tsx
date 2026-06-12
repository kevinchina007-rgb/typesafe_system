// 本文件定义后端资源图片组件，负责预加载图片并在失败时显示兜底图。

import type { ReactNode } from 'react'
import { useEffect, useState } from 'react'

import { toBackendAssetUrl } from '@/lib/presenters/view-models'

// 后端资源图片组件的输入参数。
type BackendAssetImageProps = {
  assetUrl: string | null | undefined
  alt: string
  className?: string
  fallbackContent?: ReactNode
}

// 后端资源图片组件，先预加载，再决定显示图片还是兜底内容。
export function BackendAssetImage({ assetUrl, alt, className, fallbackContent }: BackendAssetImageProps) {
  // 预加载成功后再真正渲染图片，避免闪烁和坏图。
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
    // 预加载失败或没有资源时，退回到文字/占位内容。
    return (
      <span className={className} aria-label={alt}>
        {fallbackContent ?? alt}
      </span>
    )
  }

  return <img src={resolvedImageUrl} alt={alt} className={className} />
}

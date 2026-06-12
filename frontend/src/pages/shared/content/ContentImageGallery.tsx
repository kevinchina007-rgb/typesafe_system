// 本文件定义共享页面组件或工具，负责页面间复用逻辑。

import type { ContentImageResponse } from '@/lib/mvp-types/index'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

// 内容图片画廊的输入参数。
type ContentImageGalleryProps = {
  images: ContentImageResponse[]
}

// 内容图片画廊，负责把多张内容图片按网格展示出来。
export function ContentImageGallery({ images }: ContentImageGalleryProps) {
  if (images.length === 0) {
    return null
  }

  return (
    <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
      {images.map(image => (
        <figure key={image.imageId} className="grid gap-2 border border-slate-200 bg-white p-2">
          <BackendAssetImage assetUrl={image.publicUrl} alt={image.originalFileName} className="aspect-video w-full object-cover" />
        </figure>
      ))}
    </div>
  )
}

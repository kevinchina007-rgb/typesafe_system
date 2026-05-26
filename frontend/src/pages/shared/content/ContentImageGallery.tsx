import type { ContentImageResponse } from '@/lib/mvp-types/index'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

type ContentImageGalleryProps = {
  images: ContentImageResponse[]
}

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

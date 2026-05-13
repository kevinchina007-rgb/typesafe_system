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
    <div className="content-image-gallery">
      {images.map(image => (
        <figure key={image.imageId} className="content-image-card">
          <BackendAssetImage assetUrl={image.publicUrl} alt={image.originalFileName} className="content-image" />
        </figure>
      ))}
    </div>
  )
}

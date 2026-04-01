import type { ContentImageResponse } from '../lib/mvp-types'
import { getTravelBackendOrigin } from '../lib/runtime-config'

type ContentImageGalleryProps = {
  images: ContentImageResponse[]
}

export function ContentImageGallery({ images }: ContentImageGalleryProps) {
  if (images.length === 0) {
    return null
  }

  const backendOrigin = getTravelBackendOrigin()

  return (
    <div className="content-image-gallery">
      {images.map(image => (
        <figure key={image.imageId} className="content-image-card">
          <img
            src={image.publicUrl.startsWith('http://') || image.publicUrl.startsWith('https://') ? image.publicUrl : `${backendOrigin}${image.publicUrl}`}
            alt={image.originalFileName}
            className="content-image"
          />
        </figure>
      ))}
    </div>
  )
}

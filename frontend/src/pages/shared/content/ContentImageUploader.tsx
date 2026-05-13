import type { ContentImageResponse } from '@/lib/mvp-types/index'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

type ContentImageUploaderProps = {
  images: ContentImageResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onUploadImage: (imageFile: File) => Promise<ContentImageResponse>
  onChangeImages: (images: ContentImageResponse[]) => void
}

const maximumImageBytes = 5 * 1024 * 1024
const maximumImageCount = 6
const allowedImageMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg', 'image/webp'])

export function ContentImageUploader({
  images,
  isBusy,
  translate,
  onUploadImage,
  onChangeImages,
}: ContentImageUploaderProps) {
  async function handleFiles(fileList: FileList | null) {
    if (!fileList || fileList.length === 0) {
      return
    }

    const remainingSlots = Math.max(0, maximumImageCount - images.length)
    const nextFiles = Array.from(fileList).slice(0, remainingSlots)
    const uploadedImages: ContentImageResponse[] = []

    for (const imageFile of nextFiles) {
      if (!allowedImageMimeTypes.has(imageFile.type.toLowerCase())) {
        continue
      }
      if (imageFile.size > maximumImageBytes) {
        continue
      }
      const uploadedImage = await onUploadImage(imageFile)
      uploadedImages.push(uploadedImage)
    }

    if (uploadedImages.length > 0) {
      onChangeImages(
        [...images, ...uploadedImages].map((image, index) => ({
          ...image,
          sortOrder: index,
        })),
      )
    }
  }

  return (
    <div className="content-image-uploader">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('content.images')}</p>
          <h4>{translate('content.imagesTitle')}</h4>
        </div>
        <span className="detail-label">{`${images.length}/${maximumImageCount}`}</span>
      </div>

      <p className="hero-copy">{translate('content.imagesHint')}</p>

      <div className="manager-task-actions">
        <label className="secondary-button file-upload-button">
          <input
            type="file"
            accept="image/png,image/jpeg,image/jpg,image/webp"
            multiple
            disabled={isBusy || images.length >= maximumImageCount}
            onChange={async event => {
              await handleFiles(event.target.files)
              event.currentTarget.value = ''
            }}
          />
          {translate('content.imagesSelect')}
        </label>
      </div>

      {images.length > 0 ? (
        <div className="content-image-gallery">
          {images.map(image => (
            <figure key={image.imageId} className="content-image-card">
              <BackendAssetImage assetUrl={image.publicUrl} alt={image.originalFileName} className="content-image" />
              <figcaption>{image.originalFileName}</figcaption>
              <button
                type="button"
                className="secondary-button"
                disabled={isBusy}
                onClick={() => onChangeImages(images.filter(currentImage => currentImage.imageId !== image.imageId).map((currentImage, index) => ({
                  ...currentImage,
                  sortOrder: index,
                })))}
              >
                {translate('content.imagesRemove')}
              </button>
            </figure>
          ))}
        </div>
      ) : null}
    </div>
  )
}

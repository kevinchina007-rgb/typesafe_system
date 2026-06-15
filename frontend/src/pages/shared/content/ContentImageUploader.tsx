// 本文件定义共享页面组件或工具，负责页面间复用逻辑。

import type { ContentImagePlannerResponse } from '@/lib/mvp-types/index'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

// 内容图片上传器的输入参数。
type ContentImageUploaderProps = {
  images: ContentImagePlannerResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onUploadImage: (imageFile: File) => Promise<ContentImagePlannerResponse>
  onChangeImages: (images: ContentImagePlannerResponse[]) => void
}

const maximumImageBytes = 5 * 1024 * 1024
const maximumImageCount = 6
const allowedImageMimeTypes = new Set(['image/png', 'image/jpeg', 'image/jpg', 'image/webp'])

// 内容图片上传器，负责校验图片、上传图片并维护展示顺序。
export function ContentImageUploader({
  images,
  isBusy,
  translate,
  onUploadImage,
  onChangeImages,
}: ContentImageUploaderProps) {
  // 处理用户选择的图片文件并逐个上传。
  async function handleFiles(fileList: FileList | null) {
    if (!fileList || fileList.length === 0) {
      return
    }

    const remainingSlots = Math.max(0, maximumImageCount - images.length)
    const nextFiles = Array.from(fileList).slice(0, remainingSlots)
    const uploadedImages: ContentImagePlannerResponse[] = []

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
    <div className="grid gap-3">
      {/* 上方展示图片模块标题和当前数量。 */}
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('content.images')}</p>
          <h4>{translate('content.imagesTitle')}</h4>
        </div>
        <span className="text-sm font-medium text-slate-500">{`${images.length}/${maximumImageCount}`}</span>
      </div>

      {/* 中间提示用户上传要求。 */}
      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('content.imagesHint')}</p>

      <div className="flex flex-wrap items-center gap-3">
        <label className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55 inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55">
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

      {/* 已上传图片按网格展示，并允许删除后重新排序。 */}
      {images.length > 0 ? (
        <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
          {images.map(image => (
            <figure key={image.imageId} className="grid gap-2 border border-slate-200 bg-white p-2">
              <BackendAssetImage assetUrl={image.publicUrl} alt={image.originalFileName} className="aspect-video w-full object-cover" />
              <figcaption>{image.originalFileName}</figcaption>
              <button
                type="button"
                className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy}
                onClick={() =>
                  onChangeImages(
                    images
                      .filter(currentImage => currentImage.imageId !== image.imageId)
                      .map((currentImage, index) => ({
                        ...currentImage,
                        sortOrder: index,
                      })),
                  )
                }
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

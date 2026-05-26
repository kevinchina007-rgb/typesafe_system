export type AdvertisementImageUploadResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export const advertisementImageUploadResponseFromJson = (json: string): AdvertisementImageUploadResponse =>
  JSON.parse(json) as AdvertisementImageUploadResponse

export const advertisementImageUploadResponseToJson = (value: AdvertisementImageUploadResponse): string =>
  JSON.stringify(value)

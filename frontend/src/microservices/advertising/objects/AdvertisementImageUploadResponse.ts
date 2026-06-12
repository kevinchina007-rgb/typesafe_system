// 本文件定义 advertising 模块的 `AdvertisementImageUploadResponse`，作为响应数据并提供 JSON 编解码。

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

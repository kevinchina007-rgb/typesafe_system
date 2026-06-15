// 本文件定义 advertising 模块的 `UploadAdvertisementImageResponse`，用于图片上传响应并提供 JSON 编解码。

export type UploadAdvertisementImageResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export const uploadAdvertisementImageResponseFromJson = (json: string): UploadAdvertisementImageResponse =>
  JSON.parse(json) as UploadAdvertisementImageResponse

export const uploadAdvertisementImageResponseToJson = (value: UploadAdvertisementImageResponse): string =>
  JSON.stringify(value)

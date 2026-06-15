// 本文件定义 advertising 模块的 `UploadAdvertisementImageRequest`，用于图片上传入参并提供 JSON 编解码。

export type UploadAdvertisementImageRequest = {
  originalFileName: string
  mimeType: string
  fileContentBase64: string
}

export const uploadAdvertisementImageRequestFromJson = (json: string): UploadAdvertisementImageRequest =>
  JSON.parse(json) as UploadAdvertisementImageRequest

export const uploadAdvertisementImageRequestToJson = (value: UploadAdvertisementImageRequest): string =>
  JSON.stringify(value)

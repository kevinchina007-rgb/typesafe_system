// 本文件定义 attraction 模块的 `AttractionImageUploadResponse`，作为响应数据并提供 JSON 编解码。

export type AttractionImageUploadResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export const attractionImageUploadResponseFromJson = (json: string): AttractionImageUploadResponse =>
  JSON.parse(json) as AttractionImageUploadResponse

export const attractionImageUploadResponseToJson = (value: AttractionImageUploadResponse): string =>
  JSON.stringify(value)

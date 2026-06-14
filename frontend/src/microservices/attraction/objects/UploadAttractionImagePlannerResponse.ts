// 本文件定义 attraction 模块的 `UploadAttractionImagePlannerResponse`，作为图片上传响应并提供 JSON 编解码。

export type UploadAttractionImagePlannerResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export const uploadAttractionImagePlannerResponseFromJson = (json: string): UploadAttractionImagePlannerResponse =>
  JSON.parse(json) as UploadAttractionImagePlannerResponse

export const uploadAttractionImagePlannerResponseToJson = (value: UploadAttractionImagePlannerResponse): string =>
  JSON.stringify(value)

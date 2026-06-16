// 本文件定义 attraction 模块的 `UploadAttractionImagePlannerRequest`，作为图片上传参数并提供 JSON 编解码。

export type UploadAttractionImagePlannerRequest = {
  originalFileName: string
  mimeType: string
  fileContentBase64: string
}

export const uploadAttractionImagePlannerRequestFromJson = (json: string): UploadAttractionImagePlannerRequest =>
  JSON.parse(json) as UploadAttractionImagePlannerRequest

export const uploadAttractionImagePlannerRequestToJson = (value: UploadAttractionImagePlannerRequest): string =>
  JSON.stringify(value)

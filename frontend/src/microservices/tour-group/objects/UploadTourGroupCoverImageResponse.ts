// 本文件定义 tour-group 模块的 `UploadTourGroupCoverImageResponse`，作为封面上传响应并提供 JSON 编解码。

export type UploadTourGroupCoverImageResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export const uploadTourGroupCoverImageResponseFromJson = (json: string): UploadTourGroupCoverImageResponse =>
  JSON.parse(json) as UploadTourGroupCoverImageResponse

export const uploadTourGroupCoverImageResponseToJson = (value: UploadTourGroupCoverImageResponse): string =>
  JSON.stringify(value)

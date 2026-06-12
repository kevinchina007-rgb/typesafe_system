// 本文件定义 tour-group 模块的 `TourGroupMessageAttachmentResponse`，作为响应数据并提供 JSON 编解码。

export type TourGroupMessageAttachmentResponse = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}
export const tourGroupMessageAttachmentResponseFromJson = (json: string): TourGroupMessageAttachmentResponse =>
  JSON.parse(json) as TourGroupMessageAttachmentResponse

export const tourGroupMessageAttachmentResponseToJson = (value: TourGroupMessageAttachmentResponse): string =>
  JSON.stringify(value)

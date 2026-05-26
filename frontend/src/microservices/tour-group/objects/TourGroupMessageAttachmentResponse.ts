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

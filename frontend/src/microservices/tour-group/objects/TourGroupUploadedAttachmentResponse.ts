export type TourGroupUploadedAttachmentResponse = {
  attachmentId: string
  attachmentType: string
  publicUrl: string
  storagePath: string
  originalFileName: string
  mimeType: string
  fileSize: number
  sortOrder: number
  createdAt: string
}
export const tourGroupUploadedAttachmentResponseFromJson = (json: string): TourGroupUploadedAttachmentResponse =>
  JSON.parse(json) as TourGroupUploadedAttachmentResponse

export const tourGroupUploadedAttachmentResponseToJson = (value: TourGroupUploadedAttachmentResponse): string =>
  JSON.stringify(value)

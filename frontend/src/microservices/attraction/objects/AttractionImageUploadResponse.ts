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

export type ContentImageResponse = {
  imageId: string
  publicUrl: string
  originalFileName: string
  contentType: string
  byteSize: number
  sortOrder: number
}
export const contentImageResponseFromJson = (json: string): ContentImageResponse =>
  JSON.parse(json) as ContentImageResponse

export const contentImageResponseToJson = (value: ContentImageResponse): string =>
  JSON.stringify(value)

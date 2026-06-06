export type HotelRoomTypeImageUploadResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export const hotelRoomTypeImageUploadResponseFromJson = (json: string): HotelRoomTypeImageUploadResponse =>
  JSON.parse(json) as HotelRoomTypeImageUploadResponse

export const hotelRoomTypeImageUploadResponseToJson = (value: HotelRoomTypeImageUploadResponse): string =>
  JSON.stringify(value)

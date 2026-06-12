// 本文件定义 hotel 模块的 `HotelRoomTypeImageUploadResponse`，作为响应数据并提供 JSON 编解码。

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

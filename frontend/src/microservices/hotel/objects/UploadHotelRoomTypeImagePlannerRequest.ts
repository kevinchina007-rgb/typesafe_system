export type UploadHotelRoomTypeImagePlannerRequest = {
  originalFileName: string
  mimeType: string
  fileContentBase64: string
}

export const uploadHotelRoomTypeImagePlannerRequestFromJson = (json: string): UploadHotelRoomTypeImagePlannerRequest =>
  JSON.parse(json) as UploadHotelRoomTypeImagePlannerRequest

export const uploadHotelRoomTypeImagePlannerRequestToJson = (value: UploadHotelRoomTypeImagePlannerRequest): string =>
  JSON.stringify(value)

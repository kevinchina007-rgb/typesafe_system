// This file defines `tour-group` module `UploadTourGroupCoverImagePlannerRequest` as request payload data and provides JSON helpers.

export type UploadTourGroupCoverImagePlannerRequest = {
  ownerUserId: string
  originalFileName: string
  mimeType: string
  fileContentBase64: string
}
export const uploadTourGroupCoverImagePlannerRequestFromJson = (json: string): UploadTourGroupCoverImagePlannerRequest =>
  JSON.parse(json) as UploadTourGroupCoverImagePlannerRequest

export const uploadTourGroupCoverImagePlannerRequestToJson = (value: UploadTourGroupCoverImagePlannerRequest): string =>
  JSON.stringify(value)

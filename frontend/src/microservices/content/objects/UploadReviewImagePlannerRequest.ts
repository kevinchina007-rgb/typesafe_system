// UploadReviewImagePlannerRequest：content 域评论图片上传请求对象。

export type UploadReviewImagePlannerRequest = {
  userId: string
  originalFileName: string
  contentType: string
  base64Content: string
}

export const uploadReviewImagePlannerRequestFromJson = (json: string): UploadReviewImagePlannerRequest =>
  JSON.parse(json) as UploadReviewImagePlannerRequest

export const uploadReviewImagePlannerRequestToJson = (value: UploadReviewImagePlannerRequest): string =>
  JSON.stringify(value)

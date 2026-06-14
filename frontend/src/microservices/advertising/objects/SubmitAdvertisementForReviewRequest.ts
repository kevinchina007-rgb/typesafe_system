// 本文件定义 SubmitAdvertisementForReviewPlanner 使用的提交审核请求，字段只覆盖该动作所需内容。

export type SubmitAdvertisementForReviewRequest = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
}

export const submitAdvertisementForReviewRequestFromJson = (json: string): SubmitAdvertisementForReviewRequest =>
  JSON.parse(json) as SubmitAdvertisementForReviewRequest

export const submitAdvertisementForReviewRequestToJson = (value: SubmitAdvertisementForReviewRequest): string =>
  JSON.stringify(value)

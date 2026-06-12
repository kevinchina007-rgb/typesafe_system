// 本文件定义 advertising 模块的 `AdvertisementReviewDecisionRequest`，作为请求参数并提供 JSON 编解码。

export type AdvertisementReviewDecisionRequest = {
  reviewerManagerId?: string
  reviewNote?: string | null
}

export const advertisementReviewDecisionRequestFromJson = (json: string): AdvertisementReviewDecisionRequest =>
  JSON.parse(json) as AdvertisementReviewDecisionRequest

export const advertisementReviewDecisionRequestToJson = (value: AdvertisementReviewDecisionRequest): string =>
  JSON.stringify(value)

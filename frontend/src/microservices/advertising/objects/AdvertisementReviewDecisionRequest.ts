// 本文件定义 advertising 模块的 `AdvertisementReviewDecisionRequest`，用于审核、驳回和下线展示等动作并提供 JSON 编解码。

export type AdvertisementReviewDecisionRequest = {
  advertisementId: string
  reviewerManagerId: string
  reviewNote: string | null
}

export const advertisementReviewDecisionRequestFromJson = (json: string): AdvertisementReviewDecisionRequest =>
  JSON.parse(json) as AdvertisementReviewDecisionRequest

export const advertisementReviewDecisionRequestToJson = (value: AdvertisementReviewDecisionRequest): string =>
  JSON.stringify(value)

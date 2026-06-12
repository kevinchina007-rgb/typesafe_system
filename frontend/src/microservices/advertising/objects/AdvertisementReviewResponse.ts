// 本文件定义 advertising 模块的 `AdvertisementReviewResponse`，作为响应数据并提供 JSON 编解码。

export type AdvertisementReviewResponse = {
  reviewId: string
  reviewerManagerId: string
  reviewStatus: string
  reviewNote: string | null
  reviewedAt: string
}

export const advertisementReviewResponseFromJson = (json: string): AdvertisementReviewResponse =>
  JSON.parse(json) as AdvertisementReviewResponse

export const advertisementReviewResponseToJson = (value: AdvertisementReviewResponse): string =>
  JSON.stringify(value)

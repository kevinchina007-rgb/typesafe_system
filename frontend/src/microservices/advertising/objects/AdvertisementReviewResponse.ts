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

// AdvertisementReviewPlannerResponse for advertising review records.

export type AdvertisementReviewPlannerResponse = {
  reviewId: string
  reviewerUserId: string
  reviewerDisplayName: string
  reviewStatus: string
  reviewNote: string | null
  createdAt: string
  updatedAt: string
}

export const advertisementReviewPlannerResponseFromJson = (json: string): AdvertisementReviewPlannerResponse =>
  JSON.parse(json) as AdvertisementReviewPlannerResponse

export const advertisementReviewPlannerResponseToJson = (value: AdvertisementReviewPlannerResponse): string =>
  JSON.stringify(value)

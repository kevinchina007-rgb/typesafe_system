export type AdvertisementReviewDecisionRequest = {
  reviewNote?: string | null
}

export const advertisementReviewDecisionRequestFromJson = (json: string): AdvertisementReviewDecisionRequest =>
  JSON.parse(json) as AdvertisementReviewDecisionRequest

export const advertisementReviewDecisionRequestToJson = (value: AdvertisementReviewDecisionRequest): string =>
  JSON.stringify(value)

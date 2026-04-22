export type AdvertisementReviewResponse = {
  reviewId: string
  reviewerManagerId: string
  decision: string
  reviewNote: string | null
  reviewedAt: string
}

export type AdvertisementResponse = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
  ownerDisplayName: string
  targetResourceType: string
  targetResourceId: string
  resourceSummaryTitle: string
  landingTarget: string
  placement: string
  audience: string
  title: string
  subtitle: string
  description: string
  imageUrl: string | null
  ctaLabel: string
  reviewStatus: string
  deliveryStatus: string
  priority: number
  startAt: string
  endAt: string
  rejectionNote: string | null
  createdAt: string
  updatedAt: string
  reviews: AdvertisementReviewResponse[]
}

export type AdvertisementListResponse = {
  advertisements: AdvertisementResponse[]
}

export type AdvertisementImageUploadResponse = {
  assetId: string
  publicUrl: string
  originalFileName: string
  mimeType: string
  fileSize: number
}

export type CreateAdvertisementRequest = {
  title: string
  subtitle: string
  description: string
  imageUrl?: string | null
  ctaLabel: string
  targetResourceType: string
  targetResourceId: string
  placement: string
  priority: number
  startAt: string
  endAt: string
}

export type UpdateAdvertisementRequest = {
  title: string
  subtitle: string
  description: string
  imageUrl?: string | null
  ctaLabel: string
  targetResourceId: string
  placement: string
  priority: number
  startAt: string
  endAt: string
}

export type AdvertisementReviewDecisionRequest = {
  reviewNote?: string | null
}

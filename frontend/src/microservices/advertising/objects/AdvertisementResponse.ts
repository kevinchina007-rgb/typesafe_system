import type { AdvertisementReviewResponse } from './AdvertisementReviewResponse'

export type AdvertisementResponse = {
  advertisementId: string
  ownerManagerId: string
  ownerType: string
  ownerDisplayName: string
  advertisementKind: string
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
  slotIndex: number | null
  creativeJson: string | null
  creativeWidth: number
  creativeHeight: number
  startAt: string
  endAt: string
  rejectionNote: string | null
  createdAt: string
  updatedAt: string
  reviews: AdvertisementReviewResponse[]
}

export const advertisementResponseFromJson = (json: string): AdvertisementResponse =>
  JSON.parse(json) as AdvertisementResponse

export const advertisementResponseToJson = (value: AdvertisementResponse): string =>
  JSON.stringify(value)

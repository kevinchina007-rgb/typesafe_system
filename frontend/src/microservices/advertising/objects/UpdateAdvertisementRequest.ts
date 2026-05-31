export type UpdateAdvertisementRequest = {
  ownerManagerId: string
  ownerType: string
  advertisementKind?: string | null
  title: string
  subtitle: string
  description: string
  imageUrl?: string | null
  ctaLabel: string
  targetResourceType: string
  targetResourceId: string
  resourceSummaryTitle?: string | null
  landingTarget?: string | null
  placement: string
  creativeJson?: string | null
  creativeWidth?: number | null
  creativeHeight?: number | null
  priority: number
  startAt: string
  endAt: string
}

export const updateAdvertisementRequestFromJson = (json: string): UpdateAdvertisementRequest =>
  JSON.parse(json) as UpdateAdvertisementRequest

export const updateAdvertisementRequestToJson = (value: UpdateAdvertisementRequest): string =>
  JSON.stringify(value)

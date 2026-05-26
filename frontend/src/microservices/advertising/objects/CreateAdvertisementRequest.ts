export type CreateAdvertisementRequest = {
  ownerManagerId: string
  ownerType: string
  ownerDisplayName: string
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
  priority: number
  startAt: string
  endAt: string
}

export const createAdvertisementRequestFromJson = (json: string): CreateAdvertisementRequest =>
  JSON.parse(json) as CreateAdvertisementRequest

export const createAdvertisementRequestToJson = (value: CreateAdvertisementRequest): string =>
  JSON.stringify(value)

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

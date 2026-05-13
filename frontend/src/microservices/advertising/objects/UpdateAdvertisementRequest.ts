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

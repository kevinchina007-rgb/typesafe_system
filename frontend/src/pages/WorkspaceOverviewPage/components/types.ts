import type { AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'

export type DashboardShortcut = {
  title: string
  description: string
  viewKey: AppViewKey
}

export type RecentOrderSummary = {
  orderId: string
  status: string
  totalPriceLabel: string
  createdAtLabel: string
  itemCount: number
}

export type UpcomingTripSummary = {
  id: string
  title: string
  subtitle: string
  startAtLabel: string
}

export type HeroStat = {
  label: string
  value: string | number
  detail?: string
}

export type CommunityActivitySummary = {
  id: string
  title: string
  meta: string
  kind: 'blog' | 'review'
}

export type RecommendedNextStep = {
  title: string
  description: string
  ctaLabel: string
  viewKey: AppViewKey
}

export type AccountSummaryCardProps = {
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type TravelerSummaryCardProps = {
  travelerCount: number
  defaultTravelerName: string | null
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type BookingShortcutsCardProps = {
  shortcuts: DashboardShortcut[]
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type RecentOrdersCardProps = {
  orders: RecentOrderSummary[]
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type UpcomingTripsCardProps = {
  trips: UpcomingTripSummary[]
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type CommunityActivityCardProps = {
  activities: CommunityActivitySummary[]
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type RecommendedNextStepCardProps = {
  isSessionReady: boolean
  recommendation: RecommendedNextStep
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

export type AppSidebarSummaryProps = {
  healthText: string
  signedInUser: UserResponse | null
  signedInManager: CurrentManagerSessionResponse | null
}


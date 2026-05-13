import type { AppViewKey, BlogPostSummaryResponse, OrderLineItemResponse, OrderResponse, ReviewResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'

import type { CommunityActivitySummary, DashboardShortcut, HeroStat, RecommendedNextStep, UpcomingTripSummary } from '@/pages/WorkspaceOverviewPage/components/types'

export function formatOverviewDateLabel(value: string) {
  return new Date(value).toLocaleString()
}

export function buildUpcomingTripEntries(orders: OrderResponse[]): UpcomingTripSummary[] {
  const now = Date.now()
  const trips = orders.flatMap(order =>
    order.orderLineItems.flatMap(item => {
      const nextTrip = getOrderItemUpcomingMoment(item)
      if (!nextTrip || new Date(nextTrip.startsAt).getTime() < now) {
        return []
      }

      return [
        {
          id: `${order.orderId}-${item.orderItemId}`,
          title: nextTrip.title,
          subtitle: `${order.orderId} · ${item.summaryLabel}`,
          startsAt: nextTrip.startsAt,
          startAtLabel: formatOverviewDateLabel(nextTrip.startsAt),
        },
      ]
    }),
  )

  return trips
    .sort((left, right) => new Date(left.startsAt).getTime() - new Date(right.startsAt).getTime())
    .slice(0, 4)
    .map(({ startsAt: _startsAt, ...trip }) => trip)
}

export function buildCommunityActivity(posts: BlogPostSummaryResponse[], reviews: ReviewResponse[]): CommunityActivitySummary[] {
  return [
    ...posts.slice(0, 3).map(post => ({
      id: post.postId,
      title: post.title,
      meta: `${post.authorDisplayName} · ${formatOverviewDateLabel(post.updatedAt)}`,
      kind: 'blog' as const,
    })),
    ...reviews.slice(0, 3).map(review => ({
      id: review.reviewId,
      title: review.title,
      meta: `${review.resourceSummaryTitle} · ${formatOverviewDateLabel(review.updatedAt)}`,
      kind: 'review' as const,
    })),
  ].slice(0, 5)
}

export function getRecommendedNextStep(params: {
  signedInUser: UserResponse | null
  travelerCount: number
  orderCount: number
  hasUpcomingTrip: boolean
  translate: (translationKey: string) => string
}): RecommendedNextStep {
  const { signedInUser, travelerCount, orderCount, hasUpcomingTrip, translate } = params

  if (!signedInUser) {
    return {
      title: translate('dashboard.nextStep.accountTitle'),
      description: translate('dashboard.nextStep.accountDescription'),
      ctaLabel: translate('dashboard.completeAccount'),
      viewKey: 'account',
    }
  }

  if (travelerCount === 0) {
    return {
      title: translate('dashboard.nextStep.travelerTitle'),
      description: translate('dashboard.nextStep.travelerDescription'),
      ctaLabel: translate('dashboard.manageTravelers'),
      viewKey: 'travelers',
    }
  }

  if (orderCount === 0) {
    return {
      title: translate('dashboard.nextStep.bookingTitle'),
      description: translate('dashboard.nextStep.bookingDescription'),
      ctaLabel: translate('dashboard.searchFlights'),
      viewKey: 'flights',
    }
  }

  if (hasUpcomingTrip) {
    return {
      title: translate('dashboard.nextStep.coordTitle'),
      description: translate('dashboard.nextStep.coordDescription'),
      ctaLabel: translate('dashboard.openTripHub'),
      viewKey: 'orders',
    }
  }

  return {
    title: translate('dashboard.nextStep.communityTitle'),
    description: translate('dashboard.nextStep.communityDescription'),
    ctaLabel: translate('nav.blog'),
    viewKey: 'blog',
  }
}

export function getDefaultTraveler(travelers: TravelerResponse[], signedInUser: UserResponse | null) {
  return (
    travelers.find(traveler => traveler.isDefault) ??
    travelers.find(traveler => traveler.travelerId === signedInUser?.defaultTravelerProfileId) ??
    travelers[0] ??
    null
  )
}

export function buildRecentOrders(orders: OrderResponse[]) {
  return [...orders]
    .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
    .slice(0, 4)
    .map(order => ({
      orderId: order.orderId,
      status: order.status,
      totalPriceLabel: `${order.totalPrice} ${order.orderCurrency}`,
      createdAtLabel: formatOverviewDateLabel(order.createdAt),
      itemCount: order.orderLineItems.length,
    }))
}

export function buildDashboardShortcuts(translate: (translationKey: string) => string): DashboardShortcut[] {
  return [
    { title: translate('nav.flights'), description: translate('dashboard.shortcut.flights'), viewKey: 'flights' },
    { title: translate('nav.hotels'), description: translate('dashboard.shortcut.hotels'), viewKey: 'hotels' },
    { title: translate('nav.trains'), description: translate('dashboard.shortcut.trains'), viewKey: 'trains' },
    { title: translate('nav.attractions'), description: translate('dashboard.shortcut.attractions'), viewKey: 'attractions' },
    { title: translate('nav.orders'), description: translate('dashboard.shortcut.orders'), viewKey: 'orders' },
    { title: translate('nav.tourGroups'), description: translate('dashboard.shortcut.tourGroups'), viewKey: 'tourGroups' },
  ]
}

export function buildHeroStats(params: {
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  defaultTravelerName: string | null
  orders: OrderResponse[]
  posts: BlogPostSummaryResponse[]
  reviews: ReviewResponse[]
  upcomingTrips: UpcomingTripSummary[]
  translate: (translationKey: string) => string
}): HeroStat[] {
  const { signedInUser, travelers, defaultTravelerName, orders, posts, reviews, upcomingTrips, translate } = params

  return [
    {
      label: translate('dashboard.hero.accountReadiness'),
      value: signedInUser ? translate('dashboard.hero.ready') : translate('dashboard.hero.pending'),
      detail: signedInUser ? signedInUser.membershipLevel : translate('guest.badge'),
    },
    {
      label: translate('dashboard.hero.travelerProfiles'),
      value: travelers.length,
      detail: defaultTravelerName ?? translate('dashboard.travelers.noDefault'),
    },
    {
      label: translate('dashboard.hero.orderCount'),
      value: orders.length,
      detail: upcomingTrips[0]?.startAtLabel ?? translate('dashboard.upcomingTrips.empty'),
    },
    {
      label: translate('dashboard.hero.communityPulse'),
      value: posts.length + reviews.length,
      detail: translate('dashboard.hero.communityPulseDetail'),
    },
  ]
}

export function shouldHideOverviewPrimaryAction(params: {
  isSessionReady: boolean
  isOverviewReady: boolean
  viewKey: AppViewKey
}) {
  const { isSessionReady, isOverviewReady, viewKey } = params
  return !isSessionReady || !isOverviewReady || viewKey === 'blog'
}

function getOrderItemUpcomingMoment(item: OrderLineItemResponse) {
  if (item.flightDetails) {
    return {
      startsAt: item.flightDetails.departureTime,
      title: `${item.flightDetails.departureAirport} → ${item.flightDetails.arrivalAirport}`,
    }
  }

  if (item.hotelDetails) {
    return {
      startsAt: `${item.hotelDetails.checkInDate}T12:00:00`,
      title: item.hotelDetails.hotelName,
    }
  }

  if (item.trainDetails) {
    return {
      startsAt: item.trainDetails.departureTime,
      title: `${item.trainDetails.fromStationName} → ${item.trainDetails.toStationName}`,
    }
  }

  if (item.attractionDetails) {
    return {
      startsAt: item.attractionDetails.sessionStartsAt ?? `${item.attractionDetails.useDate}T09:00:00`,
      title: item.attractionDetails.attractionName,
    }
  }

  return null
}

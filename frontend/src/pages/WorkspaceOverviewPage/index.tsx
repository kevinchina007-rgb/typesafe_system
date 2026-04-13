import { useEffect, useMemo, useState } from 'react'

import {
  AccountSummaryCard,
  BookingShortcutsCard,
  CommunityActivityCard,
  RecentOrdersCard,
  RecommendedNextStepCard,
  TravelerSummaryCard,
  UpcomingTripsCard,
} from '../../components/dashboard'
import type {
  CommunityActivitySummary,
  DashboardShortcut,
  HeroStat,
  RecommendedNextStep,
  UpcomingTripSummary,
} from '../../components/dashboard/types'
import { HeroBackground } from '../../components/HeroBackground'
import { ActionBar, PrimaryButton, SecondaryButton, StatCard } from '../../components/ui/UIComponents'
import { travelMvpApiClient } from '../../lib/api-client'
import type {
  AppViewKey,
  BlogPostSummaryResponse,
  OrderLineItemResponse,
  OrderResponse,
  ReviewResponse,
  TravelerResponse,
  UserResponse,
} from '../../lib/mvp-types'

type WorkspaceOverviewPageProps = {
  isSessionReady: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
}

function formatDateLabel(value: string) {
  return new Date(value).toLocaleString()
}

function buildUpcomingTripEntries(orders: OrderResponse[]): UpcomingTripSummary[] {
  const now = Date.now()
  const trips = orders.flatMap(order =>
    order.orderLineItems.flatMap(item => {
      const nextTrip = getOrderItemUpcomingMoment(item)
      if (!nextTrip) {
        return []
      }

      if (new Date(nextTrip.startsAt).getTime() < now) {
        return []
      }

      return [
        {
          id: `${order.orderId}-${item.orderItemId}`,
          title: nextTrip.title,
          subtitle: `${order.orderId} · ${item.summaryLabel}`,
          startsAt: nextTrip.startsAt,
          startAtLabel: formatDateLabel(nextTrip.startsAt),
        },
      ]
    }),
  )

  return trips
    .sort((left, right) => new Date(left.startsAt).getTime() - new Date(right.startsAt).getTime())
    .slice(0, 4)
    .map(({ startsAt: _startsAt, ...trip }) => trip)
}

function getOrderItemUpcomingMoment(item: OrderLineItemResponse) {
  if (item.flightDetails) {
    return {
      startsAt: item.flightDetails.departureTime,
      title: `${item.flightDetails.departureAirport} -> ${item.flightDetails.arrivalAirport}`,
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
      title: `${item.trainDetails.fromStationName} -> ${item.trainDetails.toStationName}`,
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

function buildCommunityActivity(posts: BlogPostSummaryResponse[], reviews: ReviewResponse[]): CommunityActivitySummary[] {
  return [
    ...posts.slice(0, 3).map(post => ({
      id: post.postId,
      title: post.title,
      meta: `${post.authorDisplayName} · ${formatDateLabel(post.updatedAt)}`,
      kind: 'blog' as const,
    })),
    ...reviews.slice(0, 3).map(review => ({
      id: review.reviewId,
      title: review.title,
      meta: `${review.resourceSummaryTitle} · ${formatDateLabel(review.updatedAt)}`,
      kind: 'review' as const,
    })),
  ].slice(0, 5)
}

function getRecommendedNextStep(params: {
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

export function WorkspaceOverviewPage({ isSessionReady, signedInUser, translate, onSelectView }: WorkspaceOverviewPageProps) {
  const [travelers, setTravelers] = useState<TravelerResponse[]>([])
  const [orders, setOrders] = useState<OrderResponse[]>([])
  const [posts, setPosts] = useState<BlogPostSummaryResponse[]>([])
  const [reviews, setReviews] = useState<ReviewResponse[]>([])
  const [isOverviewReady, setIsOverviewReady] = useState(false)

  useEffect(() => {
    let isCancelled = false

    const loadOverview = async () => {
      setIsOverviewReady(false)
      const postPromise = travelMvpApiClient.listBlogPosts('latest').then(response => response.posts).catch(() => [])

      if (!signedInUser) {
        const latestPosts = await postPromise
        if (!isCancelled) {
          setTravelers([])
          setOrders([])
          setReviews([])
          setPosts(latestPosts)
          setIsOverviewReady(true)
        }
        return
      }

      const [travelerList, orderList, postList, reviewList] = await Promise.all([
        travelMvpApiClient.listTravelers(signedInUser.userId).then(response => response.travelers).catch(() => []),
        travelMvpApiClient.listOrders(signedInUser.userId).then(response => response.orders).catch(() => []),
        postPromise,
        travelMvpApiClient.listMyReviews(signedInUser.userId).then(response => response.reviews).catch(() => []),
      ])

      if (isCancelled) {
        return
      }

      setTravelers(travelerList)
      setOrders(orderList)
      setPosts(postList)
      setReviews(reviewList)
      setIsOverviewReady(true)
    }

    void loadOverview()

    return () => {
      isCancelled = true
    }
  }, [signedInUser?.userId])

  const defaultTraveler = useMemo(
    () =>
      travelers.find(traveler => traveler.isDefault) ??
      travelers.find(traveler => traveler.travelerId === signedInUser?.defaultTravelerProfileId) ??
      travelers[0] ??
      null,
    [travelers, signedInUser?.defaultTravelerProfileId],
  )

  const recentOrders = useMemo(
    () =>
      [...orders]
        .sort((left, right) => new Date(right.createdAt).getTime() - new Date(left.createdAt).getTime())
        .slice(0, 4)
        .map(order => ({
          orderId: order.orderId,
          status: order.status,
          totalPriceLabel: `${order.totalPrice} ${order.orderCurrency}`,
          createdAtLabel: formatDateLabel(order.createdAt),
          itemCount: order.orderLineItems.length,
        })),
    [orders],
  )

  const upcomingTrips = useMemo(() => buildUpcomingTripEntries(orders), [orders])
  const communityActivity = useMemo(() => buildCommunityActivity(posts, reviews), [posts, reviews])

  const shortcuts: DashboardShortcut[] = [
    { title: translate('nav.flights'), description: translate('dashboard.shortcut.flights'), viewKey: 'flights' },
    { title: translate('nav.hotels'), description: translate('dashboard.shortcut.hotels'), viewKey: 'hotels' },
    { title: translate('nav.trains'), description: translate('dashboard.shortcut.trains'), viewKey: 'trains' },
    { title: translate('nav.attractions'), description: translate('dashboard.shortcut.attractions'), viewKey: 'attractions' },
    { title: translate('nav.orders'), description: translate('dashboard.shortcut.orders'), viewKey: 'orders' },
    { title: translate('nav.tourGroups'), description: translate('dashboard.shortcut.tourGroups'), viewKey: 'tourGroups' },
  ]

  const recommendation = getRecommendedNextStep({
    signedInUser,
    travelerCount: travelers.length,
    orderCount: orders.length,
    hasUpcomingTrip: upcomingTrips.length > 0,
    translate,
  })

  const heroStats: HeroStat[] = [
    {
      label: translate('dashboard.hero.accountReadiness'),
      value: signedInUser ? translate('dashboard.hero.ready') : translate('dashboard.hero.pending'),
      detail: signedInUser ? signedInUser.membershipLevel : translate('guest.badge'),
    },
    {
      label: translate('dashboard.hero.travelerProfiles'),
      value: travelers.length,
      detail: defaultTraveler?.fullName ?? translate('dashboard.travelers.noDefault'),
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

  return (
    <div className="workspace-overview-stack">
      <HeroBackground
        eyebrow={translate('nav.section.workspace')}
        title={translate('dashboard.hero.title')}
        subtitle={translate('dashboard.hero.subtitle')}
        actions={
          <ActionBar>
            {isSessionReady && isOverviewReady && recommendation.viewKey !== 'blog' ? (
              <PrimaryButton type="button" onClick={() => onSelectView(recommendation.viewKey)}>
                {recommendation.ctaLabel}
              </PrimaryButton>
            ) : null}
            <SecondaryButton type="button" onClick={() => onSelectView('orders')}>
              {translate('dashboard.viewOrders')}
            </SecondaryButton>
          </ActionBar>
        }
        stats={heroStats.map(stat => (
          <StatCard key={stat.label} label={stat.label} value={stat.value} detail={stat.detail} />
        ))}
      />

      <div className="workspace-overview-grid">
        <RecommendedNextStepCard
          isSessionReady={isSessionReady && isOverviewReady}
          recommendation={recommendation}
          translate={translate}
          onSelectView={onSelectView}
        />
        <AccountSummaryCard signedInUser={signedInUser} translate={translate} onSelectView={onSelectView} />
        <TravelerSummaryCard
          travelerCount={travelers.length}
          defaultTravelerName={defaultTraveler?.fullName ?? null}
          translate={translate}
          onSelectView={onSelectView}
        />
        <BookingShortcutsCard shortcuts={shortcuts} translate={translate} onSelectView={onSelectView} />
        <RecentOrdersCard orders={recentOrders} translate={translate} onSelectView={onSelectView} />
        <UpcomingTripsCard trips={upcomingTrips} translate={translate} onSelectView={onSelectView} />
        <CommunityActivityCard activities={communityActivity} translate={translate} onSelectView={onSelectView} />
      </div>
    </div>
  )
}

import { useEffect, useMemo, useState } from 'react'

import { AccountSummaryCard, BookingShortcutsCard, CommunityActivityCard, RecentOrdersCard, RecommendedNextStepCard, TravelerSummaryCard, UpcomingTripsCard } from '@/pages/WorkspaceOverviewPage/components'
import { HeroBackground } from '@/pages/shared/base/HeroBackground'
import { ActionBar, PrimaryButton, SecondaryButton, StatCard } from '@/app/ui/UIComponents'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppViewKey, BlogPostSummaryResponse, OrderResponse, ReviewResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { buildCommunityActivity, buildDashboardShortcuts, buildHeroStats, buildRecentOrders, buildUpcomingTripEntries, getDefaultTraveler, getRecommendedNextStep, shouldHideOverviewPrimaryAction } from '@/pages/WorkspaceOverviewPage/overviewModel'

type WorkspaceOverviewPageProps = {
  isSessionReady: boolean
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSelectView: (viewKey: AppViewKey) => void
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

  const defaultTraveler = useMemo(() => getDefaultTraveler(travelers, signedInUser), [travelers, signedInUser])
  const recentOrders = useMemo(() => buildRecentOrders(orders), [orders])
  const upcomingTrips = useMemo(() => buildUpcomingTripEntries(orders), [orders])
  const communityActivity = useMemo(() => buildCommunityActivity(posts, reviews), [posts, reviews])
  const shortcuts = useMemo(() => buildDashboardShortcuts(translate), [translate])

  const recommendation = getRecommendedNextStep({
    signedInUser,
    travelerCount: travelers.length,
    orderCount: orders.length,
    hasUpcomingTrip: upcomingTrips.length > 0,
    translate,
  })

  const heroStats = useMemo(
    () =>
      buildHeroStats({
        signedInUser,
        travelers,
        defaultTravelerName: defaultTraveler?.fullName ?? null,
        orders,
        posts,
        reviews,
        upcomingTrips,
        translate,
      }),
    [defaultTraveler?.fullName, orders, posts, reviews, signedInUser, travelers, translate, upcomingTrips],
  )

  return (
    <div className="workspace-overview-stack">
      <HeroBackground
        eyebrow={translate('nav.section.workspace')}
        title={translate('dashboard.hero.title')}
        subtitle={translate('dashboard.hero.subtitle')}
        actions={
          <ActionBar>
            {!shouldHideOverviewPrimaryAction({
              isSessionReady,
              isOverviewReady,
              viewKey: recommendation.viewKey,
            }) ? (
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

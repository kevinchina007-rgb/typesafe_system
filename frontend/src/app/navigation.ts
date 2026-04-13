import type { AppIcon } from '../components/icons/Icons'
import type { AppViewKey, CurrentManagerSessionResponse, UserResponse } from '../lib/mvp-types'

export type TopNavKey = 'overview' | 'booking' | 'travelManagement' | 'community' | 'smartPlanner' | 'profile'
export type NavSectionKey = 'workspace' | 'booking' | 'travelManagement' | 'community' | 'profile'

export type RoutePrerequisite = 'account' | 'traveler' | 'orders' | 'upcomingTrip'
export type RouteBadgeMap = Partial<Record<AppViewKey, number>>

export type RouteMeta = {
  viewKey: AppViewKey
  titleKey: string
  descriptionKey: string
  section: NavSectionKey
  topNav: TopNavKey
  icon: AppIcon
  sortOrder: number
  prerequisiteState?: RoutePrerequisite
  supportsGuests?: boolean
}

export type NavItem = RouteMeta & {
  badgeCount?: number
}

export type TopNavItem = {
  key: TopNavKey
  titleKey: string
  icon: AppIcon
  defaultViewKey: AppViewKey
}

export const topNavItems: TopNavItem[] = [
  { key: 'overview', titleKey: 'topnav.overview', icon: 'dashboard', defaultViewKey: 'overview' },
  { key: 'booking', titleKey: 'topnav.booking', icon: 'flight', defaultViewKey: 'flights' },
  { key: 'travelManagement', titleKey: 'topnav.travelManagement', icon: 'group', defaultViewKey: 'travelers' },
  { key: 'community', titleKey: 'topnav.community', icon: 'blog', defaultViewKey: 'blog' },
  { key: 'smartPlanner', titleKey: 'topnav.smartPlanner', icon: 'planner', defaultViewKey: 'smartPlanner' },
  { key: 'profile', titleKey: 'topnav.profile', icon: 'account', defaultViewKey: 'account' },
]

export const appRoutes: RouteMeta[] = [
  {
    viewKey: 'overview',
    titleKey: 'nav.overview',
    descriptionKey: 'workspace.overviewDescription',
    section: 'workspace',
    topNav: 'overview',
    icon: 'dashboard',
    sortOrder: 10,
    supportsGuests: true,
  },
  {
    viewKey: 'smartPlanner',
    titleKey: 'nav.smartPlanner',
    descriptionKey: 'workspace.smartPlannerDescription',
    section: 'workspace',
    topNav: 'smartPlanner',
    icon: 'planner',
    sortOrder: 10,
    supportsGuests: true,
  },
  {
    viewKey: 'orders',
    titleKey: 'nav.orders',
    descriptionKey: 'workspace.ordersDescription',
    section: 'workspace',
    topNav: 'overview',
    icon: 'orders',
    sortOrder: 20,
    prerequisiteState: 'account',
  },
  {
    viewKey: 'flights',
    titleKey: 'nav.flights',
    descriptionKey: 'booking.flightsDescription',
    section: 'booking',
    topNav: 'booking',
    icon: 'flight',
    sortOrder: 10,
    prerequisiteState: 'traveler',
    supportsGuests: true,
  },
  {
    viewKey: 'hotels',
    titleKey: 'nav.hotels',
    descriptionKey: 'booking.hotelsDescription',
    section: 'booking',
    topNav: 'booking',
    icon: 'hotel',
    sortOrder: 20,
    prerequisiteState: 'traveler',
    supportsGuests: true,
  },
  {
    viewKey: 'trains',
    titleKey: 'nav.trains',
    descriptionKey: 'booking.trainsDescription',
    section: 'booking',
    topNav: 'booking',
    icon: 'train',
    sortOrder: 30,
    prerequisiteState: 'traveler',
    supportsGuests: true,
  },
  {
    viewKey: 'attractions',
    titleKey: 'nav.attractions',
    descriptionKey: 'booking.attractionsDescription',
    section: 'booking',
    topNav: 'booking',
    icon: 'attraction',
    sortOrder: 40,
    prerequisiteState: 'traveler',
    supportsGuests: true,
  },
  {
    viewKey: 'travelers',
    titleKey: 'nav.travelers',
    descriptionKey: 'travelManagement.travelersDescription',
    section: 'travelManagement',
    topNav: 'travelManagement',
    icon: 'traveler',
    sortOrder: 10,
    prerequisiteState: 'account',
  },
  {
    viewKey: 'tourGroups',
    titleKey: 'nav.tourGroups',
    descriptionKey: 'travelManagement.tourGroupsDescription',
    section: 'travelManagement',
    topNav: 'travelManagement',
    icon: 'group',
    sortOrder: 20,
    prerequisiteState: 'orders',
  },
  {
    viewKey: 'blog',
    titleKey: 'nav.blog',
    descriptionKey: 'community.blogDescription',
    section: 'community',
    topNav: 'community',
    icon: 'blog',
    sortOrder: 10,
    supportsGuests: true,
  },
  {
    viewKey: 'reviews',
    titleKey: 'nav.reviews',
    descriptionKey: 'community.reviewsDescription',
    section: 'community',
    topNav: 'community',
    icon: 'review',
    sortOrder: 20,
    prerequisiteState: 'orders',
  },
  {
    viewKey: 'account',
    titleKey: 'nav.account',
    descriptionKey: 'profile.accountDescription',
    section: 'profile',
    topNav: 'profile',
    icon: 'account',
    sortOrder: 10,
    supportsGuests: true,
  },
]

export function getRouteByViewKey(viewKey: AppViewKey) {
  return appRoutes.find(route => route.viewKey === viewKey) ?? null
}

export function getActiveTopNav(viewKey: AppViewKey): TopNavKey {
  return getRouteByViewKey(viewKey)?.topNav ?? 'overview'
}

function isRouteVisible(params: {
  route: RouteMeta
  signedInUser: UserResponse | null
  signedInManager: CurrentManagerSessionResponse | null
}) {
  const { route, signedInUser, signedInManager } = params
  const isGuestMode = signedInUser === null && signedInManager === null
  const isManagerOnlyMode = signedInUser === null && signedInManager !== null

  if (!isGuestMode && !isManagerOnlyMode) {
    return true
  }

  if (isGuestMode) {
    if (route.viewKey === 'reviews' || route.viewKey === 'tourGroups' || route.viewKey === 'orders' || route.viewKey === 'travelers') {
      return false
    }
    return route.supportsGuests === true
  }

  if (isManagerOnlyMode) {
    return route.viewKey === 'overview' || route.viewKey === 'blog' || route.viewKey === 'account'
  }

  return true
}

export function getVisibleTopNavItems(params: {
  signedInUser: UserResponse | null
  signedInManager: CurrentManagerSessionResponse | null
}) {
  const { signedInUser, signedInManager } = params

  return topNavItems.filter(item => {
    if (signedInUser) {
      return true
    }

    if (signedInManager) {
      return item.key === 'overview' || item.key === 'community' || item.key === 'profile'
    }

    return item.key !== 'travelManagement'
  })
}

export function getSidebarItemsForTopNav(params: {
  topNav: TopNavKey
  signedInUser: UserResponse | null
  signedInManager: CurrentManagerSessionResponse | null
  badgeCounts?: RouteBadgeMap
}): NavItem[] {
  const { topNav, signedInUser, signedInManager, badgeCounts } = params

  return appRoutes
    .filter(route => route.topNav === topNav)
    .filter(route => isRouteVisible({ route, signedInUser, signedInManager }))
    .sort((left, right) => left.sortOrder - right.sortOrder)
    .map(route => ({
      ...route,
      badgeCount: badgeCounts?.[route.viewKey],
    }))
}

export function shouldShowSidebar(topNav: TopNavKey, sidebarItems: NavItem[]) {
  if (topNav === 'overview' && sidebarItems.length <= 1) {
    return false
  }

  if (topNav === 'smartPlanner' && sidebarItems.length <= 1) {
    return false
  }

  return sidebarItems.length > 0
}

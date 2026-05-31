import type { AppIcon } from '@/app/icons/Icons'
import type { AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'

export type TopNavKey =
  | 'overview'
  | 'orders'
  | 'booking'
  | 'travelManagement'
  | 'community'
  | 'smartPlanner'
  | 'customerFeedback'
  | 'userCenter'
  | 'managerCenter'
  | 'managerWorkspace'
  | 'managerCreateFlight'
  | 'managerFlightManagement'
  | 'managerFeedback'
  | 'managerProfile'
  | 'managerAdvertising'
  | 'siteAdminBlogAudit'
  | 'siteAdminAdvertisingReview'
  | 'siteAdminFeedback'

export type NavSectionKey = 'workspace' | 'booking' | 'travelManagement' | 'community' | 'userCenter' | 'managerCenter'

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
  badgeCount?: number
}

export const topNavItems: TopNavItem[] = [
  { key: 'overview', titleKey: 'topnav.overview', icon: 'dashboard', defaultViewKey: 'overview' },
  { key: 'orders', titleKey: 'topnav.orders', icon: 'orders', defaultViewKey: 'flightOrders' },
  { key: 'booking', titleKey: 'topnav.booking', icon: 'flight', defaultViewKey: 'flights' },
  { key: 'travelManagement', titleKey: 'topnav.travelManagement', icon: 'group', defaultViewKey: 'travelers' },
  { key: 'community', titleKey: 'topnav.community', icon: 'blog', defaultViewKey: 'blog' },
  { key: 'smartPlanner', titleKey: 'topnav.smartPlanner', icon: 'planner', defaultViewKey: 'smartPlanner' },
  { key: 'customerFeedback', titleKey: 'topnav.customerFeedback', icon: 'review', defaultViewKey: 'customerFeedback' },
  { key: 'managerCenter', titleKey: 'topnav.managerCenter', icon: 'account', defaultViewKey: 'manager' },
  { key: 'managerWorkspace', titleKey: 'topnav.managerWorkspace', icon: 'operations', defaultViewKey: 'managerWorkspace' },
  { key: 'managerCreateFlight', titleKey: 'topnav.managerCreateFlight', icon: 'flight', defaultViewKey: 'managerCreateFlight' },
  { key: 'managerFlightManagement', titleKey: 'topnav.managerFlightManagement', icon: 'operations', defaultViewKey: 'managerFlightManagement' },
  { key: 'managerFeedback', titleKey: 'topnav.managerFeedback', icon: 'review', defaultViewKey: 'managerFeedback' },
  { key: 'managerProfile', titleKey: 'topnav.managerProfile', icon: 'account', defaultViewKey: 'managerProfile' },
  { key: 'managerAdvertising', titleKey: 'topnav.managerAdvertising', icon: 'orders', defaultViewKey: 'managerAdvertising' },
  { key: 'siteAdminBlogAudit', titleKey: 'topnav.siteAdminBlogAudit', icon: 'blog', defaultViewKey: 'siteAdminBlogAudit' },
  { key: 'siteAdminAdvertisingReview', titleKey: 'topnav.siteAdminAdvertisingReview', icon: 'orders', defaultViewKey: 'siteAdminAdvertisingReview' },
  { key: 'siteAdminFeedback', titleKey: 'topnav.customerFeedback', icon: 'review', defaultViewKey: 'siteAdminFeedback' },
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
    viewKey: 'flightOrders',
    titleKey: 'nav.flightOrders',
    descriptionKey: 'bookings.flightDescription',
    section: 'workspace',
    topNav: 'orders',
    icon: 'flight',
    sortOrder: 10,
    prerequisiteState: 'account',
    supportsGuests: true,
  },
  {
    viewKey: 'hotelOrders',
    titleKey: 'nav.hotelOrders',
    descriptionKey: 'bookings.hotelDescription',
    section: 'workspace',
    topNav: 'orders',
    icon: 'hotel',
    sortOrder: 20,
    prerequisiteState: 'account',
    supportsGuests: true,
  },
  {
    viewKey: 'trainOrders',
    titleKey: 'nav.trainOrders',
    descriptionKey: 'bookings.trainDescription',
    section: 'workspace',
    topNav: 'orders',
    icon: 'train',
    sortOrder: 30,
    prerequisiteState: 'account',
    supportsGuests: true,
  },
  {
    viewKey: 'attractionOrders',
    titleKey: 'nav.attractionOrders',
    descriptionKey: 'bookings.attractionDescription',
    section: 'workspace',
    topNav: 'orders',
    icon: 'attraction',
    sortOrder: 40,
    prerequisiteState: 'account',
    supportsGuests: true,
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
    viewKey: 'account',
    titleKey: 'nav.account',
    descriptionKey: 'profile.accountDescription',
    section: 'userCenter',
    topNav: 'userCenter',
    icon: 'account',
    sortOrder: 10,
    supportsGuests: true,
  },
  {
    viewKey: 'customerFeedback',
    titleKey: 'nav.customerFeedback',
    descriptionKey: 'profile.customerFeedbackDescription',
    section: 'userCenter',
    topNav: 'customerFeedback',
    icon: 'review',
    sortOrder: 20,
  },
  {
    viewKey: 'manager',
    titleKey: 'nav.managerCenter',
    descriptionKey: 'manager.centerDescription',
    section: 'managerCenter',
    topNav: 'managerCenter',
    icon: 'account',
    sortOrder: 10,
    supportsGuests: true,
  },
  {
    viewKey: 'siteAdminLogin',
    titleKey: 'manager.siteAdmin.login',
    descriptionKey: 'manager.siteAdmin.title',
    section: 'managerCenter',
    topNav: 'managerCenter',
    icon: 'account',
    sortOrder: 11,
  },
  {
    viewKey: 'managerWorkspace',
    titleKey: 'nav.managerWorkspace',
    descriptionKey: 'manager.centerDescription',
    section: 'managerCenter',
    topNav: 'managerWorkspace',
    icon: 'operations',
    sortOrder: 20,
  },
  {
    viewKey: 'managerCreateFlight',
    titleKey: 'nav.managerCreateFlight',
    descriptionKey: 'manager.centerDescription',
    section: 'managerCenter',
    topNav: 'managerCreateFlight',
    icon: 'flight',
    sortOrder: 21,
  },
  {
    viewKey: 'managerFlightManagement',
    titleKey: 'nav.managerFlightManagement',
    descriptionKey: 'manager.centerDescription',
    section: 'managerCenter',
    topNav: 'managerFlightManagement',
    icon: 'operations',
    sortOrder: 22,
  },
  {
    viewKey: 'managerFeedback',
    titleKey: 'nav.managerFeedback',
    descriptionKey: 'manager.feedback.title',
    section: 'managerCenter',
    topNav: 'managerFeedback',
    icon: 'review',
    sortOrder: 30,
  },
  {
    viewKey: 'managerProfile',
    titleKey: 'nav.managerProfile',
    descriptionKey: 'manager.centerDescription',
    section: 'managerCenter',
    topNav: 'managerProfile',
    icon: 'account',
    sortOrder: 31,
  },
  {
    viewKey: 'managerAdvertising',
    titleKey: 'nav.managerAdvertising',
    descriptionKey: 'advertising.submitDescription',
    section: 'managerCenter',
    topNav: 'managerAdvertising',
    icon: 'orders',
    sortOrder: 35,
  },
  {
    viewKey: 'siteAdminBlogAudit',
    titleKey: 'nav.siteAdminBlogAudit',
    descriptionKey: 'manager.siteAdmin.blogAuditDescription',
    section: 'managerCenter',
    topNav: 'siteAdminBlogAudit',
    icon: 'blog',
    sortOrder: 40,
  },
  {
    viewKey: 'siteAdminAdvertisingReview',
    titleKey: 'nav.siteAdminFlightAdvertisingReview',
    descriptionKey: 'advertising.reviewDescription',
    section: 'managerCenter',
    topNav: 'siteAdminAdvertisingReview',
    icon: 'orders',
    sortOrder: 45,
  },
  {
    viewKey: 'siteAdminHotelAdvertisingReview',
    titleKey: 'nav.siteAdminHotelAdvertisingReview',
    descriptionKey: 'advertising.reviewDescription',
    section: 'managerCenter',
    topNav: 'siteAdminAdvertisingReview',
    icon: 'hotel',
    sortOrder: 46,
  },
  {
    viewKey: 'siteAdminTrainAdvertisingReview',
    titleKey: 'nav.siteAdminTrainAdvertisingReview',
    descriptionKey: 'advertising.reviewDescription',
    section: 'managerCenter',
    topNav: 'siteAdminAdvertisingReview',
    icon: 'train',
    sortOrder: 47,
  },
  {
    viewKey: 'siteAdminAttractionAdvertisingReview',
    titleKey: 'nav.siteAdminAttractionAdvertisingReview',
    descriptionKey: 'advertising.reviewDescription',
    section: 'managerCenter',
    topNav: 'siteAdminAdvertisingReview',
    icon: 'attraction',
    sortOrder: 48,
  },
  {
    viewKey: 'siteAdminFeedback',
    titleKey: 'nav.customerFeedback',
    descriptionKey: 'feedback.title',
    section: 'managerCenter',
    topNav: 'siteAdminFeedback',
    icon: 'review',
    sortOrder: 50,
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
    if (route.viewKey === 'reviews' || route.viewKey === 'tourGroups' || route.viewKey === 'travelers') {
      return false
    }
    return route.supportsGuests === true
  }

  if (isManagerOnlyMode) {
    if (signedInManager.managerType === 'SiteAdmin') {
      return (
        route.viewKey === 'siteAdminBlogAudit' ||
        route.viewKey === 'siteAdminAdvertisingReview' ||
        route.viewKey === 'siteAdminHotelAdvertisingReview' ||
        route.viewKey === 'siteAdminTrainAdvertisingReview' ||
        route.viewKey === 'siteAdminAttractionAdvertisingReview' ||
        route.viewKey === 'siteAdminFeedback'
      )
    }

    return (
      route.viewKey === 'managerWorkspace' ||
      route.viewKey === 'managerCreateFlight' ||
      route.viewKey === 'managerFlightManagement' ||
      route.viewKey === 'managerFeedback' ||
      route.viewKey === 'managerAdvertising'
    )
  }

  return true
}

export function getVisibleTopNavItems(params: {
  signedInUser: UserResponse | null
  signedInManager: CurrentManagerSessionResponse | null
  badgeCounts?: RouteBadgeMap
}) {
  const { signedInUser, signedInManager, badgeCounts } = params

  return topNavItems.filter(item => {
    if (signedInUser) {
      return (
        item.key !== 'managerCenter' &&
        item.key !== 'managerWorkspace' &&
        item.key !== 'managerCreateFlight' &&
        item.key !== 'managerFlightManagement' &&
        item.key !== 'managerFeedback' &&
        item.key !== 'managerProfile' &&
        item.key !== 'managerAdvertising' &&
        item.key !== 'siteAdminBlogAudit' &&
        item.key !== 'siteAdminAdvertisingReview' &&
        item.key !== 'siteAdminFeedback'
      )
    }

    if (signedInManager) {
      if (signedInManager.managerType === 'SiteAdmin') {
        return (
          item.key === 'siteAdminBlogAudit' ||
          item.key === 'siteAdminAdvertisingReview' ||
          item.key === 'siteAdminFeedback'
        )
      }

      if (signedInManager.managerType === 'Airline') {
        return (
          item.key === 'managerCreateFlight' ||
          item.key === 'managerFlightManagement' ||
          item.key === 'managerFeedback' ||
          item.key === 'managerAdvertising'
        )
      }

      if (signedInManager.managerType === 'Hotel' || signedInManager.managerType === 'Attraction') {
        if (signedInManager.managerType === 'Hotel') {
          return item.key === 'managerWorkspace' || item.key === 'managerFeedback' || item.key === 'managerAdvertising'
        }
        return item.key === 'managerWorkspace' || item.key === 'managerFeedback' || item.key === 'managerAdvertising'
      }

      return item.key === 'managerWorkspace' || item.key === 'managerFeedback' || item.key === 'managerAdvertising'
    }

    return (
      item.key === 'overview' ||
      item.key === 'orders' ||
      item.key === 'booking' ||
      item.key === 'community' ||
      item.key === 'smartPlanner' ||
      item.key === 'managerCenter'
    )
  }).map(item => ({
    ...item,
    badgeCount: badgeCounts?.[item.defaultViewKey],
  }))
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

export function shouldShowSidebar(sidebarItems: NavItem[]) {
  return sidebarItems.length > 1
}

import { AppShell } from '@/app/shell/AppShell'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import { AccountPage } from '@/pages/AccountPage'
import { AttractionsPage } from '@/pages/AttractionsPage'
import { BlogPage } from '@/pages/BlogPage'
import { BookingsPage } from '@/pages/BookingsPage'
import { CustomerFeedbackPage } from '@/pages/CustomerFeedbackPage'
import { FlightsPage } from '@/pages/FlightsPage'
import { HotelsPage } from '@/pages/HotelsPage'
import { HomePage } from '@/pages/HomePage'
import { ManagerPage } from '@/pages/ManagerPage'
import { ReviewsPage } from '@/pages/ReviewsPage'
import { SmartTripPlannerPage } from '@/pages/SmartTripPlannerPage'
import { TourGroupsPage } from '@/pages/TourGroupsPage'
import { TrainsPage } from '@/pages/TrainsPage'
import { TravelersPage } from '@/pages/TravelersPage'
import type { AppPageController } from '../objects'
import { useAppPageController } from '../hooks'

function renderCurrentPage(controller: ReturnType<typeof useAppPageController>) {
  const {
    normalizedViewKey,
    currentLanguage,
    signedInUserResponse,
    signedInManagerSessionResponse,
    hasResolvedPrincipalState,
    setAppView,
    showNotice,
  } = controller

  if (normalizedViewKey === 'smartPlanner') return <SmartTripPlannerPage translate={controller.translate} onSelectView={setAppView} />
  if (normalizedViewKey === 'overview') return <HomePage />
  if (normalizedViewKey === 'blog') return <BlogPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onShowNotice={showNotice} />
  if (normalizedViewKey === 'reviews') return <ReviewsPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onShowNotice={showNotice} />
  if (normalizedViewKey === 'account') return <AccountPage currentLanguage={currentLanguage} signedInManager={signedInManagerSessionResponse} signedInUser={signedInUserResponse} requestedEntryMode="login" translate={controller.translate} onSignedInManagerChange={controller.setCurrentManagerSession} onSignedInUserChange={controller.setCurrentUserSession} onNavigate={setAppView} onShowNotice={showNotice} />
  if (normalizedViewKey === 'customerFeedback') return <CustomerFeedbackPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onShowNotice={showNotice} />
  if (normalizedViewKey === 'travelers') return <TravelersPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onSignedInUserChange={controller.setCurrentUserSession} onShowNotice={showNotice} />
  if (normalizedViewKey === 'flights') return <FlightsPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onNavigate={setAppView} onShowNotice={showNotice} />
  if (normalizedViewKey === 'hotels') return <HotelsPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onNavigate={setAppView} onShowNotice={showNotice} />
  if (normalizedViewKey === 'trains') return <TrainsPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onNavigate={setAppView} onShowNotice={showNotice} />
  if (normalizedViewKey === 'attractions') return <AttractionsPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onNavigate={setAppView} onShowNotice={showNotice} />
  if (normalizedViewKey === 'tourGroups') return <TourGroupsPage currentLanguage={currentLanguage} signedInUser={signedInUserResponse} translate={controller.translate} onNavigate={setAppView} onShowNotice={showNotice} />
  if (normalizedViewKey === 'flightOrders' || normalizedViewKey === 'hotelOrders' || normalizedViewKey === 'trainOrders' || normalizedViewKey === 'attractionOrders') {
    return <BookingsPage currentLanguage={currentLanguage} orderCategory={normalizedViewKey} isSessionReady={hasResolvedPrincipalState} signedInUser={signedInUserResponse} translate={controller.translate} onNavigate={setAppView} onShowNotice={showNotice} />
  }
  if (normalizedViewKey === 'manager') {
    return <ManagerPage currentLanguage={currentLanguage} currentViewKey={normalizedViewKey} currentManagerSession={signedInManagerSessionResponse} signedInUser={signedInUserResponse} translate={controller.translate} onManagerSessionChange={controller.setCurrentManagerSession} onSignedInUserChange={controller.setCurrentUserSession} onNavigate={setAppView} onShowNotice={showNotice} />
  }
  if (
    normalizedViewKey === 'managerWorkspace' ||
    normalizedViewKey === 'managerCreateFlight' ||
    normalizedViewKey === 'managerFlightManagement' ||
    normalizedViewKey === 'managerFeedback' ||
    normalizedViewKey === 'managerProfile' ||
    normalizedViewKey === 'managerAdvertising' ||
    normalizedViewKey === 'siteAdminBlogAudit' ||
    normalizedViewKey === 'siteAdminAdvertisingReview'
  ) {
    return <ManagerPage currentLanguage={currentLanguage} currentViewKey={normalizedViewKey} currentManagerSession={signedInManagerSessionResponse} signedInUser={signedInUserResponse} translate={controller.translate} onManagerSessionChange={controller.setCurrentManagerSession} onSignedInUserChange={controller.setCurrentUserSession} onNavigate={setAppView} onShowNotice={showNotice} />
  }
  return null
}

export function AppPageShell({ controller }: { controller: AppPageController }) {
  return (
    <AppShell
      isHomePage={controller.normalizedViewKey === 'overview'}
      topNav={{
        currentTopNav: controller.currentTopNav,
        items: controller.topNavItems,
        signedInManager: controller.signedInManagerSessionResponse,
        signedInUser: controller.signedInUserResponse,
        onSelectTopNav: (_topNav, defaultViewKey) => controller.setAppView(defaultViewKey),
        submenuItemsByTopNav: controller.submenuItemsByTopNav,
        currentViewKey: controller.normalizedViewKey,
        onSelectView: controller.setAppView,
        onUploadUserAvatar: async avatarFile => {
          await controller.runHeaderAccountAction(async () => {
            if (!controller.signedInUserResponse) throw new Error(controller.translate('error.loginRequired'))
            const updatedAccount = await travelMvpApiClient.uploadUserAvatar(controller.signedInUserResponse.userId, avatarFile)
            controller.setCurrentUserSession(updatedAccount)
          }, controller.translate('notice.avatarUploaded'))
        },
        onUseDefaultUserAvatar: async avatarUrl => {
          await controller.runHeaderAccountAction(async () => {
            if (!controller.signedInUserResponse) throw new Error(controller.translate('error.loginRequired'))
            const updatedAccount = await travelMvpApiClient.uploadUserAvatar(controller.signedInUserResponse.userId, avatarUrl)
            controller.setCurrentUserSession(updatedAccount)
          }, controller.translate('notice.avatarUploaded'))
        },
        onUpdateUserProfile: async payload => {
          await controller.runHeaderAccountAction(async () => {
            if (!controller.signedInUserResponse) throw new Error(controller.translate('error.loginRequired'))
            const updatedAccount = await travelMvpApiClient.updateUserProfile({
              userId: controller.signedInUserResponse.userId,
              nickname: payload.nickname,
              phone: payload.phone,
            })
            controller.setCurrentUserSession(updatedAccount)
          }, controller.translate('notice.actionSuccess'))
        },
        onChangeUserPassword: async payload => {
          await controller.runHeaderAccountAction(async () => {
            await travelMvpApiClient.changeUserPassword(payload)
          }, controller.translate('notice.passwordChanged'))
        },
        onLogoutUser: () => {
          void controller.runHeaderAccountAction(async () => {
            await travelMvpApiClient.logoutUser()
            controller.setCurrentUserSession(null)
            controller.setAppView('overview')
          }, controller.translate('notice.logoutSuccess'))
        },
        onValidationError: message => {
          controller.showNotice('error', controller.translate('error.friendly.default'), message)
        },
        translate: controller.translate,
      }}
    >
      {renderCurrentPage(controller)}
    </AppShell>
  )
}

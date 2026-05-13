import type { PageNoticeHandler } from '@/pages/shared/usePageActions'
﻿import { useEffect, useState } from 'react'

import { setCurrentUserTravelers, useUserStore } from '@/app/stores/user-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AppLanguage, AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import { usePageActions } from '@/pages/shared/usePageActions'
import { UserPanel } from './UserPanel'

type AccountEntryMode = 'register' | 'login'

type AccountPageProps = {
  currentLanguage: AppLanguage
  signedInUser: UserResponse | null
  signedInManager: CurrentManagerSessionResponse | null
  requestedEntryMode: AccountEntryMode
  translate: (translationKey: string) => string
  onSignedInUserChange: (user: UserResponse | null) => void
  onSignedInManagerChange: (managerSession: CurrentManagerSessionResponse | null) => void
  onNavigate: (viewKey: AppViewKey) => void
  onShowNotice: PageNoticeHandler
}

export function AccountPage({
  currentLanguage,
  signedInUser,
  signedInManager,
  requestedEntryMode,
  translate,
  onSignedInUserChange,
  onSignedInManagerChange,
  onNavigate,
  onShowNotice,
}: AccountPageProps) {
  const [accountEntryMode, setAccountEntryMode] = useState<AccountEntryMode>(requestedEntryMode)
  const [loginEmailDraft, setLoginEmailDraft] = useState('')
  const userTravelers = useUserStore(state => state.userTravelers)
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  useEffect(() => {
    setAccountEntryMode(requestedEntryMode)
  }, [requestedEntryMode])

  useEffect(() => {
    if (!signedInUser) {
      setCurrentUserTravelers([])
      return
    }
    void travelMvpApiClient.listTravelers(signedInUser.userId).then(response => setCurrentUserTravelers(response.travelers)).catch(() => setCurrentUserTravelers([]))
  }, [signedInUser?.userId])

  async function ensureManagerLoggedOut() {
    if (!signedInManager) {
      return
    }

    await travelMvpApiClient.logoutManagerAuth()
    onSignedInManagerChange(null)
  }

  return (
    <UserPanel
      account={signedInUser}
      currentLanguage={currentLanguage}
      accountEntryMode={accountEntryMode}
      isBusy={isBusy}
      isGuestMode={signedInUser === null}
      loginEmailDraft={loginEmailDraft}
      travelers={userTravelers}
      translate={translate}
      onChangeAccountEntryMode={setAccountEntryMode}
      onChangeLoginEmailDraft={setLoginEmailDraft}
      onRegisterAccount={async payload => {
        await runPageAction(async () => {
          await ensureManagerLoggedOut()
          const createdSession = await travelMvpApiClient.signupUser(payload)
          onSignedInUserChange(createdSession.user)
          setLoginEmailDraft(createdSession.user.email)
          onNavigate('travelers')
        }, translate('account.create'), translate('notice.registerSuccess'))
      }}
      onLoginAccount={async payload => {
        await runPageAction(async () => {
          await ensureManagerLoggedOut()
          const nextSignedInSession = await travelMvpApiClient.loginUserWithPassword(payload)
          onSignedInUserChange(nextSignedInSession.user)
          onNavigate('travelers')
        }, translate('account.login'), translate('notice.loginSuccess'))
      }}
      onUploadAvatar={async avatarFile => {
        if (!signedInUser) {
          throw new Error(translate('error.loginRequired'))
        }
        await runPageAction(async () => {
          const updatedAccount = await travelMvpApiClient.uploadUserAvatar(signedInUser.userId, avatarFile)
          onSignedInUserChange(updatedAccount)
        }, translate('account.avatarUpload'), translate('notice.avatarUploaded'))
      }}
      onAvatarValidationError={message => {
        onShowNotice('error', translate('error.friendly.default'), message)
      }}
      onValidationError={message => {
        onShowNotice('error', translate('error.friendly.default'), message)
      }}
      onRefreshAccount={async () => {
        if (!signedInUser) {
          return
        }
        await runPageAction(async () => {
          const refreshedAccount = await travelMvpApiClient.getUser(signedInUser.userId)
          const refreshedTravelers = await travelMvpApiClient.listTravelers(signedInUser.userId)
          onSignedInUserChange(refreshedAccount)
          setCurrentUserTravelers(refreshedTravelers.travelers)
        }, translate('account.refresh'), translate('notice.actionSuccess'))
      }}
      onChangePassword={async payload => {
        await runPageAction(async () => {
          await travelMvpApiClient.changeUserPassword(payload)
        }, translate('account.changePassword'), translate('notice.passwordChanged'))
      }}
      onLogoutCurrentSession={() => {
        void runPageAction(async () => {
          await travelMvpApiClient.logoutCurrentUserSession()
          onSignedInUserChange(null)
          onNavigate('account')
        }, translate('account.logoutCurrentSession'), translate('notice.logoutSuccess'))
      }}
      onLogout={() => {
        void runPageAction(async () => {
          await travelMvpApiClient.logoutUser()
          onSignedInUserChange(null)
          onNavigate('account')
        }, translate('account.logout'), translate('notice.logoutSuccess'))
      }}
    />
  )
}

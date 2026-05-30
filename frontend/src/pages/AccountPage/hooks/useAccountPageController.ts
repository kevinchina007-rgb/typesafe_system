import { useEffect, useState } from 'react'

import { setCurrentUserTravelers } from '@/app/stores/user-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { AccountPageController, AccountPageProps, AccountEntryMode } from '../objects'
import { createProfileDraft } from '../functions'
import { usePageActions } from '@/pages/shared/usePageActions'

export function useAccountPageController({
  currentLanguage,
  signedInUser,
  signedInManager,
  requestedEntryMode,
  translate,
  onSignedInUserChange,
  onSignedInManagerChange,
  onNavigate,
  onShowNotice,
}: AccountPageProps): AccountPageController {
  const [accountEntryMode, setAccountEntryMode] = useState<AccountEntryMode>(requestedEntryMode)
  const [loginEmailDraft, setLoginEmailDraft] = useState('')
  const [loginPasswordDraft, setLoginPasswordDraft] = useState('')
  const [isLoginFormWritable, setIsLoginFormWritable] = useState(false)
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
  const [isProfileEditing, setIsProfileEditing] = useState(false)
  const [profileDraft, setProfileDraft] = useState(createProfileDraft(signedInUser))
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  useEffect(() => {
    setAccountEntryMode(requestedEntryMode)
    setLoginEmailDraft('')
    setLoginPasswordDraft('')
    setIsLoginFormWritable(false)
  }, [requestedEntryMode])

  useEffect(() => {
    if (!signedInUser) {
      setCurrentUserTravelers([])
      return
    }
    void travelMvpApiClient.listTravelers(signedInUser.userId).then(response => setCurrentUserTravelers(response.travelers)).catch(() => setCurrentUserTravelers([]))
  }, [signedInUser?.userId])

  useEffect(() => {
    setProfileDraft(createProfileDraft(signedInUser))
  }, [signedInUser?.nickname, signedInUser?.phone])

  async function ensureManagerLoggedOut() {
    if (!signedInManager) {
      return
    }
    await travelMvpApiClient.logoutManagerAuth()
    onSignedInManagerChange(null)
  }

  function switchAccountEntryMode(nextAccountEntryMode: AccountEntryMode) {
    setLoginEmailDraft('')
    setLoginPasswordDraft('')
    setIsLoginFormWritable(false)
    setAccountEntryMode(nextAccountEntryMode)
  }

  return {
    isBusy,
    accountEntryMode,
    loginEmailDraft,
    loginPasswordDraft,
    isLoginFormWritable,
    isChangePasswordOpen,
    isProfileEditing,
    profileDraft,
    setAccountEntryMode,
    setLoginEmailDraft,
    setLoginPasswordDraft,
    setIsLoginFormWritable,
    setIsChangePasswordOpen,
    setIsProfileEditing,
    setProfileDraft,
    switchAccountEntryMode,
    onRegisterAccount: async payload => {
      await runPageAction(async () => {
        await ensureManagerLoggedOut()
        const createdSession = await travelMvpApiClient.signupUser(payload)
        onSignedInUserChange(createdSession.user)
        setLoginEmailDraft(createdSession.user.email)
        onNavigate('travelers')
      }, translate('account.create'), translate('notice.registerSuccess'))
    },
    onLoginAccount: async payload => {
      await runPageAction(async () => {
        await ensureManagerLoggedOut()
        const nextSignedInSession = await travelMvpApiClient.loginUserWithPassword(payload)
        onSignedInUserChange(nextSignedInSession.user)
        onNavigate('travelers')
      }, translate('account.login'), translate('notice.loginSuccess'))
    },
    onUploadAvatar: async avatarFile => {
      if (!signedInUser) throw new Error(translate('error.loginRequired'))
      await runPageAction(async () => {
        const updatedAccount = await travelMvpApiClient.uploadUserAvatar(signedInUser.userId, avatarFile)
        onSignedInUserChange(updatedAccount)
      }, translate('account.avatarUpload'), translate('notice.avatarUploaded'))
    },
    onUseDefaultAvatar: async avatarUrl => {
      if (!signedInUser) throw new Error(translate('error.loginRequired'))
      await runPageAction(async () => {
        const updatedAccount = await travelMvpApiClient.uploadUserAvatar(signedInUser.userId, avatarUrl)
        onSignedInUserChange(updatedAccount)
      }, translate('account.changeAvatar'), translate('notice.avatarUploaded'))
    },
    onUpdateProfile: async payload => {
      if (!signedInUser) throw new Error(translate('error.loginRequired'))
      await runPageAction(async () => {
        const updatedAccount = await travelMvpApiClient.updateUserProfile({
          userId: signedInUser.userId,
          nickname: payload.nickname,
          phone: payload.phone,
        })
        onSignedInUserChange(updatedAccount)
      }, translate('account.editProfile'), translate('notice.actionSuccess'))
    },
    onAvatarValidationError: message => {
      onShowNotice('error', translate('error.friendly.default'), message)
    },
    onValidationError: message => {
      onShowNotice('error', translate('error.friendly.default'), message)
    },
    onChangePassword: async payload => {
      await runPageAction(async () => {
        await travelMvpApiClient.changeUserPassword(payload)
      }, translate('account.changePassword'), translate('notice.passwordChanged'))
    },
    onLogoutCurrentSession: () => {
      void runPageAction(async () => {
        await travelMvpApiClient.logoutCurrentUserSession()
        onSignedInUserChange(null)
        onNavigate('account')
      }, translate('account.logoutCurrentSession'), translate('notice.logoutSuccess'))
    },
    onLogout: () => {
      void runPageAction(async () => {
        await travelMvpApiClient.logoutUser()
        onSignedInUserChange(null)
        onNavigate('account')
      }, translate('account.logout'), translate('notice.logoutSuccess'))
    },
  }
}

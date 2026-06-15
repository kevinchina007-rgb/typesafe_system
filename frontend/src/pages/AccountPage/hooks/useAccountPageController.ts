import { useEffect, useState } from 'react'

import { setCurrentUserTravelers } from '@/app/stores/user-store'
import { travelMvpApiClient } from '@/microservices/TravelMvpApiClient'
import type { TravelerListPlannerResponse } from '@/microservices/traveler/objects/TravelerListPlannerResponse'
import type { AccountPageController, AccountPageProps, AccountEntryMode } from '../objects'
import { createProfileDraft } from '../functions'
import { usePageActions } from '@/pages/shared/usePageActions'

// 账户页控制器，负责页面状态和用户操作的统一编排。
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

  // 切换入口模式时，清空登录草稿并重置可编辑状态。
  useEffect(() => {
    setAccountEntryMode(requestedEntryMode)
    setLoginEmailDraft('')
    setLoginPasswordDraft('')
    setIsLoginFormWritable(false)
  }, [requestedEntryMode])

  // 当前用户变化后，刷新当前用户对应的出行人列表。
  useEffect(() => {
    if (!signedInUser) {
      setCurrentUserTravelers([])
      return
    }
    void travelMvpApiClient
      .listTravelers(signedInUser.userId)
      .then((response: TravelerListPlannerResponse) => setCurrentUserTravelers(response.travelers))
      .catch(() => setCurrentUserTravelers([]))
  }, [signedInUser?.userId])

  // 用户资料变化后，重新生成个人资料草稿。
  useEffect(() => {
    setProfileDraft(createProfileDraft(signedInUser))
  }, [signedInUser?.nickname, signedInUser?.phone])

  // 如果当前已经登录了管理员，先把管理员会话退出。
  async function ensureManagerLoggedOut() {
    if (!signedInManager) {
      return
    }
    await travelMvpApiClient.logoutManagerAuth()
    onSignedInManagerChange(null)
  }

  // 切换登录/注册模式时，同时清空输入内容。
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
    // 处理注册请求，成功后切换到出行人页。
    onRegisterAccount: async payload => {
      await runPageAction(async () => {
        await ensureManagerLoggedOut()
        const createdSession = await travelMvpApiClient.signupUser(payload)
        onSignedInUserChange(createdSession.user)
        setLoginEmailDraft(createdSession.user.email)
        onNavigate('travelers')
      }, translate('account.create'), translate('notice.registerSuccess'))
    },
    // 处理登录请求，成功后切换到出行人页。
    onLoginAccount: async payload => {
      await runPageAction(async () => {
        await ensureManagerLoggedOut()
        const nextSignedInSession = await travelMvpApiClient.loginUserWithPassword(payload)
        onSignedInUserChange(nextSignedInSession.user)
        onNavigate('travelers')
      }, translate('account.login'), translate('notice.loginSuccess'))
    },
    // 上传头像并把返回结果同步到当前登录态。
    onUploadAvatar: async avatarFile => {
      if (!signedInUser) throw new Error(translate('error.loginRequired'))
      await runPageAction(async () => {
        const updatedAccount = await travelMvpApiClient.uploadUserAvatarPlanner(signedInUser.userId, avatarFile)
        onSignedInUserChange(updatedAccount)
      }, translate('account.avatarUpload'), translate('notice.avatarUploaded'))
    },
    // 使用默认头像地址覆盖当前头像。
    onUseDefaultAvatar: async avatarUrl => {
      if (!signedInUser) throw new Error(translate('error.loginRequired'))
      await runPageAction(async () => {
        const updatedAccount = await travelMvpApiClient.uploadUserAvatarPlanner(signedInUser.userId, avatarUrl)
        onSignedInUserChange(updatedAccount)
      }, translate('account.changeAvatar'), translate('notice.avatarUploaded'))
    },
    // 修改昵称和手机号，并把结果同步回页面状态。
    onUpdateProfile: async payload => {
      if (!signedInUser) throw new Error(translate('error.loginRequired'))
      await runPageAction(async () => {
        const updatedAccount = await travelMvpApiClient.updateUserProfilePlanner({
          userId: signedInUser.userId,
          nickname: payload.nickname,
          phone: payload.phone,
        })
        onSignedInUserChange(updatedAccount)
      }, translate('account.editProfile'), translate('notice.actionSuccess'))
    },
    // 头像文件校验失败时，统一转成提示消息。
    onAvatarValidationError: message => {
      onShowNotice('error', translate('error.friendly.default'), message)
    },
    // 通用表单校验失败提示。
    onValidationError: message => {
      onShowNotice('error', translate('error.friendly.default'), message)
    },
    // 修改登录密码。
    onChangePassword: async payload => {
      await runPageAction(async () => {
        await travelMvpApiClient.changeUserPassword(payload)
      }, translate('account.changePassword'), translate('notice.passwordChanged'))
    },
    // 仅退出当前用户会话，不影响其他会话。
    onLogoutCurrentSession: () => {
      void runPageAction(async () => {
        await travelMvpApiClient.logoutCurrentUserSession()
        onSignedInUserChange(null)
        onNavigate('account')
      }, translate('account.logoutCurrentSession'), translate('notice.logoutSuccess'))
    },
    // 退出当前用户的全部会话。
    onLogout: () => {
      void runPageAction(async () => {
        await travelMvpApiClient.logoutUser()
        onSignedInUserChange(null)
        onNavigate('account')
      }, translate('account.logout'), translate('notice.logoutSuccess'))
    },
  }
}

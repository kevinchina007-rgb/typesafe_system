import type { AppLanguage, AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

// 账号页支持的入口模式。
export type AccountEntryMode = 'register' | 'login'

// 账号页向外暴露的页面参数。
export type AccountPageProps = {
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

// 账号页中可编辑的个人资料草稿。
export type AccountProfileDraft = {
  nickname: string
  phone: string
}

// 账号页控制器对组件暴露的状态和动作。
export type AccountPageController = {
  isBusy: boolean
  accountEntryMode: AccountEntryMode
  loginEmailDraft: string
  loginPasswordDraft: string
  isLoginFormWritable: boolean
  isChangePasswordOpen: boolean
  isProfileEditing: boolean
  profileDraft: AccountProfileDraft
  setAccountEntryMode: (mode: AccountEntryMode) => void
  setLoginEmailDraft: (email: string) => void
  setLoginPasswordDraft: (password: string) => void
  setIsLoginFormWritable: (writable: boolean | ((current: boolean) => boolean)) => void
  setIsChangePasswordOpen: (open: boolean | ((current: boolean) => boolean)) => void
  setIsProfileEditing: (editing: boolean | ((current: boolean) => boolean)) => void
  setProfileDraft: (draft: AccountProfileDraft | ((current: AccountProfileDraft) => AccountProfileDraft)) => void
  switchAccountEntryMode: (mode: AccountEntryMode) => void
  onRegisterAccount: (payload: { email: string; nickname: string; phone: string; password: string }) => Promise<void>
  onLoginAccount: (payload: { email: string; password: string }) => Promise<void>
  onUploadAvatar: (avatarFile: File) => Promise<void>
  onUseDefaultAvatar: (avatarUrl: string) => Promise<void>
  onUpdateProfile: (payload: { nickname: string; phone: string }) => Promise<void>
  onAvatarValidationError: (message: string) => void
  onValidationError: (message: string) => void
  onChangePassword: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onLogoutCurrentSession: () => void
  onLogout: () => void
}

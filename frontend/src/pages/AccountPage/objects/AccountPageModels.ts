import type { AppLanguage, AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import type { PageNoticeHandler } from '@/pages/shared/usePageActions'

export type AccountEntryMode = 'register' | 'login'

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

export type AccountProfileDraft = {
  nickname: string
  phone: string
}

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

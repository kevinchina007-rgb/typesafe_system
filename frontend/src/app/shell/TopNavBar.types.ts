import type { Dispatch, SetStateAction } from 'react'

import type { AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'

import type { NavItem, TopNavItem, TopNavKey } from '@/app/navigation'

export type ManagerProfileOverride = {
  displayName?: string
  logoAssetPath?: string | null
}

export type TopNavBarProps = {
  currentTopNav: TopNavKey
  signedInManager: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  items: TopNavItem[]
  submenuItemsByTopNav: Record<string, NavItem[]>
  currentViewKey: AppViewKey
  onSelectTopNav: (topNav: TopNavKey, defaultViewKey: AppViewKey) => void
  onSelectView: (viewKey: AppViewKey) => void
  onLogoClick?: () => void
  onUploadUserAvatar?: (avatarFile: File) => Promise<void>
  onUseDefaultUserAvatar?: (avatarUrl: string) => Promise<void>
  onUpdateUserProfile?: (payload: { nickname: string; phone: string }) => Promise<void>
  onUpdateManagerProfile?: (payload: { displayName: string; logoAssetPath?: string | null }) => Promise<void>
  onChangeUserPassword?: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onChangeManagerPassword?: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onLogoutUser?: () => void
  onLogoutManager?: () => void
  onValidationError?: (message: string) => void
  translate: (translationKey: string) => string
  isOverlay?: boolean
}

export type TopNavBrandProps = {
  onLogoClick?: () => void
  isOverlay?: boolean
}

export type TopNavLinksProps = {
  currentTopNav: TopNavKey
  currentViewKey: AppViewKey
  items: TopNavItem[]
  submenuItemsByTopNav: Record<string, NavItem[]>
  hoveredTopNav: TopNavKey | null
  setHoveredTopNav: Dispatch<SetStateAction<TopNavKey | null>>
  onSelectTopNav: (topNav: TopNavKey, defaultViewKey: AppViewKey) => void
  onSelectView: (viewKey: AppViewKey) => void
  translate: (translationKey: string) => string
  isOverlay?: boolean
}

export type TopNavAccountMenuProps = {
  signedInManager: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  onSelectView: (viewKey: AppViewKey) => void
  onUploadUserAvatar?: (avatarFile: File) => Promise<void>
  onUseDefaultUserAvatar?: (avatarUrl: string) => Promise<void>
  onUpdateUserProfile?: (payload: { nickname: string; phone: string }) => Promise<void>
  onUpdateManagerProfile?: (payload: { displayName: string; logoAssetPath?: string | null }) => Promise<void>
  onChangeUserPassword?: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onChangeManagerPassword?: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onLogoutUser?: () => void
  onLogoutManager?: () => void
  onValidationError?: (message: string) => void
  translate: (translationKey: string) => string
  isOverlay?: boolean
}

export type TopNavAccountAvatarUploaderProps = {
  avatarUrl: string | null
  displayName: string
  isBusy: boolean
  onUploadAvatar: (avatarFile: File) => Promise<void>
  onValidationError: (message: string) => void
  translate: (translationKey: string) => string
}

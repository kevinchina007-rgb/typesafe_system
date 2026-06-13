import { useState } from 'react'

import type { TopNavBarProps } from '@/app/shell/TopNavBar.types'
import { TopNavAccountMenu } from '@/app/shell/TopNavAccountMenu'
import { TopNavBrand } from '@/app/shell/TopNavBrand'
import { TopNavLanguageSwitch } from '@/app/shell/TopNavLanguageSwitch'
import { TopNavLinks } from '@/app/shell/TopNavLinks'
import { TopNavNotifications } from '@/app/shell/TopNavNotifications'
import type { TopNavKey } from '@/app/navigation'

export function TopNavBar({
  currentTopNav,
  signedInManager,
  signedInUser,
  items,
  submenuItemsByTopNav,
  currentViewKey,
  onSelectTopNav,
  onSelectView,
  onLogoClick,
  onUploadUserAvatar,
  onUseDefaultUserAvatar,
  onUpdateUserProfile,
  onUpdateManagerProfile,
  onChangeUserPassword,
  onChangeManagerPassword,
  onLogoutUser,
  onLogoutManager,
  onValidationError,
  translate,
  isOverlay = false,
}: TopNavBarProps) {
  const [hoveredTopNav, setHoveredTopNav] = useState<TopNavKey | null>(null)

  return (
    <header
      className={
        isOverlay
          ? 'absolute inset-x-0 top-0 z-40 flex h-28 items-center justify-between px-8 text-white md:px-14'
          : 'relative z-40 flex h-28 items-center justify-between border-b border-slate-950 bg-white px-6 text-slate-950 shadow-none md:px-14'
      }
    >
      <TopNavBrand onLogoClick={onLogoClick} isOverlay={isOverlay} />

      <TopNavLinks
        currentTopNav={currentTopNav}
        currentViewKey={currentViewKey}
        items={items}
        submenuItemsByTopNav={submenuItemsByTopNav}
        hoveredTopNav={hoveredTopNav}
        setHoveredTopNav={setHoveredTopNav}
        onSelectTopNav={onSelectTopNav}
        onSelectView={onSelectView}
        translate={translate}
        isOverlay={isOverlay}
      />

      <TopNavLanguageSwitch />
      <TopNavNotifications />

      <TopNavAccountMenu
        signedInManager={signedInManager}
        signedInUser={signedInUser}
        onSelectView={onSelectView}
        onUploadUserAvatar={onUploadUserAvatar}
        onUseDefaultUserAvatar={onUseDefaultUserAvatar}
        onUpdateUserProfile={onUpdateUserProfile}
        onUpdateManagerProfile={onUpdateManagerProfile}
        onChangeUserPassword={onChangeUserPassword}
        onChangeManagerPassword={onChangeManagerPassword}
        onLogoutUser={onLogoutUser}
        onLogoutManager={onLogoutManager}
        onValidationError={onValidationError}
        translate={translate}
        isOverlay={isOverlay}
      />
    </header>
  )
}

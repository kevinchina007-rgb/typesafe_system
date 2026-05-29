import { useEffect, useRef, useState } from 'react'
import { ChevronDown, KeyRound, LogOut, Settings, Ticket, UserRound } from 'lucide-react'

import type { AppViewKey, CurrentManagerSessionResponse, UserResponse } from '@/lib/mvp-types/index'
import type { NavItem, TopNavItem, TopNavKey } from '@/app/navigation'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { TopNavButton } from '@/app/shell/TopNavButton'
import { AvatarUploader } from '@/pages/AccountPage/AvatarUploader'

type TopNavBarProps = {
  currentTopNav: TopNavKey
  signedInManager: CurrentManagerSessionResponse | null
  signedInUser: UserResponse | null
  items: TopNavItem[]
  submenuItemsByTopNav: Record<string, NavItem[]>
  currentViewKey: AppViewKey
  onSelectTopNav: (topNav: TopNavKey, defaultViewKey: AppViewKey) => void
  onSelectView: (viewKey: AppViewKey) => void
  onUploadUserAvatar?: (avatarFile: File) => Promise<void>
  onUseDefaultUserAvatar?: (avatarUrl: string) => Promise<void>
  onUpdateUserProfile?: (payload: { nickname: string; phone: string }) => Promise<void>
  onChangeUserPassword?: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onLogoutUser?: () => void
  onValidationError?: (message: string) => void
  translate: (translationKey: string) => string
  isOverlay?: boolean
}

function getInitials(label: string) {
  return label.slice(0, 2).toUpperCase()
}

export function TopNavBar({
  currentTopNav,
  signedInManager,
  signedInUser,
  items,
  submenuItemsByTopNav,
  currentViewKey,
  onSelectTopNav,
  onSelectView,
  onUploadUserAvatar,
  onUseDefaultUserAvatar,
  onUpdateUserProfile,
  onChangeUserPassword,
  onLogoutUser,
  onValidationError,
  translate,
  isOverlay = false,
}: TopNavBarProps) {
  const popoverRef = useRef<HTMLDivElement | null>(null)
  const [isUserPopoverOpen, setIsUserPopoverOpen] = useState(false)
  const [isProfileEditing, setIsProfileEditing] = useState(false)
  const [isPasswordEditing, setIsPasswordEditing] = useState(false)
  const [nicknameDraft, setNicknameDraft] = useState(signedInUser?.nickname ?? '')
  const identityLabel = signedInUser?.nickname ?? signedInManager?.displayName ?? translate('guest.badge')
  const avatarUrl = signedInUser?.avatarUrl ?? signedInManager?.logoAssetPath ?? null

  useEffect(() => {
    setNicknameDraft(signedInUser?.nickname ?? '')
  }, [signedInUser?.nickname])

  useEffect(() => {
    if (!isUserPopoverOpen) {
      return
    }

    function closeOnOutsideClick(event: MouseEvent) {
      if (popoverRef.current && !popoverRef.current.contains(event.target as Node)) {
        setIsUserPopoverOpen(false)
      }
    }

    document.addEventListener('mousedown', closeOnOutsideClick)
    return () => document.removeEventListener('mousedown', closeOnOutsideClick)
  }, [isUserPopoverOpen])

  function showValidationError(message: string) {
    onValidationError?.(message)
  }

  return (
    <header className={isOverlay
      ? 'absolute inset-x-0 top-0 z-20 flex h-28 items-center justify-between px-8 text-white md:px-14'
      : 'z-20 flex h-28 items-center justify-between border-b border-slate-950 bg-white px-6 text-slate-950 shadow-none md:px-14'
    }>
      <div className="grid min-w-0">
        <img className="h-18 w-auto max-w-[24rem] object-contain object-left md:h-20 md:max-w-[26rem]" src="/images/fly-pig-logo.png" alt="fly pig" />
      </div>

      <nav
        className="flex items-center gap-3"
        aria-label={translate('topnav.aria')}
      >
        {items.map(item => (
          <div key={item.key} className="group relative">
            <TopNavButton
              badgeCount={item.badgeCount}
              icon={item.icon}
              isActive={currentTopNav === item.key}
              label={translate(item.titleKey)}
              targetViewKey={item.defaultViewKey}
              onSelect={viewKey => onSelectTopNav(item.key, viewKey)}
              isOverlay={isOverlay}
            />
            {(submenuItemsByTopNav[item.key]?.length ?? 0) > 1 ? (
              <>
                <div className="absolute left-1/2 top-full z-30 hidden h-3 min-w-48 -translate-x-1/2 group-hover:block" />
                <div className={isOverlay
                  ? 'absolute left-1/2 top-[calc(100%+0.65rem)] z-30 hidden min-w-48 -translate-x-1/2 bg-white/95 py-4 shadow-xl backdrop-blur-xl group-hover:grid'
                  : 'absolute left-1/2 top-[calc(100%+0.65rem)] z-30 hidden min-w-48 -translate-x-1/2 bg-black py-4 shadow-xl group-hover:grid'
                }>
                  {submenuItemsByTopNav[item.key].map(submenuItem => (
                    <button
                      key={submenuItem.viewKey}
                      type="button"
                      className={isOverlay
                        ? `w-full justify-center !border-0 ![background:transparent] px-4 py-3 !text-slate-900 !shadow-none ![transform:none] hover:![background:rgba(15,23,42,0.06)] hover:!text-slate-500 hover:![box-shadow:none] hover:![transform:none] ${
                            currentViewKey === submenuItem.viewKey ? 'font-medium' : ''
                          }`
                        : `w-full justify-center !border-0 ![background:transparent] px-4 py-3 !text-white !shadow-none ![transform:none] hover:![background:rgba(255,255,255,0.12)] hover:!text-white hover:![box-shadow:none] hover:![transform:none] ${
                            currentViewKey === submenuItem.viewKey ? 'font-semibold' : ''
                          }`
                      }
                      onClick={() => onSelectView(submenuItem.viewKey)}
                    >
                      {translate(submenuItem.titleKey)}
                    </button>
                  ))}
                </div>
              </>
            ) : null}
          </div>
        ))}
      </nav>

      <div ref={popoverRef} className="relative flex items-center">
        <button
          type="button"
          className={isOverlay
            ? 'inline-flex items-center gap-3 rounded-none border border-white/25 bg-white/10 px-3 py-2 text-white transition hover:bg-white/20'
            : 'inline-flex items-center gap-3 border border-slate-200 bg-white px-3 py-2 text-slate-500 transition hover:border-black hover:text-slate-950'
          }
          onClick={() => {
            if (!signedInUser && !signedInManager) {
              onSelectView('account')
              return
            }
            setIsUserPopoverOpen(open => !open)
          }}
        >
          {avatarUrl ? (
            <BackendAssetImage
              className="inline-flex h-10 w-10 items-center justify-center border border-slate-200 bg-white object-contain"
              assetUrl={avatarUrl}
              alt={identityLabel}
              fallbackContent={getInitials(identityLabel)}
            />
          ) : (
            <span className="inline-flex h-10 w-10 items-center justify-center border border-slate-200 bg-indigo-100 text-slate-700">
              {getInitials(identityLabel)}
            </span>
          )}
          <span>{identityLabel}</span>
          <ChevronDown className="h-4 w-4" />
        </button>

        {isUserPopoverOpen && signedInUser ? (
          <div className="absolute right-0 top-[calc(100%+0.85rem)] z-50 w-[25rem] border border-slate-200 bg-white p-5 text-slate-950 shadow-2xl shadow-slate-300/70">
            <div className="flex items-start gap-4">
              <AvatarUploader
                account={signedInUser}
                isBusy={false}
                translate={translate}
                onUploadAvatar={async avatarFile => {
                  await onUploadUserAvatar?.(avatarFile)
                }}
                onUseDefaultAvatar={async avatarPath => {
                  await onUseDefaultUserAvatar?.(avatarPath)
                }}
                onValidationError={showValidationError}
              />
              <div className="min-w-0 flex-1">
                <p className="truncate text-2xl font-black text-slate-950">{signedInUser.nickname}</p>
                <p className="mt-1 truncate text-sm font-semibold text-slate-500">{signedInUser.email}</p>
              </div>
            </div>

            <div className="mt-5 grid gap-2 border-t border-slate-100 pt-4">
              <button
                type="button"
                className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50"
                onClick={() => setIsProfileEditing(open => !open)}
              >
                <span className="inline-flex items-center gap-3"><Settings className="h-5 w-5" />个人信息设置</span>
                <span>›</span>
              </button>

              {isProfileEditing ? (
                <form
                  className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
                  onSubmit={async event => {
                    event.preventDefault()
                    if (!nicknameDraft.trim()) {
                      showValidationError('昵称不能为空。')
                      return
                    }
                    await onUpdateUserProfile?.({ nickname: nicknameDraft.trim(), phone: signedInUser.phone })
                    setIsProfileEditing(false)
                  }}
                >
                  <label className="grid gap-2 text-sm font-bold text-slate-600">
                    昵称
                    <input
                      className="min-h-11 border border-slate-300 bg-white px-3 text-slate-950 outline-none focus:border-black"
                      value={nicknameDraft}
                      onChange={event => setNicknameDraft(event.target.value)}
                    />
                  </label>
                  <button className="min-h-11 border border-pink-500 bg-pink-500 px-4 font-bold text-white" type="submit">
                    保存资料
                  </button>
                </form>
              ) : null}

              <button
                type="button"
                className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50"
                onClick={() => {
                  onSelectView('flightOrders')
                  setIsUserPopoverOpen(false)
                }}
              >
                <span className="inline-flex items-center gap-3"><Ticket className="h-5 w-5" />我的订单</span>
                <span>›</span>
              </button>
              <button
                type="button"
                className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50"
                onClick={() => {
                  onSelectView('travelers')
                  setIsUserPopoverOpen(false)
                }}
              >
                <span className="inline-flex items-center gap-3"><UserRound className="h-5 w-5" />出行人管理</span>
                <span>›</span>
              </button>
              <button
                type="button"
                className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50"
                onClick={() => setIsPasswordEditing(open => !open)}
              >
                <span className="inline-flex items-center gap-3"><KeyRound className="h-5 w-5" />修改密码</span>
                <span>›</span>
              </button>

              {isPasswordEditing ? (
                <form
                  className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
                  onSubmit={async event => {
                    event.preventDefault()
                    const formData = new FormData(event.currentTarget)
                    const currentPassword = String(formData.get('currentPassword') ?? '')
                    const newPassword = String(formData.get('newPassword') ?? '')
                    const confirmPassword = String(formData.get('confirmPassword') ?? '')
                    if (newPassword !== confirmPassword) {
                      showValidationError(translate('error.passwordMismatch'))
                      return
                    }
                    await onChangeUserPassword?.({ currentPassword, newPassword })
                    event.currentTarget.reset()
                    setIsPasswordEditing(false)
                  }}
                >
                  <label className="grid gap-2 text-sm font-bold text-slate-600">
                    当前密码
                    <input className="min-h-11 border border-slate-300 bg-white px-3 outline-none focus:border-black" name="currentPassword" type="password" autoComplete="current-password" required />
                  </label>
                  <label className="grid gap-2 text-sm font-bold text-slate-600">
                    新密码
                    <input className="min-h-11 border border-slate-300 bg-white px-3 outline-none focus:border-black" name="newPassword" type="password" autoComplete="new-password" required />
                  </label>
                  <label className="grid gap-2 text-sm font-bold text-slate-600">
                    确认新密码
                    <input className="min-h-11 border border-slate-300 bg-white px-3 outline-none focus:border-black" name="confirmPassword" type="password" autoComplete="new-password" required />
                  </label>
                  <button className="min-h-11 border border-pink-500 bg-pink-500 px-4 font-bold text-white" type="submit">
                    确认修改
                  </button>
                </form>
              ) : null}

              <button
                type="button"
                className="mt-2 flex items-center gap-3 border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50"
                onClick={() => {
                  setIsUserPopoverOpen(false)
                  onLogoutUser?.()
                }}
              >
                <LogOut className="h-5 w-5" />
                退出登录
              </button>
            </div>
          </div>
        ) : null}
      </div>
    </header>
  )
}

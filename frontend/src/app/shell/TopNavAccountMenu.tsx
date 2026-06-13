import { useEffect, useRef, useState } from 'react'
import { Camera, ChevronDown, IdCard, KeyRound, LogOut, Settings, Ticket, UserRound, X } from 'lucide-react'

import type { ManagerProfileOverride, TopNavAccountAvatarUploaderProps, TopNavAccountMenuProps } from '@/app/shell/TopNavBar.types'
import {
  allowedAvatarMimeTypes,
  getInitials,
  managerBusinessLabel,
  managerNameSuffix,
  maximumAvatarBytes,
  readManagerProfileOverride,
  stripManagerSuffix,
  writeManagerProfileOverride,
} from '@/app/shell/TopNavBar.utils'
import { AvatarUploader } from '@/pages/AccountPage/AvatarUploader'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

function ManagerAvatarUploader({
  avatarUrl,
  displayName,
  isBusy,
  onUploadAvatar,
  onValidationError,
  translate,
}: TopNavAccountAvatarUploaderProps) {
  const hiddenFileInputRef = useRef<HTMLInputElement | null>(null)
  const [selectedAvatarFile, setSelectedAvatarFile] = useState<File | null>(null)
  const [isAvatarDialogOpen, setIsAvatarDialogOpen] = useState(false)

  function resetSelectedFile() {
    setSelectedAvatarFile(null)
    if (hiddenFileInputRef.current) hiddenFileInputRef.current.value = ''
  }

  function closeAvatarDialog() {
    resetSelectedFile()
    setIsAvatarDialogOpen(false)
  }

  function handleSelectedFile(nextAvatarFile: File | null) {
    if (!nextAvatarFile) {
      setSelectedAvatarFile(null)
      return
    }
    if (!allowedAvatarMimeTypes.has(nextAvatarFile.type)) {
      onValidationError(translate('error.avatarType'))
      return
    }
    if (nextAvatarFile.size > maximumAvatarBytes) {
      onValidationError(translate('error.avatarTooLarge'))
      return
    }
    setSelectedAvatarFile(nextAvatarFile)
  }

  async function uploadSelectedAvatar() {
    if (!selectedAvatarFile) {
      onValidationError(translate('error.avatarMissing'))
      return
    }
    await onUploadAvatar(selectedAvatarFile)
    closeAvatarDialog()
  }

  const avatarPreview = avatarUrl ? (
    <BackendAssetImage
      className="h-full w-full bg-white object-contain"
      assetUrl={avatarUrl}
      alt="管理者头像"
      fallbackContent={getInitials(displayName)}
    />
  ) : (
    <span className="grid h-full w-full place-items-center bg-indigo-100 text-3xl font-bold text-slate-950">{getInitials(displayName)}</span>
  )

  return (
    <div className="grid gap-4">
      <button
        type="button"
        className="group relative h-24 w-24 overflow-hidden border-2 border-slate-200 bg-white p-0 text-left transition focus-visible:outline focus-visible:outline-2 focus-visible:outline-offset-2 focus-visible:outline-pink-500 disabled:cursor-not-allowed disabled:opacity-60"
        disabled={isBusy}
        onClick={() => setIsAvatarDialogOpen(true)}
        aria-label="修改头像"
        title="修改头像"
      >
        {avatarPreview}
        <span className="absolute inset-0 grid place-items-center bg-black/0 transition group-hover:bg-black/35 group-focus-visible:bg-black/35">
          <span className="grid h-9 w-11 place-items-center border-2 border-white bg-black/25 opacity-0 transition group-hover:opacity-100 group-focus-visible:opacity-100">
            <Camera className="h-5 w-5 text-white" strokeWidth={2.4} />
          </span>
        </span>
      </button>

      {isAvatarDialogOpen ? (
        <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 px-4">
          <div className="w-full max-w-lg border border-slate-200 bg-white p-6 shadow-2xl">
            <div className="mb-5 flex justify-end">
              <button
                type="button"
                className="grid h-11 w-11 place-items-center border border-slate-300 bg-white text-slate-950 transition hover:border-black hover:bg-black hover:text-white"
                onClick={closeAvatarDialog}
                aria-label="关闭头像修改窗口"
              >
                <X className="h-5 w-5" />
              </button>
            </div>

            <section className="grid gap-3">
              <h3 className="text-lg font-bold text-slate-950">上传管理者头像</h3>
              <input
                ref={hiddenFileInputRef}
                type="file"
                accept=".png,.jpg,.jpeg,image/png,image/jpeg"
                className="hidden"
                onChange={event => handleSelectedFile(event.target.files?.[0] ?? null)}
              />
              <div className="flex flex-wrap items-center gap-3">
                <button
                  type="button"
                  className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                  disabled={isBusy}
                  onClick={() => hiddenFileInputRef.current?.click()}
                >
                  选择图片
                </button>
                <button
                  type="button"
                  className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55"
                  disabled={isBusy || !selectedAvatarFile}
                  onClick={() => void uploadSelectedAvatar()}
                >
                  上传头像
                </button>
              </div>
              <span className="text-sm font-semibold text-slate-600">{selectedAvatarFile?.name ?? translate('account.avatarEmpty')}</span>
            </section>
          </div>
        </div>
      ) : null}
    </div>
  )
}

function PasswordForm({
  translate,
  onSubmitPassword,
  onValidationError,
  onDone,
}: {
  translate: (translationKey: string) => string
  onSubmitPassword: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onValidationError: (message: string) => void
  onDone: () => void
}) {
  return (
    <form
      className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
      onSubmit={async event => {
        event.preventDefault()
        const formData = new FormData(event.currentTarget)
        const currentPassword = String(formData.get('currentPassword') ?? '')
        const newPassword = String(formData.get('newPassword') ?? '')
        const confirmPassword = String(formData.get('confirmPassword') ?? '')
        if (newPassword !== confirmPassword) {
          onValidationError(translate('error.passwordMismatch'))
          return
        }
        await onSubmitPassword({ currentPassword, newPassword })
        event.currentTarget.reset()
        onDone()
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
  )
}

export function TopNavAccountMenu({
  signedInManager,
  signedInUser,
  onSelectView,
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
}: TopNavAccountMenuProps) {
  const popoverRef = useRef<HTMLDivElement | null>(null)
  const [isAccountPopoverOpen, setIsAccountPopoverOpen] = useState(false)
  const [isProfileEditing, setIsProfileEditing] = useState(false)
  const [isPasswordEditing, setIsPasswordEditing] = useState(false)
  const [nicknameDraft, setNicknameDraft] = useState(signedInUser?.nickname ?? '')
  const [managerNameDraft, setManagerNameDraft] = useState('')
  const [managerProfileOverride, setManagerProfileOverride] = useState<ManagerProfileOverride>({})

  const effectiveManager = signedInManager
    ? {
        ...signedInManager,
        displayName: managerProfileOverride.displayName ?? signedInManager.displayName,
        logoAssetPath: managerProfileOverride.logoAssetPath ?? signedInManager.logoAssetPath,
      }
    : null
  const identityLabel = signedInUser?.nickname ?? effectiveManager?.displayName ?? translate('guest.badge')
  const avatarUrl = signedInUser?.avatarUrl ?? effectiveManager?.logoAssetPath ?? null

  useEffect(() => {
    setNicknameDraft(signedInUser?.nickname ?? '')
  }, [signedInUser?.nickname])

  useEffect(() => {
    if (!signedInManager) {
      setManagerProfileOverride({})
      setManagerNameDraft('')
      return
    }
    const override = readManagerProfileOverride(signedInManager.managerId)
    const displayName = override.displayName ?? signedInManager.displayName
    setManagerProfileOverride(override)
    setManagerNameDraft(stripManagerSuffix(displayName, signedInManager.managerType))
  }, [signedInManager?.managerId, signedInManager?.displayName, signedInManager?.managerType])

  useEffect(() => {
    if (!isAccountPopoverOpen) return
    function closeOnOutsideClick(event: MouseEvent) {
      if (popoverRef.current && !popoverRef.current.contains(event.target as Node)) setIsAccountPopoverOpen(false)
    }
    document.addEventListener('mousedown', closeOnOutsideClick)
    return () => document.removeEventListener('mousedown', closeOnOutsideClick)
  }, [isAccountPopoverOpen])

  function showValidationError(message: string) {
    onValidationError?.(message)
  }

  function renderAvatar(sizeClassName: string) {
    if (avatarUrl) {
      return (
        <BackendAssetImage
          className={`${sizeClassName} inline-flex items-center justify-center border border-slate-200 bg-white object-cover`}
          assetUrl={avatarUrl}
          alt={identityLabel}
          fallbackContent={getInitials(identityLabel)}
        />
      )
    }
    return <span className={`${sizeClassName} inline-flex items-center justify-center border border-slate-200 bg-indigo-100 text-slate-700`}>{getInitials(identityLabel)}</span>
  }

  async function updateManagerName() {
    if (!effectiveManager) return
    const baseName = managerNameDraft.trim()
    if (!baseName) {
      showValidationError(`${managerBusinessLabel(effectiveManager.managerType)}名称不能为空。`)
      return
    }
    const nextDisplayName = `${baseName}${managerNameSuffix(effectiveManager.managerType)}`
    const nextOverride = { ...managerProfileOverride, displayName: nextDisplayName }
    writeManagerProfileOverride(effectiveManager.managerId, nextOverride)
    setManagerProfileOverride(nextOverride)
    await onUpdateManagerProfile?.({ displayName: nextDisplayName, logoAssetPath: nextOverride.logoAssetPath })
    setIsProfileEditing(false)
  }

  async function updateManagerAvatar(avatarFile: File) {
    if (!effectiveManager) return
    const nextAvatarUrl = await new Promise<string>((resolve, reject) => {
      const reader = new FileReader()
      reader.onload = () => resolve(String(reader.result ?? ''))
      reader.onerror = () => reject(new Error('avatar_read_failed'))
      reader.readAsDataURL(avatarFile)
    })
    const nextOverride = { ...managerProfileOverride, logoAssetPath: nextAvatarUrl }
    writeManagerProfileOverride(effectiveManager.managerId, nextOverride)
    setManagerProfileOverride(nextOverride)
    await onUpdateManagerProfile?.({ displayName: effectiveManager.displayName, logoAssetPath: nextAvatarUrl })
  }

  return (
    <div ref={popoverRef} className="relative flex items-center">
      <button
        type="button"
        className={
          isOverlay
            ? 'inline-flex items-center gap-3 rounded-none border border-white/25 bg-white/10 px-3 py-2 text-white transition hover:bg-white/20'
            : 'inline-flex items-center gap-3 border border-slate-200 bg-white px-3 py-2 text-slate-500 transition hover:border-black hover:text-slate-950'
        }
        onClick={() => {
          if (!signedInUser && !signedInManager) {
            onSelectView('account')
            return
          }
          setIsAccountPopoverOpen(open => !open)
        }}
      >
        {renderAvatar('h-10 w-10')}
        <span>{identityLabel}</span>
        <ChevronDown className="h-4 w-4" />
      </button>

      {isAccountPopoverOpen && signedInUser ? (
        <div className="absolute right-0 top-[calc(100%+0.85rem)] z-50 w-[25rem] border border-slate-200 bg-white p-5 text-slate-950 shadow-2xl shadow-slate-300/70">
          <div className="flex items-start gap-4">
            <AvatarUploader
              account={signedInUser}
              isBusy={false}
              translate={translate}
              onUploadAvatar={async avatarFile => onUploadUserAvatar?.(avatarFile)}
              onUseDefaultAvatar={async avatarPath => onUseDefaultUserAvatar?.(avatarPath)}
              onValidationError={showValidationError}
            />
            <div className="min-w-0 flex-1">
              <p className="truncate text-2xl font-black text-slate-950">{signedInUser.nickname}</p>
              <p className="mt-1 truncate text-sm font-semibold text-slate-500">{signedInUser.email}</p>
            </div>
          </div>

          <div className="mt-5 grid gap-2 border-t border-slate-100 pt-4">
            <button type="button" className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => setIsProfileEditing(open => !open)}>
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
                  <input className="min-h-11 border border-slate-300 bg-white px-3 text-slate-950 outline-none focus:border-black" value={nicknameDraft} onChange={event => setNicknameDraft(event.target.value)} />
                </label>
                <button className="min-h-11 border border-pink-500 bg-pink-500 px-4 font-bold text-white" type="submit">
                  保存资料
                </button>
              </form>
            ) : null}

            <button type="button" className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => { onSelectView('flightOrders'); setIsAccountPopoverOpen(false) }}>
              <span className="inline-flex items-center gap-3"><Ticket className="h-5 w-5" />我的订单</span>
              <span>›</span>
            </button>
            <button type="button" className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => { onSelectView('travelers'); setIsAccountPopoverOpen(false) }}>
              <span className="inline-flex items-center gap-3"><UserRound className="h-5 w-5" />出行人管理</span>
              <span>›</span>
            </button>
            <button type="button" className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => setIsPasswordEditing(open => !open)}>
              <span className="inline-flex items-center gap-3"><KeyRound className="h-5 w-5" />修改密码</span>
              <span>›</span>
            </button>

            {isPasswordEditing ? (
              <PasswordForm
                translate={translate}
                onSubmitPassword={async payload => onChangeUserPassword?.(payload)}
                onValidationError={showValidationError}
                onDone={() => setIsPasswordEditing(false)}
              />
            ) : null}

            <button type="button" className="mt-2 flex items-center gap-3 border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => { setIsAccountPopoverOpen(false); onLogoutUser?.() }}>
              <LogOut className="h-5 w-5" />
              退出登录
            </button>
          </div>
        </div>
      ) : null}

      {isAccountPopoverOpen && effectiveManager ? (
        <div className="absolute right-0 top-[calc(100%+0.85rem)] z-50 w-[25rem] border border-slate-200 bg-white p-5 text-slate-950 shadow-2xl shadow-slate-300/70">
          <div className="flex items-start gap-4">
            <ManagerAvatarUploader
              avatarUrl={effectiveManager.logoAssetPath ?? null}
              displayName={effectiveManager.displayName}
              isBusy={false}
              translate={translate}
              onUploadAvatar={updateManagerAvatar}
              onValidationError={showValidationError}
            />
            <div className="min-w-0 flex-1">
              <p className="truncate text-2xl font-black text-slate-950">{effectiveManager.displayName}</p>
              <p className="mt-1 truncate text-sm font-semibold text-slate-500">{effectiveManager.email}</p>
              <p className="mt-1 truncate text-xs font-semibold text-slate-400">{effectiveManager.managerType}</p>
            </div>
          </div>

          <div className="mt-5 grid gap-2 border-t border-slate-100 pt-4">
            <button type="button" className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => setIsProfileEditing(open => !open)}>
              <span className="inline-flex items-center gap-3"><IdCard className="h-5 w-5" />修改{managerBusinessLabel(effectiveManager.managerType)}名称</span>
              <span>›</span>
            </button>

            {isProfileEditing ? (
              <form
                className="grid gap-3 border border-slate-200 bg-slate-50 p-3"
                onSubmit={event => {
                  event.preventDefault()
                  void updateManagerName()
                }}
              >
                <label className="grid gap-2 text-sm font-bold text-slate-600">
                  {managerBusinessLabel(effectiveManager.managerType)}名称
                  <span className="grid grid-cols-[minmax(0,1fr)_auto] items-center border border-slate-300 bg-white focus-within:border-black">
                    <input
                      className="min-h-11 min-w-0 border-0 bg-transparent px-3 text-slate-950 outline-none"
                      value={managerNameDraft}
                      onChange={event => setManagerNameDraft(event.target.value)}
                    />
                    <span className="border-l border-slate-200 px-3 text-sm font-bold text-slate-500">{managerNameSuffix(effectiveManager.managerType)}</span>
                  </span>
                </label>
                <button className="min-h-11 border border-pink-500 bg-pink-500 px-4 font-bold text-white" type="submit">
                  保存名称
                </button>
              </form>
            ) : null}

            <button type="button" className="flex items-center justify-between border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => setIsPasswordEditing(open => !open)}>
              <span className="inline-flex items-center gap-3"><KeyRound className="h-5 w-5" />修改密码</span>
              <span>›</span>
            </button>

            {isPasswordEditing ? (
              <PasswordForm
                translate={translate}
                onSubmitPassword={async payload => onChangeManagerPassword?.(payload)}
                onValidationError={showValidationError}
                onDone={() => setIsPasswordEditing(false)}
              />
            ) : null}

            <button type="button" className="mt-2 flex items-center gap-3 border border-transparent px-3 py-3 text-left font-bold text-slate-700 transition hover:border-slate-200 hover:bg-slate-50" onClick={() => { setIsAccountPopoverOpen(false); onLogoutManager?.() }}>
              <LogOut className="h-5 w-5" />
              退出登录
            </button>
          </div>
        </div>
      ) : null}
    </div>
  )
}

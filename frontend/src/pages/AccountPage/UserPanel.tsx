import type { ReactNode } from 'react'
import { useEffect, useRef, useState } from 'react'

import type { UserResponse } from '@/lib/mvp-types/index'
import { getPasswordValidationMessage } from '@/pages/shared/auth/passwordValidation'
import { AvatarUploader } from './AvatarUploader'

type AccountEntryMode = 'register' | 'login'

type UserPanelProps = {
  account: UserResponse | null
  accountEntryMode: AccountEntryMode
  isBusy: boolean
  isGuestMode: boolean
  loginEmailDraft: string
  translate: (translationKey: string) => string
  onChangeAccountEntryMode: (accountEntryMode: AccountEntryMode) => void
  onChangeLoginEmailDraft: (email: string) => void
  onRegisterAccount: (payload: {
    email: string
    nickname: string
    phone: string
    password: string
  }) => Promise<void>
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

const cardClassName =
  'grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60'
const accountEntryCardClassName =
  'mx-auto grid w-full gap-4 self-start border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60 md:w-1/2 md:max-w-3xl'
const heroCardClassName =
  'grid gap-4 border border-slate-200 bg-white p-6 text-slate-950 shadow-xl shadow-slate-200/60'
const primaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center !border !border-black ![background:#000] px-4 py-2 text-sm font-semibold !text-white !shadow-none ![transform:none] transition hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
const secondaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center !border !border-slate-300 ![background:transparent] px-4 py-2 text-sm font-semibold !text-slate-950 !shadow-none ![transform:none] transition hover:!border-black hover:![background:#000] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
const authPrimaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center !border !border-pink-500 ![background:#ec4899] px-4 py-2 text-sm font-semibold !text-white !shadow-none ![transform:none] transition hover:![background:#db2777] hover:!text-white hover:![box-shadow:none] hover:![transform:none] disabled:cursor-not-allowed disabled:opacity-55'
const labelClassName = 'grid gap-2 text-sm font-medium text-slate-700'
const inputClassName =
  'min-h-11 border border-slate-300 bg-white px-3 py-2 text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black focus:ring-2 focus:ring-slate-200'

function SectionHeader({ eyebrow, title, actions }: { eyebrow: string; title: string; actions?: ReactNode }) {
  return (
    <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
      <div>
        <p className="mb-1 text-xs font-bold uppercase tracking-wider text-slate-500">{eyebrow}</p>
        <h2 className="m-0 text-2xl font-bold leading-tight text-slate-950 md:text-3xl">{title}</h2>
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
    </div>
  )
}

function DetailField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div>
      <span className="mb-1 block text-sm text-slate-500">{label}</span>
      <strong className="text-slate-950">{children}</strong>
    </div>
  )
}

export function UserPanel({
  account,
  accountEntryMode,
  isBusy,
  isGuestMode,
  loginEmailDraft,
  translate,
  onChangeAccountEntryMode,
  onChangeLoginEmailDraft,
  onRegisterAccount,
  onLoginAccount,
  onUploadAvatar,
  onUseDefaultAvatar,
  onUpdateProfile,
  onAvatarValidationError,
  onValidationError,
  onChangePassword,
  onLogoutCurrentSession,
  onLogout,
}: UserPanelProps) {
  const registerFormRef = useRef<HTMLFormElement>(null)
  const loginFormRef = useRef<HTMLFormElement>(null)
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
  const [isProfileEditing, setIsProfileEditing] = useState(false)
  const [profileDraft, setProfileDraft] = useState({ nickname: account?.nickname ?? '', phone: account?.phone ?? '' })
  const [loginPasswordDraft, setLoginPasswordDraft] = useState('')
  const [isLoginFormWritable, setIsLoginFormWritable] = useState(false)
  function clearAccountEntryForms() {
    registerFormRef.current?.reset()
    loginFormRef.current?.reset()
    onChangeLoginEmailDraft('')
    setLoginPasswordDraft('')
    setIsLoginFormWritable(false)
  }

  function switchAccountEntryMode(nextAccountEntryMode: AccountEntryMode) {
    clearAccountEntryForms()
    onChangeAccountEntryMode(nextAccountEntryMode)
  }

  useEffect(() => {
    if (!isGuestMode) {
      return
    }

    clearAccountEntryForms()
    const resetTimers = [50, 250, 800].map(delay => window.setTimeout(clearAccountEntryForms, delay))
    return () => resetTimers.forEach(timerId => window.clearTimeout(timerId))
  }, [accountEntryMode, isGuestMode])

  useEffect(() => {
    setProfileDraft({ nickname: account?.nickname ?? '', phone: account?.phone ?? '' })
  }, [account?.nickname, account?.phone])

  if (isGuestMode) {
    return (
      <section className="grid gap-4">
        <section className={accountEntryCardClassName}>
          <SectionHeader
            eyebrow={translate('account.entryEyebrow')}
            title={translate(accountEntryMode === 'register' ? 'account.registerTitle' : 'account.loginTitle')}
          />

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              className={accountEntryMode === 'register' ? primaryButtonClassName : secondaryButtonClassName}
              disabled={isBusy}
              onClick={() => switchAccountEntryMode('register')}
            >
              {translate('account.create')}
            </button>
            <button
              type="button"
              className={accountEntryMode === 'login' ? primaryButtonClassName : secondaryButtonClassName}
              disabled={isBusy}
              onClick={() => switchAccountEntryMode('login')}
            >
              {translate('account.login')}
            </button>
          </div>

          {accountEntryMode === 'register' ? (
            <form
              key="register-account-form"
              ref={registerFormRef}
              className="grid gap-4"
              autoComplete="off"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                const password = String(formData.get('registerPassword') ?? '')
                const confirmPassword = String(formData.get('registerConfirmPassword') ?? '')
                if (password !== confirmPassword) {
                  onValidationError(translate('error.passwordMismatch'))
                  return
                }
                const email = String(formData.get('registerEmail') ?? '').trim()
                const passwordValidationMessage = getPasswordValidationMessage(password, email)
                if (passwordValidationMessage) {
                  onValidationError(passwordValidationMessage)
                  return
                }
                await onRegisterAccount({
                  email,
                  nickname: String(formData.get('registerNickname') ?? ''),
                  phone: String(formData.get('registerPhone') ?? ''),
                  password,
                })
                event.currentTarget.reset()
              }}
            >
              <label className={labelClassName}>
                {translate('account.nickname')}
                <input className={inputClassName} name="registerNickname" autoComplete="off" required />
              </label>
              <label className={labelClassName}>
                {translate('account.email')}
                <input className={inputClassName} name="registerEmail" type="email" autoComplete="off" required />
              </label>
              <label className={labelClassName}>
                {translate('account.phone')}
                <input className={inputClassName} name="registerPhone" autoComplete="off" required />
              </label>
              <label className={labelClassName}>
                {translate('account.password')}
                <input className={inputClassName} name="registerPassword" type="password" autoComplete="new-password" required />
              </label>
              <label className={labelClassName}>
                {translate('account.confirmPassword')}
                <input className={inputClassName} name="registerConfirmPassword" type="password" autoComplete="new-password" required />
              </label>
              <button className={authPrimaryButtonClassName} type="submit" disabled={isBusy}>
                {translate('account.create')}
              </button>
            </form>
          ) : (
            <form
              key="login-account-form"
              ref={loginFormRef}
              className="grid gap-4"
              autoComplete="new-password"
              onSubmit={async event => {
                event.preventDefault()
                await onLoginAccount({
                  email: loginEmailDraft,
                  password: loginPasswordDraft,
                })
                setLoginPasswordDraft('')
                event.currentTarget.reset()
              }}
            >
              <input className="hidden" tabIndex={-1} aria-hidden="true" autoComplete="username" />
              <input className="hidden" tabIndex={-1} aria-hidden="true" type="password" autoComplete="current-password" />
              <label className={labelClassName}>
                {translate('account.email')}
                <input
                  className={inputClassName}
                  name="fpAccountContact"
                  type="text"
                  inputMode="email"
                  value={loginEmailDraft}
                  autoComplete="new-password"
                  readOnly={!isLoginFormWritable}
                  onFocus={() => setIsLoginFormWritable(true)}
                  onMouseDown={() => setIsLoginFormWritable(true)}
                  onChange={event => onChangeLoginEmailDraft(event.target.value)}
                  required
                />
              </label>
              <label className={labelClassName}>
                {translate('account.password')}
                <input
                  className={inputClassName}
                  name="fpAccountSecret"
                  type="password"
                  value={loginPasswordDraft}
                  autoComplete="new-password"
                  readOnly={!isLoginFormWritable}
                  onFocus={() => setIsLoginFormWritable(true)}
                  onMouseDown={() => setIsLoginFormWritable(true)}
                  onChange={event => setLoginPasswordDraft(event.target.value)}
                  required
                />
              </label>
              <button className={authPrimaryButtonClassName} type="submit" disabled={isBusy}>
                {translate('account.login')}
              </button>
            </form>
          )}
        </section>
      </section>
    )
  }

  return (
    <section className="mx-auto grid w-full gap-4 lg:w-3/4">
      <section className={heroCardClassName}>
        <div className="grid gap-6 lg:grid-cols-[7rem_minmax(0,1fr)]">
          <AvatarUploader
            account={account!}
            isBusy={isBusy}
            translate={translate}
            onUploadAvatar={onUploadAvatar}
            onUseDefaultAvatar={onUseDefaultAvatar}
            onValidationError={onAvatarValidationError}
          />

          <div className="grid gap-4">
            <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
              <div className="grid gap-4 md:grid-cols-2">
                <DetailField label={translate('account.nickname')}>{account!.nickname}</DetailField>
                <DetailField label={translate('account.email')}>{account!.email}</DetailField>
              </div>
              <div className="flex flex-wrap items-center gap-3">
                <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={() => setIsProfileEditing(editing => !editing)}>
                  {translate('account.editProfile')}
                </button>
                <button className={primaryButtonClassName} type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
                  {translate('account.logoutCurrentSession')}
                </button>
              </div>
            </div>

            {isProfileEditing ? (
              <form
                className="grid gap-4 border border-slate-200 bg-slate-50 p-4"
                onSubmit={async event => {
                  event.preventDefault()
                  if (!profileDraft.nickname.trim()) {
                    onValidationError('昵称不能为空。')
                    return
                  }
                  await onUpdateProfile({
                    nickname: profileDraft.nickname.trim(),
                    phone: account!.phone,
                  })
                  setIsProfileEditing(false)
                }}
              >
                <label className={labelClassName}>
                  {translate('account.nickname')}
                  <input
                    className={inputClassName}
                    value={profileDraft.nickname}
                    onChange={event => setProfileDraft(current => ({ ...current, nickname: event.target.value }))}
                  />
                </label>
                <div className="flex flex-wrap gap-3">
                  <button className={primaryButtonClassName} type="submit" disabled={isBusy}>
                    {translate('account.saveProfile')}
                  </button>
                  <button
                    className={secondaryButtonClassName}
                    type="button"
                    disabled={isBusy}
                    onClick={() => {
                      setProfileDraft({ nickname: account?.nickname ?? '', phone: account?.phone ?? '' })
                      setIsProfileEditing(false)
                    }}
                  >
                    {translate('account.cancelEdit')}
                  </button>
                </div>
              </form>
            ) : null}
          </div>
        </div>
      </section>

      <section className={cardClassName}>
        <SectionHeader
          eyebrow={translate('account.security')}
          title={translate('account.accountSecurity')}
          actions={
            <>
              <button
                className={secondaryButtonClassName}
                type="button"
                disabled={isBusy}
                onClick={() => setIsChangePasswordOpen(open => !open)}
              >
                {translate(isChangePasswordOpen ? 'account.hideChangePassword' : 'account.showChangePassword')}
              </button>
              <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={onLogout}>
                {translate('account.logout')}
              </button>
            </>
          }
        />

        {isChangePasswordOpen ? (
          <form
            className="grid gap-4"
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
              await onChangePassword({ currentPassword, newPassword })
              event.currentTarget.reset()
              setIsChangePasswordOpen(false)
            }}
          >
            <label className={labelClassName}>
              {translate('account.currentPassword')}
              <input className={inputClassName} name="currentPassword" type="password" required />
            </label>
            <label className={labelClassName}>
              {translate('account.newPassword')}
              <input className={inputClassName} name="newPassword" type="password" required />
            </label>
            <label className={labelClassName}>
              {translate('account.confirmPassword')}
              <input className={inputClassName} name="confirmPassword" type="password" required />
            </label>
            <button className={primaryButtonClassName} type="submit" disabled={isBusy}>
              {translate('account.changePassword')}
            </button>
          </form>
        ) : null}
      </section>
    </section>
  )
}


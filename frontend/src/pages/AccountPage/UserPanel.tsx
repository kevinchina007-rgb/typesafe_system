import type { ReactNode } from 'react'
﻿import { useState } from 'react'

import type { AppLanguage, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { formatTravelerReference, localizeAccountStatus, localizeMembershipLevel } from '@/lib/presenters/view-models'
import { AvatarUploader } from './AvatarUploader'

type AccountEntryMode = 'register' | 'login'

type UserPanelProps = {
  account: UserResponse | null
  currentLanguage: AppLanguage
  accountEntryMode: AccountEntryMode
  isBusy: boolean
  isGuestMode: boolean
  loginEmailDraft: string
  travelers: TravelerResponse[]
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
  onAvatarValidationError: (message: string) => void
  onValidationError: (message: string) => void
  onRefreshAccount: () => Promise<void>
  onChangePassword: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onLogoutCurrentSession: () => void
  onLogout: () => void
}

const cardClassName =
  'grid gap-4 rounded-2xl border border-slate-700/50 bg-slate-950/75 p-6 shadow-xl shadow-slate-950/25 backdrop-blur'
const heroCardClassName =
  'grid gap-4 rounded-2xl border border-slate-700/50 bg-[radial-gradient(circle_at_top_right,rgba(14,165,233,0.16),transparent_28%),radial-gradient(circle_at_bottom_left,rgba(99,102,241,0.18),transparent_30%),linear-gradient(135deg,rgba(15,23,42,0.97),rgba(30,41,59,0.9))] p-6 shadow-2xl shadow-cyan-950/25'
const primaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center rounded-xl border border-cyan-300/40 bg-cyan-300 px-4 py-2 text-sm font-semibold text-slate-950 shadow-lg shadow-cyan-950/20 transition hover:bg-cyan-200 disabled:cursor-not-allowed disabled:opacity-55'
const secondaryButtonClassName =
  'inline-flex min-h-11 items-center justify-center rounded-xl border border-slate-600/70 bg-white/5 px-4 py-2 text-sm font-semibold text-slate-100 transition hover:border-cyan-300/50 hover:bg-cyan-300/10 disabled:cursor-not-allowed disabled:opacity-55'
const labelClassName = 'grid gap-2 text-sm font-medium text-slate-300'
const inputClassName =
  'min-h-11 rounded-xl border border-slate-600/70 bg-slate-900/80 px-3 py-2 text-slate-100 outline-none transition placeholder:text-slate-500 focus:border-cyan-300 focus:ring-2 focus:ring-cyan-300/20'

function scrollToSection(sectionId: string) {
  document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

function SectionHeader({ eyebrow, title, actions }: { eyebrow: string; title: string; actions?: ReactNode }) {
  return (
    <div className="flex flex-col justify-between gap-4 md:flex-row md:items-start">
      <div>
        <p className="mb-1 text-xs font-bold uppercase tracking-wider text-cyan-300">{eyebrow}</p>
        <h2 className="m-0 text-2xl font-bold leading-tight text-slate-50 md:text-3xl">{title}</h2>
      </div>
      {actions ? <div className="flex flex-wrap items-center gap-3">{actions}</div> : null}
    </div>
  )
}

function StatBlock({ label, value }: { label: string; value: ReactNode }) {
  return (
    <div className="grid gap-1 rounded-2xl border border-white/10 bg-white/5 p-4 shadow-inner shadow-white/5">
      <span className="text-sm text-slate-400">{label}</span>
      <strong className="text-xl font-bold text-slate-50">{value}</strong>
    </div>
  )
}

function DetailField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <div>
      <span className="mb-1 block text-sm text-slate-400">{label}</span>
      <strong className="text-slate-50">{children}</strong>
    </div>
  )
}

export function UserPanel({
  account,
  currentLanguage,
  accountEntryMode,
  isBusy,
  isGuestMode,
  loginEmailDraft,
  travelers,
  translate,
  onChangeAccountEntryMode,
  onChangeLoginEmailDraft,
  onRegisterAccount,
  onLoginAccount,
  onUploadAvatar,
  onAvatarValidationError,
  onValidationError,
  onRefreshAccount,
  onChangePassword,
  onLogoutCurrentSession,
  onLogout,
}: UserPanelProps) {
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
  const placeholderTexts = {
    nickname: currentLanguage === 'zh' ? '鏋楁櫒' : 'Lin Chen',
    email: 'lin.chen@example.com',
    phone: '+8613812345678',
    password: currentLanguage === 'zh' ? '?? 8 ???' : 'At least 8 characters',
    confirmPassword: currentLanguage === 'zh' ? '鍐嶆杈撳叆瀵嗙爜' : 'Repeat password',
    currentPassword: currentLanguage === 'zh' ? '褰撳墠瀵嗙爜' : 'Current password',
    newPassword: currentLanguage === 'zh' ? '???' : 'New password',
  }
  const defaultTraveler =
    account?.defaultTravelerProfileId
      ? travelers.find(traveler => traveler.travelerId === account.defaultTravelerProfileId) ?? null
      : null

  if (isGuestMode) {
    return (
      <section className="grid gap-4">
        <section className={cardClassName}>
          <SectionHeader
            eyebrow={translate('account.entryEyebrow')}
            title={translate(accountEntryMode === 'register' ? 'account.registerTitle' : 'account.loginTitle')}
          />

          <div className="flex flex-wrap items-center gap-3">
            <button
              type="button"
              className={accountEntryMode === 'register' ? primaryButtonClassName : secondaryButtonClassName}
              disabled={isBusy}
              onClick={() => onChangeAccountEntryMode('register')}
            >
              {translate('account.create')}
            </button>
            <button
              type="button"
              className={accountEntryMode === 'login' ? primaryButtonClassName : secondaryButtonClassName}
              disabled={isBusy}
              onClick={() => onChangeAccountEntryMode('login')}
            >
              {translate('account.login')}
            </button>
          </div>

          {accountEntryMode === 'register' ? (
            <form
              className="grid gap-4"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                const password = String(formData.get('password') ?? '')
                const confirmPassword = String(formData.get('confirmPassword') ?? '')
                if (password !== confirmPassword) {
                  onValidationError(translate('error.passwordMismatch'))
                  return
                }
                await onRegisterAccount({
                  email: String(formData.get('email') ?? ''),
                  nickname: String(formData.get('nickname') ?? ''),
                  phone: String(formData.get('phone') ?? ''),
                  password,
                })
                event.currentTarget.reset()
              }}
            >
              <label className={labelClassName}>
                {translate('account.nickname')}
                <input className={inputClassName} name="nickname" placeholder={placeholderTexts.nickname} required />
              </label>
              <label className={labelClassName}>
                {translate('account.email')}
                <input className={inputClassName} name="email" type="email" placeholder={placeholderTexts.email} required />
              </label>
              <label className={labelClassName}>
                {translate('account.phone')}
                <input className={inputClassName} name="phone" placeholder={placeholderTexts.phone} required />
              </label>
              <label className={labelClassName}>
                {translate('account.password')}
                <input className={inputClassName} name="password" type="password" placeholder={placeholderTexts.password} required />
              </label>
              <label className={labelClassName}>
                {translate('account.confirmPassword')}
                <input className={inputClassName} name="confirmPassword" type="password" placeholder={placeholderTexts.confirmPassword} required />
              </label>
              <button className={primaryButtonClassName} type="submit" disabled={isBusy}>
                {translate('account.create')}
              </button>
            </form>
          ) : (
            <form
              className="grid gap-4"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onLoginAccount({
                  email: loginEmailDraft,
                  password: String(formData.get('password') ?? ''),
                })
              }}
            >
              <label className={labelClassName}>
                {translate('account.email')}
                <input
                  className={inputClassName}
                  name="loginEmail"
                  type="email"
                  value={loginEmailDraft}
                  placeholder={placeholderTexts.email}
                  onChange={event => onChangeLoginEmailDraft(event.target.value)}
                  required
                />
              </label>
              <label className={labelClassName}>
                {translate('account.password')}
                <input className={inputClassName} name="password" type="password" placeholder={placeholderTexts.currentPassword} required />
              </label>
              <button className={primaryButtonClassName} type="submit" disabled={isBusy}>
                {translate('account.login')}
              </button>
            </form>
          )}
        </section>
      </section>
    )
  }

  return (
    <section className="grid gap-4">
      <section className={heroCardClassName}>
        <div className="grid gap-5 lg:grid-cols-[minmax(18rem,22rem)_minmax(0,1fr)]">
          <AvatarUploader
            account={account!}
            isBusy={isBusy}
            translate={translate}
            onUploadAvatar={onUploadAvatar}
            onValidationError={onAvatarValidationError}
          />

          <div className="grid gap-4">
            <SectionHeader
              eyebrow={translate('account.heroEyebrow')}
              title={account?.nickname ?? translate('account.profileTitle')}
              actions={
                <>
                  <button className={primaryButtonClassName} type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
                    {translate('account.logoutCurrentSession')}
                  </button>
                  <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={() => scrollToSection('account-profile-section')}>
                    {translate('account.editProfile')}
                  </button>
                </>
              }
            />

            <div className="grid gap-3 sm:grid-cols-2 xl:grid-cols-4">
              <StatBlock label={translate('account.status')} value={localizeAccountStatus(account!.status, currentLanguage)} />
              <StatBlock label={translate('account.membership')} value={localizeMembershipLevel(account!.membershipLevel, currentLanguage)} />
              <StatBlock label={translate('account.points')} value={account!.points} />
              <StatBlock label={translate('account.primaryTraveler')} value={formatTravelerReference(defaultTraveler)} />
            </div>
          </div>
        </div>
      </section>

      <section id="account-actions-section" className={cardClassName}>
        <SectionHeader eyebrow={translate('account.operationsEyebrow')} title={translate('account.operationsTitle')} />

        <div className="flex flex-wrap items-center gap-3">
          <button className={primaryButtonClassName} type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
            {translate('account.logoutCurrentSession')}
          </button>
          <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={() => void onRefreshAccount()}>
            {translate('account.refresh')}
          </button>
          <button className={secondaryButtonClassName} type="button" disabled={isBusy} onClick={onLogout}>
            {translate('account.logout')}
          </button>
        </div>
      </section>

      <section id="account-profile-section" className={cardClassName}>
        <SectionHeader eyebrow={translate('account.profileEyebrow')} title={translate('account.profileTitle')} />

        <div className="grid gap-4 md:grid-cols-2 xl:grid-cols-3">
          <DetailField label={translate('account.nickname')}>{account!.nickname}</DetailField>
          <DetailField label={translate('account.email')}>{account!.email}</DetailField>
          <DetailField label={translate('account.phone')}>{account!.phone}</DetailField>
          <DetailField label={translate('account.status')}>{localizeAccountStatus(account!.status, currentLanguage)}</DetailField>
          <DetailField label={translate('account.membership')}>{localizeMembershipLevel(account!.membershipLevel, currentLanguage)}</DetailField>
          <DetailField label={translate('account.points')}>{account!.points}</DetailField>
          <DetailField label={translate('account.primaryTraveler')}>{formatTravelerReference(defaultTraveler)}</DetailField>
          <DetailField label={translate('account.createdAt')}>{new Date(account!.createdAt).toLocaleString()}</DetailField>
        </div>
      </section>

      <section className={cardClassName}>
        <SectionHeader
          eyebrow={translate('account.security')}
          title={translate('account.changePassword')}
          actions={
            <button
              className={secondaryButtonClassName}
              type="button"
              disabled={isBusy}
              onClick={() => setIsChangePasswordOpen(open => !open)}
            >
              {translate(isChangePasswordOpen ? 'account.hideChangePassword' : 'account.showChangePassword')}
            </button>
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
              <input className={inputClassName} name="currentPassword" type="password" placeholder={placeholderTexts.currentPassword} required />
            </label>
            <label className={labelClassName}>
              {translate('account.newPassword')}
              <input className={inputClassName} name="newPassword" type="password" placeholder={placeholderTexts.newPassword} required />
            </label>
            <label className={labelClassName}>
              {translate('account.confirmPassword')}
              <input className={inputClassName} name="confirmPassword" type="password" placeholder={placeholderTexts.confirmPassword} required />
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

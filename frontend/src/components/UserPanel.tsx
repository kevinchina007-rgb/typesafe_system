import { useState } from 'react'

import { ActionBar, AppCard, PrimaryButton, SecondaryButton, SectionHeader, StatCard } from './ui/UIComponents'
import { AvatarUploader } from './AvatarUploader'
import type { AppLanguage, AuthSessionResponse, TravelerResponse, UserResponse } from '../lib/mvp-types'
import { formatTravelerReference, localizeAccountStatus, localizeMembershipLevel } from '../lib/view-models'

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
  sessions: AuthSessionResponse[]
  onRefreshSessions: () => Promise<void>
  onChangePassword: (payload: { currentPassword: string; newPassword: string }) => Promise<void>
  onLogoutCurrentSession: () => void
  onLogoutOtherSessions: () => Promise<void>
  onLogout: () => void
}

function scrollToSection(sectionId: string) {
  document.getElementById(sectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
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
  sessions,
  onRefreshSessions,
  onChangePassword,
  onLogoutCurrentSession,
  onLogoutOtherSessions,
  onLogout,
}: UserPanelProps) {
  const currentSession = sessions.find(session => session.isCurrent)
  const [isChangePasswordOpen, setIsChangePasswordOpen] = useState(false)
  const [isSessionsOpen, setIsSessionsOpen] = useState(false)
  const defaultTraveler =
    account?.defaultTravelerProfileId
      ? travelers.find(traveler => traveler.travelerId === account.defaultTravelerProfileId) ?? null
      : null

  if (isGuestMode) {
    return (
      <section className="account-page-stack">
        <AppCard>
          <SectionHeader
            eyebrow={translate('account.entryEyebrow')}
            title={translate(accountEntryMode === 'register' ? 'account.registerTitle' : 'account.loginTitle')}
          />

          <div className="action-row">
            <button
              type="button"
              className={accountEntryMode === 'register' ? '' : 'secondary-button'}
              disabled={isBusy}
              onClick={() => onChangeAccountEntryMode('register')}
            >
              {translate('account.create')}
            </button>
            <button
              type="button"
              className={accountEntryMode === 'login' ? '' : 'secondary-button'}
              disabled={isBusy}
              onClick={() => onChangeAccountEntryMode('login')}
            >
              {translate('account.login')}
            </button>
          </div>

          {accountEntryMode === 'register' ? (
            <form
              className="stack-form"
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
              <label>
                {translate('account.nickname')}
                <input name="nickname" placeholder="Lin Chen" required />
              </label>
              <label>
                {translate('account.email')}
                <input name="email" type="email" placeholder="lin.chen@example.com" required />
              </label>
              <label>
                {translate('account.phone')}
                <input name="phone" placeholder="+8613812345678" required />
              </label>
              <label>
                {translate('account.password')}
                <input name="password" type="password" placeholder="At least 8 characters" required />
              </label>
              <label>
                {translate('account.confirmPassword')}
                <input name="confirmPassword" type="password" placeholder="Repeat password" required />
              </label>
              <PrimaryButton type="submit" disabled={isBusy}>
                {translate('account.create')}
              </PrimaryButton>
            </form>
          ) : (
            <form
              className="stack-form"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onLoginAccount({
                  email: loginEmailDraft,
                  password: String(formData.get('password') ?? ''),
                })
              }}
            >
              <label>
                {translate('account.email')}
                <input
                  name="loginEmail"
                  type="email"
                  value={loginEmailDraft}
                  placeholder="lin.chen@example.com"
                  onChange={event => onChangeLoginEmailDraft(event.target.value)}
                  required
                />
              </label>
              <label>
                {translate('account.password')}
                <input name="password" type="password" placeholder="Password" required />
              </label>
              <PrimaryButton type="submit" disabled={isBusy}>
                {translate('account.login')}
              </PrimaryButton>
            </form>
          )}
        </AppCard>
      </section>
    )
  }

  return (
    <section className="account-page-stack">
      <AppCard className="account-hero-card">
        <div className="account-hero-layout">
          <div className="account-hero-identity">
            <AvatarUploader
              account={account!}
              isBusy={isBusy}
              translate={translate}
              onUploadAvatar={onUploadAvatar}
              onValidationError={onAvatarValidationError}
            />
          </div>

          <div className="account-hero-summary">
            <SectionHeader
              eyebrow={translate('account.heroEyebrow')}
              title={account?.nickname ?? translate('account.profileTitle')}
              actions={
                <ActionBar>
                  <PrimaryButton type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
                    {translate('account.logoutCurrentSession')}
                  </PrimaryButton>
                  <SecondaryButton type="button" disabled={isBusy} onClick={() => scrollToSection('account-profile-section')}>
                    {translate('account.editProfile')}
                  </SecondaryButton>
                </ActionBar>
              }
            />

            <div className="account-hero-stats">
              <StatCard
                label={translate('account.status')}
                value={localizeAccountStatus(account!.status, currentLanguage)}
              />
              <StatCard
                label={translate('account.membership')}
                value={localizeMembershipLevel(account!.membershipLevel, currentLanguage)}
              />
              <StatCard
                label={translate('account.points')}
                value={account!.points}
              />
              <StatCard
                label={translate('account.primaryTraveler')}
                value={formatTravelerReference(defaultTraveler)}
              />
            </div>
          </div>
        </div>
      </AppCard>

      <AppCard id="account-actions-section">
        <SectionHeader
          eyebrow={translate('account.operationsEyebrow')}
          title={translate('account.operationsTitle')}
        />

        <ActionBar>
          <PrimaryButton type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
            {translate('account.logoutCurrentSession')}
          </PrimaryButton>
          <SecondaryButton type="button" disabled={isBusy} onClick={() => void onRefreshAccount()}>
            {translate('account.refresh')}
          </SecondaryButton>
          <SecondaryButton type="button" disabled={isBusy} onClick={() => void onLogoutOtherSessions()}>
            {translate('account.logoutOtherSessions')}
          </SecondaryButton>
          <SecondaryButton type="button" disabled={isBusy} onClick={onLogout}>
            {translate('account.logout')}
          </SecondaryButton>
        </ActionBar>
      </AppCard>

      <AppCard id="account-profile-section">
        <SectionHeader
          eyebrow={translate('account.profileEyebrow')}
          title={translate('account.profileTitle')}
        />

        <div className="account-profile-grid">
          <div>
            <span className="detail-label">{translate('account.nickname')}</span>
            <strong>{account!.nickname}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.email')}</span>
            <strong>{account!.email}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.phone')}</span>
            <strong>{account!.phone}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.status')}</span>
            <strong>{localizeAccountStatus(account!.status, currentLanguage)}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.membership')}</span>
            <strong>{localizeMembershipLevel(account!.membershipLevel, currentLanguage)}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.points')}</span>
            <strong>{account!.points}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.primaryTraveler')}</span>
            <strong>{formatTravelerReference(defaultTraveler)}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.sessionExpiresAt')}</span>
            <strong>{currentSession ? new Date(currentSession.expiresAt).toLocaleString() : '-'}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.createdAt')}</span>
            <strong>{new Date(account!.createdAt).toLocaleString()}</strong>
          </div>
        </div>
      </AppCard>

      <div className="account-security-grid">
        <AppCard className="account-secondary-card">
          <SectionHeader
            eyebrow={translate('account.security')}
            title={translate('account.changePassword')}
            actions={
              <SecondaryButton
                type="button"
                disabled={isBusy}
                onClick={() => setIsChangePasswordOpen(open => !open)}
              >
                {translate(isChangePasswordOpen ? 'account.hideChangePassword' : 'account.showChangePassword')}
              </SecondaryButton>
            }
          />

          {isChangePasswordOpen ? (
            <form
              className="stack-form"
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
              <label>
                {translate('account.currentPassword')}
                <input name="currentPassword" type="password" placeholder={translate('account.currentPassword')} required />
              </label>
              <label>
                {translate('account.newPassword')}
                <input name="newPassword" type="password" placeholder={translate('account.newPassword')} required />
              </label>
              <label>
                {translate('account.confirmPassword')}
                <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required />
              </label>
              <PrimaryButton type="submit" disabled={isBusy}>
                {translate('account.changePassword')}
              </PrimaryButton>
            </form>
          ) : (
            null
          )}
        </AppCard>

        <AppCard className="account-tertiary-card">
          <SectionHeader
            eyebrow={translate('account.security')}
            title={translate('account.sessions')}
            actions={
              <ActionBar>
                <SecondaryButton
                  type="button"
                  disabled={isBusy}
                  onClick={() => setIsSessionsOpen(open => !open)}
                >
                  {translate(isSessionsOpen ? 'account.hideSessions' : 'account.showSessions')}
                </SecondaryButton>
                <SecondaryButton type="button" disabled={isBusy} onClick={() => void onRefreshSessions()}>
                  {translate('account.refreshSessions')}
                </SecondaryButton>
              </ActionBar>
            }
          />

          {isSessionsOpen ? sessions.length > 0 ? (
            <div className="stack-list">
              {sessions.map(session => (
                <article key={session.sessionId} className="list-card">
                  <div className="detail-grid">
                    <div>
                      <span className="detail-label">{translate('account.sessionStatus')}</span>
                      <strong>{session.isCurrent ? translate('account.currentSession') : session.status}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('account.createdAt')}</span>
                      <strong>{new Date(session.createdAt).toLocaleString()}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('account.lastSeenAt')}</span>
                      <strong>{new Date(session.lastSeenAt).toLocaleString()}</strong>
                    </div>
                    <div>
                      <span className="detail-label">{translate('account.sessionExpiresAt')}</span>
                      <strong>{new Date(session.expiresAt).toLocaleString()}</strong>
                    </div>
                  </div>
                </article>
              ))}
            </div>
          ) : (
            <p className="empty-state">{translate('account.noSessions')}</p>
          ) : null}
        </AppCard>
      </div>
    </section>
  )
}

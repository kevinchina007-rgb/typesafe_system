import { useState } from 'react'

import { AvatarUploader } from './AvatarUploader'
import type { AuthSessionResponse, UserResponse } from '../lib/mvp-types'

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

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.account')}</p>
          <h2>{translate('account.title')}</h2>
        </div>
        {!isGuestMode ? (
          <div className="action-row">
            <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onRefreshAccount()}>
              {translate('account.refresh')}
            </button>
            <button type="button" disabled={isBusy} onClick={onLogoutCurrentSession}>
              {translate('account.logoutCurrentSession')}
            </button>
            <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onLogoutOtherSessions()}>
              {translate('account.logoutOtherSessions')}
            </button>
            <button type="button" className="secondary-button" disabled={isBusy} onClick={onLogout}>
              {translate('account.logout')}
            </button>
          </div>
        ) : null}
      </div>

      {isGuestMode ? (
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
      ) : null}

      {accountEntryMode === 'register' ? (
        <form
          className="stack-form panel-card"
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
          <h3>{translate('account.registerTitle')}</h3>
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
          <button type="submit" disabled={isBusy}>
            {translate('account.create')}
          </button>
        </form>
      ) : (
        <form
          className="stack-form panel-card"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            await onLoginAccount({
              email: loginEmailDraft,
              password: String(formData.get('password') ?? ''),
            })
          }}
        >
          <h3>{translate('account.loginTitle')}</h3>
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
          <button type="submit" disabled={isBusy}>
            {translate('account.login')}
          </button>
          <p className="empty-state">{translate('guest.description')}</p>
        </form>
      )}

      <div className="list-surface">
        <h3>{translate('account.profileTitle')}</h3>
        {account ? (
          <>
            <AvatarUploader
              account={account}
              isBusy={isBusy}
              translate={translate}
              onUploadAvatar={onUploadAvatar}
              onValidationError={onAvatarValidationError}
            />
            <div className="detail-grid">
              <div>
                <span className="detail-label">{translate('account.nickname')}</span>
                <strong>{account.nickname}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.email')}</span>
                <strong>{account.email}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.phone')}</span>
                <strong>{account.phone}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.status')}</span>
                <strong>{account.status}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.membership')}</span>
                <strong>{account.membershipLevel}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.points')}</span>
                <strong>{account.points}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.primaryTraveler')}</span>
                <strong>{account.defaultTravelerProfileId ?? '-'}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.sessionExpiresAt')}</span>
                <strong>{currentSession ? new Date(currentSession.expiresAt).toLocaleString() : '-'}</strong>
              </div>
              <div>
                <span className="detail-label">{translate('account.createdAt')}</span>
                <strong>{new Date(account.createdAt).toLocaleString()}</strong>
              </div>
            </div>

            <div className="page-card">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow-label">{translate('account.security')}</p>
                  <h3>{translate('account.changePassword')}</h3>
                </div>
                <button
                  type="button"
                  className="secondary-button"
                  disabled={isBusy}
                  onClick={() => setIsChangePasswordOpen(open => !open)}
                >
                  {translate(isChangePasswordOpen ? 'account.hideChangePassword' : 'account.showChangePassword')}
                </button>
              </div>
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
                  <button type="submit" disabled={isBusy}>
                    {translate('account.changePassword')}
                  </button>
                </form>
              ) : null}
            </div>

            <div className="list-surface">
              <div className="panel-heading">
                <div>
                  <p className="eyebrow-label">{translate('account.security')}</p>
                  <h3>{translate('account.sessions')}</h3>
                </div>
                <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onRefreshSessions()}>
                  {translate('account.refreshSessions')}
                </button>
              </div>
              {sessions.length > 0 ? (
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
              )}
            </div>
          </>
        ) : (
          <p className="empty-state">{translate('guest.description')}</p>
        )}
      </div>
    </section>
  )
}

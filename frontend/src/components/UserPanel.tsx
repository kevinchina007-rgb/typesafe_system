import type { UserResponse } from '../lib/mvp-types'

type UserPanelProps = {
  account: UserResponse | null
  isBusy: boolean
  isGuestMode: boolean
  loginEmailDraft: string
  translate: (translationKey: string) => string
  onChangeLoginEmailDraft: (email: string) => void
  onRegisterAccount: (payload: {
    email: string
    nickname: string
    phone: string
  }) => Promise<void>
  onLoginAccount: (payload: { email: string }) => Promise<void>
  onRefreshAccount: () => Promise<void>
  onLogout: () => void
}

export function UserPanel({
  account,
  isBusy,
  isGuestMode,
  loginEmailDraft,
  translate,
  onChangeLoginEmailDraft,
  onRegisterAccount,
  onLoginAccount,
  onRefreshAccount,
  onLogout,
}: UserPanelProps) {
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
            <button type="button" disabled={isBusy} onClick={onLogout}>
              {translate('account.logout')}
            </button>
          </div>
        ) : null}
      </div>

      <div className="page-grid">
        <form
          className="stack-form panel-card"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            await onRegisterAccount({
              email: String(formData.get('email') ?? ''),
              nickname: String(formData.get('nickname') ?? ''),
              phone: String(formData.get('phone') ?? ''),
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
          <button type="submit" disabled={isBusy}>
            {translate('account.create')}
          </button>
        </form>

        <form
          className="stack-form panel-card"
          onSubmit={async event => {
            event.preventDefault()
            await onLoginAccount({ email: loginEmailDraft })
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
          <button type="submit" disabled={isBusy}>
            {translate('account.login')}
          </button>
          <p className="empty-state">{translate('guest.description')}</p>
        </form>
      </div>

      <div className="list-surface">
        <h3>{translate('account.profileTitle')}</h3>
        {account ? (
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
              <span className="detail-label">{translate('account.createdAt')}</span>
              <strong>{new Date(account.createdAt).toLocaleString()}</strong>
            </div>
          </div>
        ) : (
          <p className="empty-state">{translate('guest.description')}</p>
        )}
      </div>
    </section>
  )
}

import { useEffect, useState } from 'react'

import { UserPanel } from '../../components/UserPanel'
import { travelMvpApiClient } from '../../lib/api-client'
import type { AppLanguage, AppViewKey, CurrentManagerSessionResponse, ManagerType, UserResponse } from '../../lib/mvp-types'
import { usePageActions, type PageNoticeHandler } from '../shared/usePageActions'

type AccountEntryMode = 'register' | 'login'

type AccountPageProps = {
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

export function AccountPage({
  currentLanguage,
  signedInUser,
  signedInManager,
  requestedEntryMode,
  translate,
  onSignedInUserChange,
  onSignedInManagerChange,
  onNavigate,
  onShowNotice,
}: AccountPageProps) {
  const [accountEntryMode, setAccountEntryMode] = useState<AccountEntryMode>(requestedEntryMode)
  const [loginEmailDraft, setLoginEmailDraft] = useState('')
  const [managerAuthMode, setManagerAuthMode] = useState<'login' | 'register'>('login')
  const [managerTypeDraft, setManagerTypeDraft] = useState<ManagerType>('airline')
  const { isBusy, runPageAction } = usePageActions(currentLanguage, translate, onShowNotice)

  useEffect(() => {
    setAccountEntryMode(requestedEntryMode)
  }, [requestedEntryMode])

  return (
    <>
      <UserPanel
      account={signedInUser}
      accountEntryMode={accountEntryMode}
      isBusy={isBusy}
      isGuestMode={signedInUser === null}
      loginEmailDraft={loginEmailDraft}
      translate={translate}
      onChangeAccountEntryMode={setAccountEntryMode}
      onChangeLoginEmailDraft={setLoginEmailDraft}
      onRegisterAccount={async payload => {
        await runPageAction(async () => {
          const createdSession = await travelMvpApiClient.signupUser(payload)
          onSignedInUserChange(createdSession.user)
          setLoginEmailDraft(createdSession.user.email)
          onNavigate('travelers')
        }, translate('account.create'), translate('notice.registerSuccess'))
      }}
      onLoginAccount={async payload => {
        await runPageAction(async () => {
          const nextSignedInSession = await travelMvpApiClient.loginUserWithPassword(payload)
          onSignedInUserChange(nextSignedInSession.user)
          onNavigate('travelers')
        }, translate('account.login'), translate('notice.loginSuccess'))
      }}
      onUploadAvatar={async avatarFile => {
        if (!signedInUser) {
          throw new Error(translate('error.loginRequired'))
        }
        await runPageAction(async () => {
          const updatedAccount = await travelMvpApiClient.uploadUserAvatar(signedInUser.userId, avatarFile)
          onSignedInUserChange(updatedAccount)
        }, translate('account.avatarUpload'), translate('notice.avatarUploaded'))
      }}
      onAvatarValidationError={message => {
        onShowNotice('error', translate('error.friendly.default'), message)
      }}
      onValidationError={message => {
        onShowNotice('error', translate('error.friendly.default'), message)
      }}
      onRefreshAccount={async () => {
        if (!signedInUser) {
          return
        }
        await runPageAction(async () => {
          const refreshedAccount = await travelMvpApiClient.getUser(signedInUser.userId)
          onSignedInUserChange(refreshedAccount)
        }, translate('account.refresh'), translate('notice.actionSuccess'))
      }}
      onLogout={() => {
        void runPageAction(async () => {
          await travelMvpApiClient.logoutUser()
          onSignedInUserChange(null)
          onNavigate('blog')
        }, translate('account.logout'), translate('notice.logoutSuccess'))
      }}
    />

      <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.manager')}</p>
          <h2>{translate('account.managerAccess')}</h2>
        </div>
        {signedInManager ? (
          <button
            type="button"
            className="secondary-button"
            disabled={isBusy}
            onClick={() => {
              void runPageAction(async () => {
                await travelMvpApiClient.logoutManagerAuth()
                onSignedInManagerChange(null)
                onNavigate('blog')
              }, translate('manager.logout'), translate('notice.logoutSuccess'))
            }}
          >
            {translate('manager.logout')}
          </button>
        ) : null}
      </div>

      {signedInManager ? (
        <div className="detail-grid">
          <div>
            <span className="detail-label">{translate('manager.displayName')}</span>
            <strong>{signedInManager.displayName}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('manager.scope')}</span>
            <strong>{signedInManager.scopeId}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('manager.status')}</span>
            <strong>{signedInManager.status}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('account.sessionExpiresAt')}</span>
            <strong>{new Date(signedInManager.expiresAt).toLocaleString()}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('manager.email')}</span>
            <strong>{signedInManager.email}</strong>
          </div>
          <div>
            <span className="detail-label">{translate('manager.profile')}</span>
            <strong>{signedInManager.managerType}</strong>
          </div>
        </div>
      ) : (
        <div className="stack-form">
          <div className="action-row">
            <button
              type="button"
              className={managerAuthMode === 'login' ? '' : 'secondary-button'}
              disabled={isBusy}
              onClick={() => setManagerAuthMode('login')}
            >
              {translate('account.managerLogin')}
            </button>
            <button
              type="button"
              className={managerAuthMode === 'register' ? '' : 'secondary-button'}
              disabled={isBusy}
              onClick={() => setManagerAuthMode('register')}
            >
              {translate('account.managerRegister')}
            </button>
          </div>

          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)

              if (managerAuthMode === 'login') {
                await runPageAction(async () => {
                  const nextManagerSession = await travelMvpApiClient.loginManagerAuth({
                    managerType: managerTypeDraft,
                    email: String(formData.get('email') ?? '').trim(),
                    password: String(formData.get('password') ?? ''),
                  })
                  onSignedInManagerChange(nextManagerSession)
                  onNavigate('manager')
                }, translate('account.managerLogin'), translate('notice.loginSuccess'))
                return
              }

              await runPageAction(async () => {
                const commonPayload = {
                  email: String(formData.get('email') ?? '').trim(),
                  displayName: String(formData.get('displayName') ?? '').trim(),
                  password: String(formData.get('password') ?? ''),
                }
                const confirmPassword = String(formData.get('confirmPassword') ?? '')
                if (commonPayload.password !== confirmPassword) {
                  throw new Error(translate('error.passwordMismatch'))
                }

                if (managerTypeDraft === 'airline') {
                  await travelMvpApiClient.registerAirlineManager({
                    ...commonPayload,
                    airlineName: String(formData.get('airlineName') ?? '').trim(),
                    airlineCode: String(formData.get('airlineCode') ?? '').trim(),
                  })
                } else if (managerTypeDraft === 'hotel') {
                  await travelMvpApiClient.registerHotelManager({
                    ...commonPayload,
                    hotelName: String(formData.get('hotelName') ?? '').trim(),
                    location: String(formData.get('location') ?? '').trim(),
                  })
                } else if (managerTypeDraft === 'train') {
                  await travelMvpApiClient.registerRailwayManager({
                    ...commonPayload,
                    operatorCode: String(formData.get('operatorCode') ?? '').trim(),
                  })
                } else {
                  await travelMvpApiClient.registerAttractionManager(commonPayload)
                }

                const nextManagerSession = await travelMvpApiClient.loginManagerAuth({
                  managerType: managerTypeDraft,
                  email: commonPayload.email,
                  password: commonPayload.password,
                })
                onSignedInManagerChange(nextManagerSession)
                onNavigate('manager')
              }, translate('account.managerRegister'), translate('notice.registerSuccess'))
            }}
          >
            <h3>{translate(managerAuthMode === 'login' ? 'account.managerLogin' : 'account.managerRegister')}</h3>
            <label>
              {translate('manager.type')}
              <select value={managerTypeDraft} onChange={event => setManagerTypeDraft(event.target.value as ManagerType)}>
                <option value="airline">{translate('manager.type.airline')}</option>
                <option value="hotel">{translate('manager.type.hotel')}</option>
                <option value="train">{translate('manager.type.train')}</option>
                <option value="attraction">{translate('manager.type.attraction')}</option>
              </select>
            </label>
            <label>
              {translate('manager.email')}
              <input name="email" type="email" placeholder="ops@example.com" required />
            </label>
            {managerAuthMode === 'register' ? (
              <label>
                {translate('manager.displayName')}
                <input name="displayName" placeholder={translate('manager.displayName')} required />
              </label>
            ) : null}
            <label>
              {translate('account.password')}
              <input name="password" type="password" placeholder={translate('account.password')} required />
            </label>
            {managerAuthMode === 'register' ? (
              <label>
                {translate('account.confirmPassword')}
                <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required />
              </label>
            ) : null}
            {managerAuthMode === 'register' && managerTypeDraft === 'airline' ? (
              <>
                <label>
                  {translate('manager.airlineName')}
                  <input name="airlineName" placeholder={translate('manager.airlineName')} required />
                </label>
                <label>
                  {translate('manager.airlineCode')}
                  <input name="airlineCode" placeholder="MU" required />
                </label>
              </>
            ) : null}
            {managerAuthMode === 'register' && managerTypeDraft === 'hotel' ? (
              <>
                <label>
                  {translate('manager.hotelName')}
                  <input name="hotelName" placeholder={translate('manager.hotelName')} required />
                </label>
                <label>
                  {translate('manager.hotelLocation')}
                  <input name="location" placeholder={translate('manager.hotelLocation')} required />
                </label>
              </>
            ) : null}
            {managerAuthMode === 'register' && managerTypeDraft === 'train' ? (
              <label>
                {translate('trainAdmin.operatorCode')}
                <input name="operatorCode" placeholder="CRH" required />
              </label>
            ) : null}
            <button type="submit" disabled={isBusy}>
              {translate(managerAuthMode === 'login' ? 'account.managerLogin' : 'account.managerRegister')}
            </button>
          </form>
        </div>
      )}
      </section>
    </>
  )
}

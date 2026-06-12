import { getPasswordValidationMessage } from '@/pages/shared/auth/passwordValidation'
import type { AccountPageController } from '../../objects'
import { accountEntryCardClassName, authPrimaryButtonClassName, inputClassName, labelClassName, primaryButtonClassName, secondaryButtonClassName } from '../../functions'
import { SectionHeader } from '../shared/SectionHeader'

type AccountGuestSectionProps = {
  controller: AccountPageController
  translate: (translationKey: string) => string
}

// 未登录状态下的账号入口区域。
export function AccountGuestSection({ controller, translate }: AccountGuestSectionProps) {
  const {
    accountEntryMode,
    isBusy,
    loginEmailDraft,
    loginPasswordDraft,
    isLoginFormWritable,
    setLoginPasswordDraft,
    setIsLoginFormWritable,
    setLoginEmailDraft,
    switchAccountEntryMode,
    onRegisterAccount,
    onLoginAccount,
    onValidationError,
  } = controller

  return (
    <section className="grid gap-4">
      <section className={accountEntryCardClassName}>
        <SectionHeader
          eyebrow={translate('account.entryEyebrow')}
          title={translate(accountEntryMode === 'register' ? 'account.registerTitle' : 'account.loginTitle')}
        />

        <div className="flex flex-wrap items-center gap-3">
          <button type="button" className={accountEntryMode === 'register' ? primaryButtonClassName : secondaryButtonClassName} disabled={isBusy} onClick={() => switchAccountEntryMode('register')}>
            {translate('account.create')}
          </button>
          <button type="button" className={accountEntryMode === 'login' ? primaryButtonClassName : secondaryButtonClassName} disabled={isBusy} onClick={() => switchAccountEntryMode('login')}>
            {translate('account.login')}
          </button>
        </div>

        {accountEntryMode === 'register' ? (
          <form
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
            <label className={labelClassName}>{translate('account.nickname')}<input className={inputClassName} name="registerNickname" autoComplete="off" required /></label>
            <label className={labelClassName}>{translate('account.email')}<input className={inputClassName} name="registerEmail" type="email" autoComplete="off" required /></label>
            <label className={labelClassName}>{translate('account.phone')}<input className={inputClassName} name="registerPhone" autoComplete="off" required /></label>
            <label className={labelClassName}>{translate('account.password')}<input className={inputClassName} name="registerPassword" type="password" autoComplete="new-password" required /></label>
            <label className={labelClassName}>{translate('account.confirmPassword')}<input className={inputClassName} name="registerConfirmPassword" type="password" autoComplete="new-password" required /></label>
            <button className={authPrimaryButtonClassName} type="submit" disabled={isBusy}>{translate('account.create')}</button>
          </form>
        ) : (
          <form
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
                onChange={event => setLoginEmailDraft(event.target.value)}
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
            <button className={authPrimaryButtonClassName} type="submit" disabled={isBusy}>{translate('account.login')}</button>
          </form>
        )}
      </section>
    </section>
  )
}

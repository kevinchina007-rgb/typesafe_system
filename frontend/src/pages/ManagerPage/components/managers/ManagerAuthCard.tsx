import { useEffect, useRef, useState } from 'react'

import { getPasswordValidationMessage } from '@/pages/shared/auth/passwordValidation'
import type { ManagerAuthCardProps, ManagerAuthMode } from '@/pages/ManagerPage/objects'
import {
  managerActionsClassName,
  managerAuthCardClassName,
  managerAuthPrimaryButtonClassName,
  managerEyebrowClassName,
  managerFormGridClassName,
  managerFormTitleClassName,
  managerHeaderClassName,
  managerInputClassName,
  managerLabelClassName,
  managerSecondaryButtonClassName,
  managerTitleClassName,
} from '@/pages/ManagerPage/functions'

export function ManagerAuthCard({
  title,
  registerTitle,
  loginTitle,
  initialAuthMode,
  registerFields,
  loginManagerType,
  isBusy,
  onValidationError,
  onRegister,
  onLogin,
  onBack,
  hideBack = false,
  eyebrow,
  allowRegister = false,
  translate,
}: ManagerAuthCardProps) {
  const [authMode, setAuthMode] = useState<ManagerAuthMode>(initialAuthMode)
  const registerFormRef = useRef<HTMLFormElement>(null)
  const loginFormRef = useRef<HTMLFormElement>(null)

  function resetManagerAuthForms() {
    registerFormRef.current?.reset()
    loginFormRef.current?.reset()
  }

  useEffect(() => {
    setAuthMode(initialAuthMode)
    resetManagerAuthForms()
  }, [initialAuthMode, loginManagerType])

  return (
    <article className={managerAuthCardClassName}>
      <div className={managerHeaderClassName}>
        <p className={managerEyebrowClassName}>{eyebrow ?? translate('nav.managerCenter')}</p>
        <h3 className={managerTitleClassName}>{title}</h3>
      </div>

      <div className={managerActionsClassName}>
        {!hideBack ? (
          <button type="button" className={managerSecondaryButtonClassName} onClick={onBack}>
            {translate('manager.backToCategories')}
          </button>
        ) : null}
        {loginManagerType !== 'siteAdmin' || allowRegister ? (
          <button
            type="button"
            className={authMode === 'register' ? managerAuthPrimaryButtonClassName : managerSecondaryButtonClassName}
            onClick={() => {
              setAuthMode('register')
              resetManagerAuthForms()
            }}
          >
            {translate('manager.createAccount')}
          </button>
        ) : null}
        <button
          type="button"
          className={authMode === 'login' || loginManagerType === 'siteAdmin' ? managerAuthPrimaryButtonClassName : managerSecondaryButtonClassName}
          onClick={() => {
            setAuthMode('login')
            resetManagerAuthForms()
          }}
        >
          {translate('manager.login')}
        </button>
      </div>

      {authMode === 'register' && (loginManagerType !== 'siteAdmin' || allowRegister) ? (
        <form
          ref={registerFormRef}
          className={managerFormGridClassName}
          autoComplete="off"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            const password = String(formData.get('password') ?? '')
            const confirmPassword = String(formData.get('confirmPassword') ?? '')
            if (password !== confirmPassword) {
              onValidationError(translate('error.passwordMismatch'))
              return
            }
            const email = String(formData.get('email') ?? '').trim()
            const passwordValidationMessage = getPasswordValidationMessage(password, email)
            if (passwordValidationMessage) {
              onValidationError(passwordValidationMessage)
              return
            }

            const payload = Object.fromEntries(formData.entries()) as Record<string, string>
            await onRegister(payload)
            resetManagerAuthForms()
            event.currentTarget.reset()
          }}
        >
          <h4 className={managerFormTitleClassName}>{registerTitle}</h4>
          {registerFields.map(field => (
            <label key={field.name} className={managerLabelClassName}>
              {field.label}
              <input
                className={managerInputClassName}
                name={field.name}
                type={field.type ?? 'text'}
                autoComplete="off"
                required
                disabled={isBusy}
              />
            </label>
          ))}
          <label className={managerLabelClassName}>
            {translate('account.password')}
            <input className={managerInputClassName} name="password" type="password" autoComplete="new-password" required disabled={isBusy} />
          </label>
          <label className={managerLabelClassName}>
            {translate('account.confirmPassword')}
            <input className={managerInputClassName} name="confirmPassword" type="password" autoComplete="new-password" required disabled={isBusy} />
          </label>
          <button type="submit" className={managerAuthPrimaryButtonClassName} disabled={isBusy}>
            {translate('manager.createAccount')}
          </button>
        </form>
      ) : null}

      {authMode === 'login' ? (
        <form
          ref={loginFormRef}
          className={managerFormGridClassName}
          autoComplete="off"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            await onLogin({
              managerType: loginManagerType,
              email: String(formData.get('email') ?? '').trim(),
              password: String(formData.get('password') ?? ''),
            })
            resetManagerAuthForms()
          }}
        >
          <h4 className={managerFormTitleClassName}>{loginTitle}</h4>
          <label className={managerLabelClassName}>
            {translate('manager.email')}
            <input className={managerInputClassName} name="email" type="email" autoComplete="off" required disabled={isBusy} />
          </label>
          <label className={managerLabelClassName}>
            {translate('account.password')}
            <input className={managerInputClassName} name="password" type="password" autoComplete="off" required disabled={isBusy} />
          </label>
          <button type="submit" className={managerAuthPrimaryButtonClassName} disabled={isBusy}>
            {translate('manager.login')}
          </button>
        </form>
      ) : null}
    </article>
  )
}

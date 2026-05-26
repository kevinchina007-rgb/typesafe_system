import type { ManagerPanelProps } from '@/pages/ManagerPage/components/managers/manager-panel-shared'

import { getPasswordValidationMessage } from '@/pages/shared/auth/passwordValidation'

type ManagerPanelAuthProps = Pick<
  ManagerPanelProps,
  'isBusy' | 'translate' | 'onRegisterAirlineManager' | 'onRegisterHotelManager' | 'onLoginManager' | 'onValidationError'
>

const managerAuthGridClassName = 'mx-auto grid max-w-5xl gap-6 lg:grid-cols-2'
const managerAuthFormClassName = 'grid w-full gap-5 self-start border border-slate-200 bg-white p-6 shadow-sm shadow-slate-950/5'
const managerAuthTitleClassName = 'text-2xl font-bold text-slate-950'
const managerAuthLabelClassName = 'grid gap-2 text-sm font-semibold text-slate-600'
const managerAuthInputClassName =
  'min-h-12 border border-slate-200 bg-white px-4 text-base text-slate-950 outline-none transition placeholder:text-slate-400 focus:border-black disabled:cursor-not-allowed disabled:bg-slate-100'
const managerAuthButtonClassName =
  'inline-flex min-h-12 w-fit items-center justify-center border border-pink-500 bg-pink-500 px-6 py-3 text-sm font-semibold text-white transition hover:border-pink-600 hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-60'

export function ManagerPanelAuth({
  isBusy,
  translate,
  onRegisterAirlineManager,
  onRegisterHotelManager,
  onLoginManager,
  onValidationError,
}: ManagerPanelAuthProps) {
  return (
    <>
      <div className={managerAuthGridClassName}>
        <form
          className={managerAuthFormClassName}
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
            await onRegisterAirlineManager({
              email,
              displayName: String(formData.get('displayName') ?? ''),
              airlineName: String(formData.get('airlineName') ?? ''),
              airlineCode: String(formData.get('airlineCode') ?? ''),
              password,
            })
            event.currentTarget.reset()
          }}
        >
          <h3 className={managerAuthTitleClassName}>{translate('manager.registerAirline')}</h3>
          <label className={managerAuthLabelClassName}>
            {translate('manager.displayName')}
            <input className={managerAuthInputClassName} name="displayName" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('manager.email')}
            <input className={managerAuthInputClassName} name="email" type="email" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('manager.airlineName')}
            <input className={managerAuthInputClassName} name="airlineName" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('manager.airlineCode')}
            <input className={managerAuthInputClassName} name="airlineCode" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('account.password')}
            <input className={managerAuthInputClassName} name="password" type="password" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('account.confirmPassword')}
            <input
              className={managerAuthInputClassName}
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              required
            />
          </label>
          <button type="submit" className={managerAuthButtonClassName} disabled={isBusy}>
            {translate('manager.createAccount')}
          </button>
        </form>

        <form
          className={managerAuthFormClassName}
          autoComplete="off"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            await onLoginManager({
              managerType: 'airline',
              email: String(formData.get('email') ?? ''),
              password: String(formData.get('password') ?? ''),
            })
          }}
        >
          <h3 className={managerAuthTitleClassName}>{translate('manager.loginAirline')}</h3>
          <label className={managerAuthLabelClassName}>
            {translate('manager.email')}
            <input className={managerAuthInputClassName} name="email" type="email" autoComplete="off" required disabled={isBusy} />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('account.password')}
            <input className={managerAuthInputClassName} name="password" type="password" autoComplete="off" required disabled={isBusy} />
          </label>
          <button type="submit" className={managerAuthButtonClassName} disabled={isBusy}>
            {translate('manager.loginAirline')}
          </button>
        </form>
      </div>

      <div className={managerAuthGridClassName}>
        <form
          className={managerAuthFormClassName}
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
            await onRegisterHotelManager({
              email,
              displayName: String(formData.get('displayName') ?? ''),
              hotelName: String(formData.get('hotelName') ?? ''),
              location: String(formData.get('location') ?? ''),
              password,
            })
            event.currentTarget.reset()
          }}
        >
          <h3 className={managerAuthTitleClassName}>{translate('manager.registerHotel')}</h3>
          <label className={managerAuthLabelClassName}>
            {translate('manager.displayName')}
            <input className={managerAuthInputClassName} name="displayName" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('manager.email')}
            <input className={managerAuthInputClassName} name="email" type="email" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('manager.hotelName')}
            <input className={managerAuthInputClassName} name="hotelName" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('manager.hotelLocation')}
            <input className={managerAuthInputClassName} name="location" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('account.password')}
            <input className={managerAuthInputClassName} name="password" type="password" autoComplete="off" required />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('account.confirmPassword')}
            <input
              className={managerAuthInputClassName}
              name="confirmPassword"
              type="password"
              autoComplete="new-password"
              required
            />
          </label>
          <button type="submit" className={managerAuthButtonClassName} disabled={isBusy}>
            {translate('manager.createAccount')}
          </button>
        </form>

        <form
          className={managerAuthFormClassName}
          autoComplete="off"
          onSubmit={async event => {
            event.preventDefault()
            const formData = new FormData(event.currentTarget)
            await onLoginManager({
              managerType: 'hotel',
              email: String(formData.get('email') ?? ''),
              password: String(formData.get('password') ?? ''),
            })
          }}
        >
          <h3 className={managerAuthTitleClassName}>{translate('manager.loginHotel')}</h3>
          <label className={managerAuthLabelClassName}>
            {translate('manager.email')}
            <input className={managerAuthInputClassName} name="email" type="email" autoComplete="off" required disabled={isBusy} />
          </label>
          <label className={managerAuthLabelClassName}>
            {translate('account.password')}
            <input className={managerAuthInputClassName} name="password" type="password" autoComplete="off" required disabled={isBusy} />
          </label>
          <button type="submit" className={managerAuthButtonClassName} disabled={isBusy}>
            {translate('manager.loginHotel')}
          </button>
        </form>
      </div>
    </>
  )
}

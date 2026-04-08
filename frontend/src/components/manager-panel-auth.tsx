import type { ManagerPanelProps } from './manager-panel-shared'

type ManagerPanelAuthProps = Pick<
  ManagerPanelProps,
  'isBusy' | 'translate' | 'onRegisterAirlineManager' | 'onRegisterHotelManager' | 'onLoginManager' | 'onValidationError'
>

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
      <div className="two-column-grid">
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
            await onRegisterAirlineManager({
              email: String(formData.get('email') ?? ''),
              displayName: String(formData.get('displayName') ?? ''),
              airlineName: String(formData.get('airlineName') ?? ''),
              airlineCode: String(formData.get('airlineCode') ?? ''),
              password,
            })
            event.currentTarget.reset()
          }}
        >
          <h3>{translate('manager.registerAirline')}</h3>
          <label>
            {translate('manager.displayName')}
            <input name="displayName" placeholder={translate('manager.displayName')} required />
          </label>
          <label>
            {translate('manager.email')}
            <input name="email" type="email" placeholder={translate('manager.email')} required />
          </label>
          <label>
            {translate('manager.airlineName')}
            <input name="airlineName" placeholder={translate('manager.airlineName')} required />
          </label>
          <label>
            {translate('manager.airlineCode')}
            <input name="airlineCode" placeholder="MU" required />
          </label>
          <label>
            {translate('account.password')}
            <input name="password" type="password" placeholder={translate('account.password')} required />
          </label>
          <label>
            {translate('account.confirmPassword')}
            <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required />
          </label>
          <button type="submit" disabled={isBusy}>
            {translate('manager.createAccount')}
          </button>
        </form>

        <form
          className="stack-form panel-card"
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
          <h3>{translate('manager.loginAirline')}</h3>
          <label>
            {translate('manager.email')}
            <input name="email" type="email" placeholder={translate('manager.email')} required disabled={isBusy} />
          </label>
          <label>
            {translate('account.password')}
            <input name="password" type="password" placeholder={translate('account.password')} required disabled={isBusy} />
          </label>
          <button type="submit" disabled={isBusy}>
            {translate('manager.loginAirline')}
          </button>
        </form>
      </div>

      <div className="two-column-grid">
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
            await onRegisterHotelManager({
              email: String(formData.get('email') ?? ''),
              displayName: String(formData.get('displayName') ?? ''),
              hotelName: String(formData.get('hotelName') ?? ''),
              location: String(formData.get('location') ?? ''),
              password,
            })
            event.currentTarget.reset()
          }}
        >
          <h3>{translate('manager.registerHotel')}</h3>
          <label>
            {translate('manager.displayName')}
            <input name="displayName" placeholder={translate('manager.displayName')} required />
          </label>
          <label>
            {translate('manager.email')}
            <input name="email" type="email" placeholder={translate('manager.email')} required />
          </label>
          <label>
            {translate('manager.hotelName')}
            <input name="hotelName" placeholder={translate('manager.hotelName')} required />
          </label>
          <label>
            {translate('manager.hotelLocation')}
            <input name="location" placeholder={translate('manager.hotelLocation')} required />
          </label>
          <label>
            {translate('account.password')}
            <input name="password" type="password" placeholder={translate('account.password')} required />
          </label>
          <label>
            {translate('account.confirmPassword')}
            <input name="confirmPassword" type="password" placeholder={translate('account.confirmPassword')} required />
          </label>
          <button type="submit" disabled={isBusy}>
            {translate('manager.createAccount')}
          </button>
        </form>

        <form
          className="stack-form panel-card"
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
          <h3>{translate('manager.loginHotel')}</h3>
          <label>
            {translate('manager.email')}
            <input name="email" type="email" placeholder={translate('manager.email')} required disabled={isBusy} />
          </label>
          <label>
            {translate('account.password')}
            <input name="password" type="password" placeholder={translate('account.password')} required disabled={isBusy} />
          </label>
          <button type="submit" disabled={isBusy}>
            {translate('manager.loginHotel')}
          </button>
        </form>
      </div>
    </>
  )
}

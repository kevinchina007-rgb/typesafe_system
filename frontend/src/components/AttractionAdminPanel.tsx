import type { AppLanguage, AttractionAdminSessionResponse } from '../lib/mvp-types'

type AttractionAdminPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  attractionAdminSession: AttractionAdminSessionResponse | null
  translate: (translationKey: string) => string
  onRegisterAttractionManager: (payload: {
    email: string
    displayName: string
  }) => Promise<void>
  onLoginAttractionManager: (payload: { email: string }) => Promise<void>
  onReloadManagedAttractions: () => Promise<void>
  onCreateAttraction: (payload: {
    attractionName: string
    city: string
    location: string
    description: string
  }) => Promise<void>
  onCreateTicketType: (payload: {
    attractionId: string
    ticketTypeName: string
    description: string
    unitPrice: string
    currency: string
  }) => Promise<void>
  onCreateRule: (payload: {
    attractionId: string
    ticketTypeId: string
    ruleType: string
    ageValue?: number | null
    minAge?: number | null
    maxAge?: number | null
    documentType?: string | null
    documentNumberPrefix?: string | null
  }) => Promise<void>
  onLogoutAttractionManager: () => void
}

export function AttractionAdminPanel({
  isBusy,
  attractionAdminSession,
  translate,
  onRegisterAttractionManager,
  onLoginAttractionManager,
  onReloadManagedAttractions,
  onCreateAttraction,
  onCreateTicketType,
  onCreateRule,
  onLogoutAttractionManager,
}: AttractionAdminPanelProps) {
  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.attractionAdmin')}</p>
          <h2>{translate('attractionAdmin.title')}</h2>
        </div>
        {attractionAdminSession ? (
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onLogoutAttractionManager}>
            {translate('manager.logout')}
          </button>
        ) : null}
      </div>

      <p className="hero-copy">{translate('attractionAdmin.description')}</p>

      {!attractionAdminSession ? (
        <div className="two-column-grid">
          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onRegisterAttractionManager({
                email: String(formData.get('email') ?? '').trim(),
                displayName: String(formData.get('displayName') ?? '').trim(),
              })
            }}
          >
            <h3>{translate('attractionAdmin.registerTitle')}</h3>
            <label>
              {translate('attractionAdmin.email')}
              <input name="email" type="email" placeholder="attraction@example.com" required />
            </label>
            <label>
              {translate('attractionAdmin.displayName')}
              <input name="displayName" placeholder={translate('attractionAdmin.displayNamePlaceholder')} required />
            </label>
            <button type="submit" disabled={isBusy}>
              {translate('attractionAdmin.createAccount')}
            </button>
          </form>

          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onLoginAttractionManager({
                email: String(formData.get('email') ?? '').trim(),
              })
            }}
          >
            <h3>{translate('attractionAdmin.loginTitle')}</h3>
            <label>
              {translate('attractionAdmin.email')}
              <input name="email" type="email" placeholder="attraction@example.com" required />
            </label>
            <button type="submit" disabled={isBusy}>
              {translate('attractionAdmin.login')}
            </button>
          </form>
        </div>
      ) : (
        <div className="stack-form">
          <div className="panel-card">
            <div className="panel-heading">
              <div>
                <strong>{attractionAdminSession.displayName}</strong>
                <p>{attractionAdminSession.email}</p>
              </div>
              <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onReloadManagedAttractions()}>
                {translate('attractionAdmin.refresh')}
              </button>
            </div>
          </div>

          <div className="two-column-grid">
            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onCreateAttraction({
                  attractionName: String(formData.get('attractionName') ?? '').trim(),
                  city: String(formData.get('city') ?? '').trim(),
                  location: String(formData.get('location') ?? '').trim(),
                  description: String(formData.get('description') ?? '').trim(),
                })
                event.currentTarget.reset()
              }}
            >
              <h3>{translate('attractionAdmin.createAttraction')}</h3>
              <label>
                {translate('attractionAdmin.attractionName')}
                <input name="attractionName" placeholder={translate('attractionAdmin.attractionNamePlaceholder')} required />
              </label>
              <label>
                {translate('attractionAdmin.city')}
                <input name="city" placeholder={translate('attractionAdmin.cityPlaceholder')} required />
              </label>
              <label>
                {translate('attractionAdmin.location')}
                <input name="location" placeholder={translate('attractionAdmin.locationPlaceholder')} required />
              </label>
              <label>
                {translate('attractionAdmin.descriptionField')}
                <input name="description" placeholder={translate('attractionAdmin.descriptionPlaceholder')} required />
              </label>
              <button type="submit" disabled={isBusy}>
                {translate('attractionAdmin.createAttraction')}
              </button>
            </form>

            <form
              className="stack-form panel-card"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onCreateTicketType({
                  attractionId: String(formData.get('attractionId') ?? '').trim(),
                  ticketTypeName: String(formData.get('ticketTypeName') ?? '').trim(),
                  description: String(formData.get('description') ?? '').trim(),
                  unitPrice: String(formData.get('unitPrice') ?? '').trim(),
                  currency: String(formData.get('currency') ?? '').trim(),
                })
                event.currentTarget.reset()
              }}
            >
              <h3>{translate('attractionAdmin.createTicketType')}</h3>
              <label>
                {translate('attractionAdmin.attraction')}
                <select name="attractionId" required defaultValue="">
                  <option value="" disabled>
                    {translate('attractionAdmin.attractionSelect')}
                  </option>
                  {attractionAdminSession.managedAttractions.map(attraction => (
                    <option key={attraction.attractionId} value={attraction.attractionId}>
                      {attraction.attractionName}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                {translate('attractionAdmin.ticketTypeName')}
                <input name="ticketTypeName" placeholder={translate('attractionAdmin.ticketTypeNamePlaceholder')} required />
              </label>
              <label>
                {translate('attractionAdmin.descriptionField')}
                <input name="description" placeholder={translate('attractionAdmin.ticketTypeDescriptionPlaceholder')} required />
              </label>
              <label>
                {translate('attractionAdmin.unitPrice')}
                <input name="unitPrice" type="number" min="0" step="0.01" placeholder="99" required />
              </label>
              <label>
                {translate('attractionAdmin.currency')}
                <input name="currency" placeholder="CNY" defaultValue="CNY" required />
              </label>
              <button type="submit" disabled={isBusy}>
                {translate('attractionAdmin.createTicketType')}
              </button>
            </form>
          </div>

          <form
            className="stack-form panel-card"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              const ruleType = String(formData.get('ruleType') ?? '').trim()
              await onCreateRule({
                attractionId: String(formData.get('attractionId') ?? '').trim(),
                ticketTypeId: String(formData.get('ticketTypeId') ?? '').trim(),
                ruleType,
                ageValue: formData.get('ageValue') ? Number(formData.get('ageValue')) : null,
                minAge: formData.get('minAge') ? Number(formData.get('minAge')) : null,
                maxAge: formData.get('maxAge') ? Number(formData.get('maxAge')) : null,
                documentType: formData.get('documentType') ? String(formData.get('documentType')) : null,
                documentNumberPrefix: formData.get('documentNumberPrefix') ? String(formData.get('documentNumberPrefix')) : null,
              })
              event.currentTarget.reset()
            }}
          >
            <h3>{translate('attractionAdmin.createRule')}</h3>
            <div className="three-column-grid">
              <label>
                {translate('attractionAdmin.attraction')}
                <select name="attractionId" required defaultValue="">
                  <option value="" disabled>
                    {translate('attractionAdmin.attractionSelect')}
                  </option>
                  {attractionAdminSession.managedAttractions.map(attraction => (
                    <option key={attraction.attractionId} value={attraction.attractionId}>
                      {attraction.attractionName}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                {translate('attractionAdmin.ticketTypeId')}
                <input name="ticketTypeId" placeholder={translate('attractionAdmin.ticketTypeIdPlaceholder')} required />
              </label>
              <label>
                {translate('attractionAdmin.ruleType')}
                <select name="ruleType" required defaultValue="AgeLessThan">
                  <option value="AgeLessThan">AgeLessThan</option>
                  <option value="AgeBetween">AgeBetween</option>
                  <option value="AgeAtLeast">AgeAtLeast</option>
                  <option value="DocumentTypeEquals">DocumentTypeEquals</option>
                  <option value="DocumentNumberPrefix">DocumentNumberPrefix</option>
                </select>
              </label>
              <label>
                {translate('attractionAdmin.ageValue')}
                <input name="ageValue" type="number" min="0" placeholder="18" />
              </label>
              <label>
                {translate('attractionAdmin.minAge')}
                <input name="minAge" type="number" min="0" placeholder="60" />
              </label>
              <label>
                {translate('attractionAdmin.maxAge')}
                <input name="maxAge" type="number" min="0" placeholder="70" />
              </label>
              <label>
                {translate('attractionAdmin.documentType')}
                <input name="documentType" placeholder="NationalIdentityCard" />
              </label>
              <label>
                {translate('attractionAdmin.documentNumberPrefix')}
                <input name="documentNumberPrefix" placeholder="310" />
              </label>
            </div>
            <button type="submit" disabled={isBusy}>
              {translate('attractionAdmin.createRule')}
            </button>
          </form>

          <div className="entity-list">
            {attractionAdminSession.managedAttractions.map(attraction => (
              <article key={attraction.attractionId} className="panel-card">
                <strong>{attraction.attractionName}</strong>
                <p>{`${attraction.city} | ${attraction.location}`}</p>
                <ul className="entity-list">
                  {attraction.ticketTypes.map(ticketType => (
                    <li key={ticketType.ticketTypeId}>
                      <div>
                        <strong>{`${ticketType.ticketTypeName} (${ticketType.ticketTypeId})`}</strong>
                        <p>{`${ticketType.priceAmount} ${ticketType.priceCurrency}`}</p>
                        <p>{ticketType.rules.length > 0 ? ticketType.rules.map(rule => rule.summary).join(' | ') : translate('attractionAdmin.noRules')}</p>
                      </div>
                    </li>
                  ))}
                </ul>
              </article>
            ))}
          </div>
        </div>
      )}
    </section>
  )
}

import { useState } from 'react'

import type { AppLanguage, AttractionAdminSessionResponse } from '@/lib/mvp-types/index'

import { getPasswordValidationMessage } from '@/pages/shared/auth/passwordValidation'

// 景点管理员后台面板，负责注册、登录、景点创建和票种配置。
type AttractionAdminPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  attractionAdminSession: AttractionAdminSessionResponse | null
  translate: (translationKey: string) => string
  onRegisterAttractionManager: (payload: {
    email: string
    displayName: string
    password: string
  }) => Promise<void>
  onLoginAttractionManager: (payload: { email: string; password: string }) => Promise<void>
  onValidationError: (message: string) => void
  onReloadManagedAttractions: () => Promise<void>
  onCreateAttraction: (payload: {
    attractionName: string
    city: string
    location: string
    description: string
    attractionImageFile?: File | null
  }) => Promise<void>
  onCreateTicketType: (payload: {
    attractionId: string
    ticketTypeName: string
    description: string
    unitPrice: string
    currency: string
    availableFromDate: string
    availableToDate: string
    totalQuantity: number
    validWeekdays: string[]
  }) => Promise<void>
  onCreateSession: (payload: {
    attractionId: string
    ticketTypeId: string
    sessionName: string
    useDate: string
    startsAt: string
    endsAt: string
    capacity: number
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
  onValidationError,
  onReloadManagedAttractions,
  onCreateAttraction,
  onCreateTicketType,
  onCreateSession,
  onCreateRule,
  onLogoutAttractionManager,
}: AttractionAdminPanelProps) {
  const allWeekdayValues = ['MONDAY', 'TUESDAY', 'WEDNESDAY', 'THURSDAY', 'FRIDAY', 'SATURDAY', 'SUNDAY']
  const [selectedWeekdays, setSelectedWeekdays] = useState<string[]>(allWeekdayValues)

  function toggleWeekdaySelection(weekday: string) {
    setSelectedWeekdays(currentWeekdays =>
      currentWeekdays.includes(weekday) ? currentWeekdays.filter(currentWeekday => currentWeekday !== weekday) : [...currentWeekdays, weekday],
    )
  }

  function setAllWeekdays(enabled: boolean) {
    setSelectedWeekdays(enabled ? allWeekdayValues : [])
  }

  return (
    <section className="grid gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{translate('nav.attractionAdmin')}</p>
          <h2>{translate('attractionAdmin.title')}</h2>
        </div>
        {attractionAdminSession ? (
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onLogoutAttractionManager}>
            {translate('manager.logout')}
          </button>
        ) : null}
      </div>

      <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('attractionAdmin.description')}</p>

      {!attractionAdminSession ? (
        <div className="mx-auto grid max-w-5xl gap-4 md:grid-cols-2">
          <form
            className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
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
              await onRegisterAttractionManager({
                email,
                displayName: String(formData.get('displayName') ?? '').trim(),
                password,
              })
            }}
          >
            <h3>{translate('attractionAdmin.registerTitle')}</h3>
            <label>
              {translate('attractionAdmin.email')}
              <input name="email" type="email" autoComplete="off" required />
            </label>
            <label>
              {translate('attractionAdmin.displayName')}
              <input name="displayName" autoComplete="off" required />
            </label>
            <label>
              {translate('account.password')}
              <input name="password" type="password" autoComplete="off" required />
            </label>
            <label>
              {translate('account.confirmPassword')}
              <input name="confirmPassword" type="password" autoComplete="new-password" required />
            </label>
            <button className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white shadow-none transition hover:border-pink-600 hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
              {translate('attractionAdmin.createAccount')}
            </button>
          </form>

          <form
            className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
            autoComplete="off"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onLoginAttractionManager({
                email: String(formData.get('email') ?? '').trim(),
                password: String(formData.get('password') ?? ''),
              })
            }}
          >
            <h3>{translate('attractionAdmin.loginTitle')}</h3>
            <label>
              {translate('attractionAdmin.email')}
              <input name="email" type="email" autoComplete="off" required />
            </label>
            <label>
              {translate('account.password')}
              <input name="password" type="password" autoComplete="off" required />
            </label>
            <button className="inline-flex min-h-11 items-center justify-center border border-pink-500 bg-pink-500 px-4 py-2 text-sm font-semibold text-white shadow-none transition hover:border-pink-600 hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
              {translate('attractionAdmin.login')}
            </button>
          </form>
        </div>
      ) : (
        <div className="grid gap-4">
          <div className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
            <div className="text-lg font-bold text-slate-950">
              <div>
                <strong>{attractionAdminSession.displayName}</strong>
                <p>{attractionAdminSession.email}</p>
              </div>
              <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={() => void onReloadManagedAttractions()}>
                {translate('attractionAdmin.refresh')}
              </button>
            </div>
          </div>

          <div className="grid gap-4 md:grid-cols-2">
            <form
            className="grid gap-4 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
            autoComplete="off"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                const attractionImageEntry = formData.get('attractionImageFile')
                await onCreateAttraction({
                  attractionName: String(formData.get('attractionName') ?? '').trim(),
                  city: String(formData.get('city') ?? '').trim(),
                  location: String(formData.get('location') ?? '').trim(),
                  description: String(formData.get('description') ?? '').trim(),
                  attractionImageFile: attractionImageEntry instanceof File && attractionImageEntry.size > 0 ? attractionImageEntry : null,
                })
                event.currentTarget.reset()
              }}
            >
              <h3>{translate('attractionAdmin.createAttraction')}</h3>
              <label>
                {translate('attractionAdmin.attractionName')}
                <input name="attractionName" required />
              </label>
              <label>
                {translate('attractionAdmin.city')}
                <input name="city" required />
              </label>
              <label>
                {translate('attractionAdmin.location')}
                <input name="location" autoComplete="off" required />
              </label>
              <label>
                {translate('attractionAdmin.descriptionField')}
                <input name="description" required />
              </label>
              <label>
                {translate('attractionAdmin.attractionImage')}
                <input name="attractionImageFile" type="file" accept="image/png,image/jpeg,image/jpg,image/webp" />
                <span className="text-sm font-medium text-slate-500">{translate('attractionAdmin.attractionImageHint')}</span>
              </label>
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
                {translate('attractionAdmin.createAttraction')}
              </button>
            </form>

            <form
            className="grid gap-4 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
            autoComplete="off"
              onSubmit={async event => {
                event.preventDefault()
                const formData = new FormData(event.currentTarget)
                await onCreateTicketType({
                  attractionId: String(formData.get('attractionId') ?? '').trim(),
                  ticketTypeName: String(formData.get('ticketTypeName') ?? '').trim(),
                  description: String(formData.get('description') ?? '').trim(),
                  unitPrice: String(formData.get('unitPrice') ?? '').trim(),
                  currency: String(formData.get('currency') ?? '').trim(),
                  availableFromDate: String(formData.get('availableFromDate') ?? '').trim(),
                  availableToDate: String(formData.get('availableToDate') ?? '').trim(),
                  totalQuantity: Number(formData.get('totalQuantity') ?? 0),
                  validWeekdays: selectedWeekdays,
                })
                event.currentTarget.reset()
                setSelectedWeekdays(allWeekdayValues)
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
                <input name="ticketTypeName" required />
              </label>
              <label>
                {translate('attractionAdmin.descriptionField')}
                <input name="description" required />
              </label>
              <label>
                {translate('attractionAdmin.unitPrice')}
                <input name="unitPrice" type="number" min="0" step="0.01" required />
              </label>
              <label>
                {translate('attractionAdmin.currency')}
                <input name="currency" defaultValue="CNY" required />
              </label>
              <label>
                {translate('attractionAdmin.availableFromDate')}
                <input name="availableFromDate" type="date" defaultValue="2026-04-05" required />
              </label>
              <label>
                {translate('attractionAdmin.availableToDate')}
                <input name="availableToDate" type="date" defaultValue="2026-04-30" required />
              </label>
              <label>
                {translate('attractionAdmin.totalQuantity')}
                <input name="totalQuantity" type="number" min="1" step="1" defaultValue="100" required />
              </label>
              <div className="grid gap-2">
                <p className="text-sm font-medium text-slate-500">{translate('attractionAdmin.validWeekdays')}</p>
                <label className="flex items-center gap-2">
                  <input
                    type="checkbox"
                    checked={selectedWeekdays.length === allWeekdayValues.length}
                    onChange={event => setAllWeekdays(event.target.checked)}
                  />
                  {translate('attractionAdmin.allWeekdays')}
                </label>
                <div className="grid gap-4 md:grid-cols-3">
                  {allWeekdayValues.map(weekday => (
                    <label key={weekday} className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={selectedWeekdays.includes(weekday)}
                        onChange={() => toggleWeekdaySelection(weekday)}
                      />
                      {translate(`weekdays.${weekday.toLowerCase()}`)}
                    </label>
                  ))}
                </div>
              </div>
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
                {translate('attractionAdmin.createTicketType')}
              </button>
            </form>
          </div>

          <form
            className="grid gap-4 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
            autoComplete="off"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onCreateSession({
                attractionId: String(formData.get('attractionId') ?? '').trim(),
                ticketTypeId: String(formData.get('ticketTypeId') ?? '').trim(),
                sessionName: String(formData.get('sessionName') ?? '').trim(),
                useDate: String(formData.get('useDate') ?? '').trim(),
                startsAt: new Date(String(formData.get('startsAt') ?? '').trim()).toISOString(),
                endsAt: new Date(String(formData.get('endsAt') ?? '').trim()).toISOString(),
                capacity: Number(formData.get('capacity') ?? 0),
              })
              event.currentTarget.reset()
            }}
          >
            <h3>{translate('attractionAdmin.createSession')}</h3>
            <div className="grid gap-4 md:grid-cols-3">
              <label>
                {translate('attractionAdmin.attraction')}
                <select name="attractionId" required defaultValue="">
                  <option value="" disabled>{translate('attractionAdmin.attractionSelect')}</option>
                  {attractionAdminSession.managedAttractions.map(attraction => (
                    <option key={attraction.attractionId} value={attraction.attractionId}>{attraction.attractionName}</option>
                  ))}
                </select>
              </label>
              <label>
                {translate('attractionAdmin.ticketTypeId')}
                <input name="ticketTypeId" required />
              </label>
              <label>
                {translate('attractionAdmin.sessionName')}
                <input name="sessionName" required />
              </label>
              <label>
                {translate('attractionAdmin.useDate')}
                <input name="useDate" type="date" required />
              </label>
              <label>
                {translate('attractionAdmin.startsAt')}
                <input name="startsAt" type="datetime-local" required />
              </label>
              <label>
                {translate('attractionAdmin.endsAt')}
                <input name="endsAt" type="datetime-local" required />
              </label>
              <label>
                {translate('attractionAdmin.capacity')}
                <input name="capacity" type="number" min="1" step="1" defaultValue="50" required />
              </label>
            </div>
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>{translate('attractionAdmin.createSession')}</button>
          </form>

          <form
            className="grid gap-4 grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
            autoComplete="off"
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
            <div className="grid gap-4 md:grid-cols-3">
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
                <input name="ticketTypeId" required />
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
                <input name="ageValue" type="number" min="0" />
              </label>
              <label>
                {translate('attractionAdmin.minAge')}
                <input name="minAge" type="number" min="0" />
              </label>
              <label>
                {translate('attractionAdmin.maxAge')}
                <input name="maxAge" type="number" min="0" />
              </label>
              <label>
                {translate('attractionAdmin.documentType')}
                <input name="documentType" />
              </label>
              <label>
                {translate('attractionAdmin.documentNumberPrefix')}
                <input name="documentNumberPrefix" />
              </label>
            </div>
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy}>
              {translate('attractionAdmin.createRule')}
            </button>
          </form>

          <div className="grid gap-3">
            {attractionAdminSession.managedAttractions.map(attraction => (
              <article key={attraction.attractionId} className="grid gap-4 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50">
                <strong>{attraction.attractionName}</strong>
                <p>{`${attraction.city} | ${attraction.location}`}</p>
                <ul className="grid gap-3">
                  {attraction.ticketTypes.map(ticketType => (
                    <li key={ticketType.ticketTypeId}>
                      <div>
                        <strong>{`${ticketType.ticketTypeName} (${ticketType.ticketTypeId})`}</strong>
                        <p>{`${ticketType.priceAmount} ${ticketType.priceCurrency}`}</p>
                        <p>{`${translate('attractionAdmin.availableDateRange')}: ${ticketType.availableFromDate} - ${ticketType.availableToDate}`}</p>
                        <p>{`${translate('attractionAdmin.totalQuantity')}: ${ticketType.totalQuantity}`}</p>
                        <p>{`${translate('attractionAdmin.validWeekdays')}: ${ticketType.validWeekdays.map(weekday => translate(`weekdays.${weekday.toLowerCase()}`)).join(' / ')}`}</p>
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


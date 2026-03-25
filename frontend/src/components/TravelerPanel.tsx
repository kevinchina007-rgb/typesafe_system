import { useState } from 'react'

import type { AppLanguage, TravelerResponse } from '../lib/mvp-types'
import {
  deriveTravelerTypeLabelFromBirthDate,
  localizeDocumentType,
} from '../lib/view-models'

type TravelerPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onCreateTraveler: (payload: {
    fullName: string
    documentType: string
    documentNumber: string
    phone: string
    birthDate: string
    seatPreference: string
    mealPreference: string
    accessibilityRequestNotes: string | null
    emergencyContactName: string | null
    emergencyContactPhoneNumber: string | null
    isDefaultTraveler: boolean
  }) => Promise<void>
  onReloadTravelers: () => Promise<void>
}

export function TravelerPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onCreateTraveler,
  onReloadTravelers,
}: TravelerPanelProps) {
  const todayInputValue = new Date().toISOString().slice(0, 10)
  const [draftBirthDate, setDraftBirthDate] = useState('')

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.travelers')}</p>
          <h2>{translate('travelers.title')}</h2>
        </div>
        <button
          type="button"
          disabled={isGuestMode || isBusy}
          className="secondary-button"
          onClick={() => void onReloadTravelers()}
        >
          {translate('travelers.refresh')}
        </button>
      </div>

      <p className="hero-copy">{translate('travelers.description')}</p>

      <form
        className="stack-form panel-card"
        onSubmit={async event => {
          event.preventDefault()
          const formData = new FormData(event.currentTarget)
          const birthDate = String(formData.get('birthDate') ?? '')
          if (!birthDate || birthDate > todayInputValue) {
            return
          }

          await onCreateTraveler({
            fullName: String(formData.get('fullName') ?? ''),
            documentType: String(formData.get('documentType') ?? 'passport'),
            documentNumber: String(formData.get('documentNumber') ?? ''),
            phone: String(formData.get('phone') ?? ''),
            birthDate,
            seatPreference: String(formData.get('seatPreference') ?? 'none'),
            mealPreference: String(formData.get('mealPreference') ?? 'standard'),
            accessibilityRequestNotes:
              String(formData.get('accessibilityRequestNotes') ?? '').trim() || null,
            emergencyContactName:
              String(formData.get('emergencyContactName') ?? '').trim() || null,
            emergencyContactPhoneNumber:
              String(formData.get('emergencyContactPhoneNumber') ?? '').trim() || null,
            isDefaultTraveler: formData.get('isDefaultTraveler') === 'on',
          })
          setDraftBirthDate('')
          event.currentTarget.reset()
        }}
      >
        <div className="three-column-grid">
          <label>
            {translate('travelers.fullName')}
            <input name="fullName" placeholder="Lin Chen" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('travelers.documentType')}
            <select name="documentType" defaultValue="passport" disabled={isGuestMode || isBusy}>
              <option value="passport">{translate('travelers.document.passport')}</option>
              <option value="identity-card">{translate('travelers.document.identity-card')}</option>
              <option value="residence-permit">{translate('travelers.document.residence-permit')}</option>
              <option value="other">{translate('travelers.document.other')}</option>
            </select>
          </label>
          <label>
            {translate('travelers.documentNumber')}
            <input name="documentNumber" placeholder="E12345678" required disabled={isGuestMode || isBusy} />
          </label>
        </div>

        <div className="three-column-grid">
          <label>
            {translate('travelers.phone')}
            <input name="phone" placeholder="+8613812345678" required disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('travelers.birthDate')}
            <input
              name="birthDate"
              type="date"
              max={todayInputValue}
              required
              disabled={isGuestMode || isBusy}
              value={draftBirthDate}
              onChange={event => setDraftBirthDate(event.target.value)}
            />
          </label>
          <label>
            {translate('travelers.derivedType')}
            <input value={deriveTravelerTypeLabelFromBirthDate(draftBirthDate, currentLanguage)} readOnly disabled />
          </label>
        </div>

        <div className="two-column-grid">
          <label>
            {translate('travelers.seatPreference')}
            <select name="seatPreference" defaultValue="none" disabled={isGuestMode || isBusy}>
              <option value="window">{translate('travelers.seat.window')}</option>
              <option value="aisle">{translate('travelers.seat.aisle')}</option>
              <option value="middle">{translate('travelers.seat.middle')}</option>
              <option value="none">{translate('travelers.seat.none')}</option>
            </select>
          </label>
          <label>
            {translate('travelers.mealPreference')}
            <select name="mealPreference" defaultValue="standard" disabled={isGuestMode || isBusy}>
              <option value="standard">{translate('travelers.meal.standard')}</option>
              <option value="vegetarian">{translate('travelers.meal.vegetarian')}</option>
              <option value="vegan">{translate('travelers.meal.vegan')}</option>
              <option value="halal">{translate('travelers.meal.halal')}</option>
            </select>
          </label>
        </div>

        <label>
          {translate('travelers.accessibility')}
          <input name="accessibilityRequestNotes" disabled={isGuestMode || isBusy} />
        </label>

        <div className="two-column-grid">
          <label>
            {translate('travelers.emergencyName')}
            <input name="emergencyContactName" disabled={isGuestMode || isBusy} />
          </label>
          <label>
            {translate('travelers.emergencyPhone')}
            <input name="emergencyContactPhoneNumber" disabled={isGuestMode || isBusy} />
          </label>
        </div>

        <label className="checkbox-row">
          <input type="checkbox" name="isDefaultTraveler" disabled={isGuestMode || isBusy} />
          {translate('travelers.primaryToggle')}
        </label>

        <button type="submit" disabled={isGuestMode || isBusy}>
          {translate('travelers.add')}
        </button>
      </form>

      <div className="list-surface">
        {isGuestMode ? <p className="empty-state">{translate('travelers.guest')}</p> : null}
        {travelers.length > 0 ? (
          <ul className="entity-list">
            {travelers.map(traveler => (
              <li key={traveler.travelerId}>
                <div>
                  <strong>{traveler.fullName}</strong>
                  <p>{`${localizeDocumentType(traveler.documentType, currentLanguage)} · ${traveler.documentNumber}`}</p>
                  <p>{deriveTravelerTypeLabelFromBirthDate(traveler.birthDate, currentLanguage)}</p>
                </div>
                <span className="tag-chip">
                  {traveler.isDefault
                    ? currentLanguage === 'zh'
                      ? '默认出行人'
                      : 'Primary traveler'
                    : deriveTravelerTypeLabelFromBirthDate(traveler.birthDate, currentLanguage)}
                </span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="empty-state">{translate('travelers.empty')}</p>
        )}
      </div>
    </section>
  )
}

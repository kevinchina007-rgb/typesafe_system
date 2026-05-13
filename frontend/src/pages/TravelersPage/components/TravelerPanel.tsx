import { useMemo, useState } from 'react'

import type { AppLanguage, TravelerResponse } from '@/lib/mvp-types/index'
import { deriveTravelerTypeLabelFromBirthDate, localizeDocumentType } from '@/lib/presenters/view-models'

type TravelerFormDraft = {
  travelerId: string | null
  fullName: string
  documentType: string
  documentNumber: string
  phone: string
  birthDate: string
  seatPreference: string
  mealPreference: string
  accessibilityRequestNotes: string
  emergencyContactName: string
  emergencyContactPhoneNumber: string
  isDefaultTraveler: boolean
}

type CreateTravelerPayload = {
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
}

type TravelerPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isGuestMode: boolean
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onCreateTraveler: (payload: CreateTravelerPayload) => Promise<void>
  onUpdateTraveler: (payload: TravelerFormDraft) => Promise<void>
  onDeleteTraveler: (travelerId: string) => Promise<void>
  onReloadTravelers: () => Promise<void>
}

const emptyTravelerFormDraft: TravelerFormDraft = {
  travelerId: null,
  fullName: '',
  documentType: 'passport',
  documentNumber: '',
  phone: '',
  birthDate: '',
  seatPreference: 'none',
  mealPreference: 'standard',
  accessibilityRequestNotes: '',
  emergencyContactName: '',
  emergencyContactPhoneNumber: '',
  isDefaultTraveler: false,
}

function createTravelerFormDraft(traveler: TravelerResponse): TravelerFormDraft {
  return {
    travelerId: traveler.travelerId,
    fullName: traveler.fullName,
    documentType: traveler.documentType,
    documentNumber: traveler.documentNumber,
    phone: traveler.phone,
    birthDate: traveler.birthDate,
    seatPreference: 'none',
    mealPreference: 'standard',
    accessibilityRequestNotes: '',
    emergencyContactName: '',
    emergencyContactPhoneNumber: '',
    isDefaultTraveler: traveler.isDefault,
  }
}

function renderTravelerLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

export function TravelerPanel({
  currentLanguage,
  isBusy,
  isGuestMode,
  travelers,
  translate,
  onCreateTraveler,
  onUpdateTraveler,
  onDeleteTraveler,
  onReloadTravelers,
}: TravelerPanelProps) {
  const todayInputValue = new Date().toISOString().slice(0, 10)
  const placeholderTexts = {
    fullName: currentLanguage === 'zh' ? '林晨' : 'Lin Chen',
    documentNumber: currentLanguage === 'zh' ? 'E12345678' : 'E12345678',
    phone: currentLanguage === 'zh' ? '+8613812345678' : '+8613812345678',
  }
  const [travelerFormDraft, setTravelerFormDraft] = useState<TravelerFormDraft>(emptyTravelerFormDraft)

  const derivedTravelerTypeLabel = useMemo(
    () => deriveTravelerTypeLabelFromBirthDate(travelerFormDraft.birthDate, currentLanguage),
    [travelerFormDraft.birthDate, currentLanguage],
  )

  const isEditingTraveler = travelerFormDraft.travelerId !== null

  function updateTravelerFormDraft<K extends keyof TravelerFormDraft>(
    key: K,
    value: TravelerFormDraft[K],
  ) {
    setTravelerFormDraft(currentTravelerFormDraft => ({
      ...currentTravelerFormDraft,
      [key]: value,
    }))
  }

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
          if (!travelerFormDraft.birthDate || travelerFormDraft.birthDate > todayInputValue) {
            return
          }

          if (travelerFormDraft.travelerId) {
            await onUpdateTraveler(travelerFormDraft)
          } else {
            await onCreateTraveler({
              fullName: travelerFormDraft.fullName,
              documentType: travelerFormDraft.documentType,
              documentNumber: travelerFormDraft.documentNumber,
              phone: travelerFormDraft.phone,
              birthDate: travelerFormDraft.birthDate,
              seatPreference: travelerFormDraft.seatPreference,
              mealPreference: travelerFormDraft.mealPreference,
              accessibilityRequestNotes: travelerFormDraft.accessibilityRequestNotes.trim() || null,
              emergencyContactName: travelerFormDraft.emergencyContactName.trim() || null,
              emergencyContactPhoneNumber: travelerFormDraft.emergencyContactPhoneNumber.trim() || null,
              isDefaultTraveler: travelerFormDraft.isDefaultTraveler,
            })
          }

          setTravelerFormDraft(emptyTravelerFormDraft)
        }}
      >
        <div className="panel-heading">
          <h3>{isEditingTraveler ? translate('travelers.edit') : translate('travelers.add')}</h3>
          {isEditingTraveler ? (
            <button type="button" className="secondary-button" onClick={() => setTravelerFormDraft(emptyTravelerFormDraft)}>
              {translate('travelers.cancelEdit')}
            </button>
          ) : null}
        </div>

        <div className="three-column-grid">
          <label>
            {translate('travelers.fullName')}
            <input
              value={travelerFormDraft.fullName}
              onChange={event => updateTravelerFormDraft('fullName', event.target.value)}
              placeholder={placeholderTexts.fullName}
              required
              disabled={isGuestMode || isBusy}
            />
          </label>
          <label>
            {translate('travelers.documentType')}
            <select
              value={travelerFormDraft.documentType}
              onChange={event => updateTravelerFormDraft('documentType', event.target.value)}
              disabled={isGuestMode || isBusy}
            >
              <option value="passport">{translate('travelers.document.passport')}</option>
              <option value="identity-card">{translate('travelers.document.identity-card')}</option>
              <option value="residence-permit">{translate('travelers.document.residence-permit')}</option>
              <option value="other">{translate('travelers.document.other')}</option>
            </select>
          </label>
          <label>
            {translate('travelers.documentNumber')}
            <input
              value={travelerFormDraft.documentNumber}
              onChange={event => updateTravelerFormDraft('documentNumber', event.target.value)}
              placeholder={placeholderTexts.documentNumber}
              required
              disabled={isGuestMode || isBusy}
            />
          </label>
        </div>

        <div className="three-column-grid">
          <label>
            {translate('travelers.phone')}
            <input
              value={travelerFormDraft.phone}
              onChange={event => updateTravelerFormDraft('phone', event.target.value)}
              placeholder={placeholderTexts.phone}
              required
              disabled={isGuestMode || isBusy}
            />
          </label>
          <label>
            {translate('travelers.birthDate')}
            <input
              type="date"
              max={todayInputValue}
              required
              disabled={isGuestMode || isBusy}
              value={travelerFormDraft.birthDate}
              onChange={event => updateTravelerFormDraft('birthDate', event.target.value)}
            />
          </label>
          <label>
            {translate('travelers.derivedType')}
            <input value={derivedTravelerTypeLabel} readOnly disabled />
          </label>
        </div>

        <div className="two-column-grid">
          <label>
            {translate('travelers.seatPreference')}
            <select
              value={travelerFormDraft.seatPreference}
              onChange={event => updateTravelerFormDraft('seatPreference', event.target.value)}
              disabled={isGuestMode || isBusy}
            >
              <option value="window">{translate('travelers.seat.window')}</option>
              <option value="aisle">{translate('travelers.seat.aisle')}</option>
              <option value="middle">{translate('travelers.seat.middle')}</option>
              <option value="none">{translate('travelers.seat.none')}</option>
            </select>
          </label>
          <label>
            {translate('travelers.mealPreference')}
            <select
              value={travelerFormDraft.mealPreference}
              onChange={event => updateTravelerFormDraft('mealPreference', event.target.value)}
              disabled={isGuestMode || isBusy}
            >
              <option value="standard">{translate('travelers.meal.standard')}</option>
              <option value="vegetarian">{translate('travelers.meal.vegetarian')}</option>
              <option value="vegan">{translate('travelers.meal.vegan')}</option>
              <option value="halal">{translate('travelers.meal.halal')}</option>
            </select>
          </label>
        </div>

        <label>
          {translate('travelers.accessibility')}
          <input
            value={travelerFormDraft.accessibilityRequestNotes}
            onChange={event => updateTravelerFormDraft('accessibilityRequestNotes', event.target.value)}
            disabled={isGuestMode || isBusy}
          />
        </label>

        <div className="two-column-grid">
          <label>
            {translate('travelers.emergencyName')}
            <input
              value={travelerFormDraft.emergencyContactName}
              onChange={event => updateTravelerFormDraft('emergencyContactName', event.target.value)}
              disabled={isGuestMode || isBusy}
            />
          </label>
          <label>
            {translate('travelers.emergencyPhone')}
            <input
              value={travelerFormDraft.emergencyContactPhoneNumber}
              onChange={event => updateTravelerFormDraft('emergencyContactPhoneNumber', event.target.value)}
              disabled={isGuestMode || isBusy}
            />
          </label>
        </div>

        {!isEditingTraveler ? (
          <label className="checkbox-row">
            <input
              type="checkbox"
              checked={travelerFormDraft.isDefaultTraveler}
              onChange={event => updateTravelerFormDraft('isDefaultTraveler', event.target.checked)}
              disabled={isGuestMode || isBusy}
            />
            {translate('travelers.primaryToggle')}
          </label>
        ) : null}

        <button type="submit" disabled={isGuestMode || isBusy}>
          {isEditingTraveler ? translate('travelers.saveEdit') : translate('travelers.add')}
        </button>
      </form>

      <div className="list-surface">
        {isGuestMode ? <p className="empty-state">{translate('travelers.guest')}</p> : null}
        {travelers.length > 0 ? (
          <ul className="entity-list">
            {travelers.map(traveler => (
              <li key={traveler.travelerId}>
                <div>
                  <strong>{renderTravelerLabel(traveler)}</strong>
                  <p>{`${localizeDocumentType(traveler.documentType, currentLanguage)} · ${traveler.documentNumber}`}</p>
                  <p>{deriveTravelerTypeLabelFromBirthDate(traveler.birthDate, currentLanguage)}</p>
                </div>
                <div className="compact-action-block">
                  <span className="tag-chip">
                    {traveler.isDefault
                      ? translate('travelers.primary')
                      : deriveTravelerTypeLabelFromBirthDate(traveler.birthDate, currentLanguage)}
                  </span>
                  {!isGuestMode ? (
                    <>
                      <button type="button" className="secondary-button" onClick={() => setTravelerFormDraft(createTravelerFormDraft(traveler))}>
                        {translate('travelers.edit')}
                      </button>
                      <button type="button" className="secondary-button" disabled={isBusy} onClick={() => void onDeleteTraveler(traveler.travelerId)}>
                        {translate('travelers.delete')}
                      </button>
                    </>
                  ) : null}
                </div>
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


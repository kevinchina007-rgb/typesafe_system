import { useMemo, useState, type ReactNode } from 'react'

import type { AppLanguage, TravelerResponse } from '@/lib/mvp-types/index'
import { deriveTravelerTypeLabelFromBirthDate, localizeDocumentType } from '@/lib/presenters/view-models'
import type { TravelerProfileInput } from '@/microservices/traveler/objects/TravelerProfileInput'

type TravelerFormDraft = {
  travelerId: string | null
  fullName: string
  gender: string
  nationality: string
  documentType: string
  documentNumber: string
  documentExpiryDate: string
  phone: string
  email: string
  birthDate: string
  seatPreference: string
  mealPreference: string
  quietSeatPreferred: boolean
  assistanceType: string
  requirementNote: string
  hasLargeLuggage: boolean
  luggageNote: string
  emergencyContactName: string
  emergencyContactPhoneNumber: string
  isDefaultTraveler: boolean
}

type CreateTravelerPayload = TravelerProfileInput

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
  gender: '未填写',
  nationality: '中国',
  documentType: 'passport',
  documentNumber: '',
  documentExpiryDate: '',
  phone: '',
  email: '',
  birthDate: '',
  seatPreference: 'none',
  mealPreference: 'standard',
  quietSeatPreferred: false,
  assistanceType: '无',
  requirementNote: '',
  hasLargeLuggage: false,
  luggageNote: '',
  emergencyContactName: '',
  emergencyContactPhoneNumber: '',
  isDefaultTraveler: false,
}

const fieldClassName = 'h-12 w-full border-2 border-slate-300 bg-white px-3 text-base text-slate-950 outline-none focus:border-pink-500'
const textareaClassName = 'min-h-24 w-full border-2 border-slate-300 bg-white px-3 py-2 text-base text-slate-950 outline-none focus:border-pink-500'
const buttonClassName = 'inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55'

function createTravelerFormDraft(traveler: TravelerResponse): TravelerFormDraft {
  return {
    travelerId: traveler.travelerId,
    fullName: traveler.basicInfo?.fullName ?? traveler.fullName,
    gender: traveler.basicInfo?.gender ?? '未填写',
    nationality: traveler.basicInfo?.nationality ?? '中国',
    documentType: traveler.documentInfo?.documentType ?? traveler.documentType,
    documentNumber: traveler.documentInfo?.documentNumber ?? traveler.documentNumber,
    documentExpiryDate: traveler.documentInfo?.documentExpiryDate ?? '',
    phone: traveler.contactInfo?.phone ?? traveler.phone,
    email: traveler.contactInfo?.email ?? '',
    birthDate: traveler.basicInfo?.birthDate ?? traveler.birthDate,
    seatPreference: traveler.preferenceInfo?.seatPreference ?? 'none',
    mealPreference: traveler.preferenceInfo?.mealPreference ?? 'standard',
    quietSeatPreferred: traveler.preferenceInfo?.quietSeatPreferred ?? false,
    assistanceType: traveler.specialRequirementInfo?.assistanceType ?? '无',
    requirementNote: traveler.specialRequirementInfo?.requirementNote ?? '',
    hasLargeLuggage: traveler.specialRequirementInfo?.hasLargeLuggage ?? false,
    luggageNote: traveler.specialRequirementInfo?.luggageNote ?? '',
    emergencyContactName: '',
    emergencyContactPhoneNumber: '',
    isDefaultTraveler: traveler.isDefault,
  }
}

function buildTravelerPayload(draft: TravelerFormDraft): TravelerProfileInput {
  const requirementNote = draft.requirementNote.trim() || null
  const luggageNote = draft.luggageNote.trim() || null
  const email = draft.email.trim() || null
  return {
    fullName: draft.fullName.trim(),
    documentType: draft.documentType,
    documentNumber: draft.documentNumber.trim(),
    phone: draft.phone.trim(),
    birthDate: draft.birthDate,
    seatPreference: draft.seatPreference,
    mealPreference: draft.mealPreference,
    accessibilityRequestNotes: requirementNote,
    emergencyContactName: draft.emergencyContactName.trim() || null,
    emergencyContactPhoneNumber: draft.emergencyContactPhoneNumber.trim() || null,
    isDefaultTraveler: draft.isDefaultTraveler,
    basicInfo: {
      fullName: draft.fullName.trim(),
      gender: draft.gender,
      birthDate: draft.birthDate,
      nationality: draft.nationality.trim() || '中国',
    },
    documentInfo: {
      documentType: draft.documentType,
      documentNumber: draft.documentNumber.trim(),
      documentExpiryDate: draft.documentExpiryDate || null,
    },
    contactInfo: {
      phone: draft.phone.trim(),
      email,
    },
    preferenceInfo: {
      seatPreference: draft.seatPreference,
      mealPreference: draft.mealPreference,
      quietSeatPreferred: draft.quietSeatPreferred,
    },
    specialRequirementInfo: {
      assistanceType: draft.assistanceType,
      requirementNote,
      hasLargeLuggage: draft.hasLargeLuggage,
      luggageNote,
    },
  }
}

function renderTravelerLabel(traveler: TravelerResponse): string {
  return `${traveler.fullName} (${traveler.documentNumber.slice(-4)})`
}

function labelForPreference(value: string): string {
  const labels: Record<string, string> = {
    Window: '靠窗',
    Aisle: '靠过道',
    Middle: '中间座',
    NoPreference: '无要求',
    window: '靠窗',
    aisle: '靠过道',
    middle: '中间座',
    none: '无要求',
  }
  return labels[value] ?? value
}

function labelForMeal(value: string): string {
  const labels: Record<string, string> = {
    Standard: '标准餐',
    Vegetarian: '素食餐',
    Vegan: '纯素餐',
    Halal: '清真餐',
    ChildMeal: '儿童餐',
    NoPreference: '无要求',
    standard: '标准餐',
    vegetarian: '素食餐',
    vegan: '纯素餐',
    halal: '清真餐',
  }
  return labels[value] ?? value
}

function FormField({ label, children }: { label: string; children: ReactNode }) {
  return (
    <label className="grid gap-2 text-base font-bold text-slate-700">
      {label}
      {children}
    </label>
  )
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
  const [travelerFormDraft, setTravelerFormDraft] = useState<TravelerFormDraft>(emptyTravelerFormDraft)

  const derivedTravelerTypeLabel = useMemo(
    () => deriveTravelerTypeLabelFromBirthDate(travelerFormDraft.birthDate, currentLanguage),
    [travelerFormDraft.birthDate, currentLanguage],
  )

  const isEditingTraveler = travelerFormDraft.travelerId !== null

  function updateTravelerFormDraft<K extends keyof TravelerFormDraft>(key: K, value: TravelerFormDraft[K]) {
    setTravelerFormDraft(currentTravelerFormDraft => ({
      ...currentTravelerFormDraft,
      [key]: value,
    }))
  }

  return (
    <section className="mx-auto grid w-full max-w-6xl gap-5 border-y border-slate-200 bg-white p-6 text-slate-950 shadow-sm shadow-slate-200/40">
      <div className="flex flex-wrap items-center justify-between gap-4">
        <div>
          <h2 className="m-0 text-3xl font-black text-slate-950">{translate('travelers.title')}</h2>
        </div>
        <button type="button" disabled={isGuestMode || isBusy} className={buttonClassName} onClick={() => void onReloadTravelers()}>
          {translate('travelers.refresh')}
        </button>
      </div>

      <form
        className="grid gap-6 border border-slate-200 bg-white p-5 text-slate-950 shadow-sm shadow-slate-200/50"
        onSubmit={async event => {
          event.preventDefault()
          if (!travelerFormDraft.birthDate || travelerFormDraft.birthDate > todayInputValue) {
            return
          }

          if (travelerFormDraft.travelerId) {
            await onUpdateTraveler(travelerFormDraft)
          } else {
            await onCreateTraveler(buildTravelerPayload(travelerFormDraft))
          }

          setTravelerFormDraft(emptyTravelerFormDraft)
        }}
      >
        <div className="flex flex-wrap items-center justify-between gap-3">
          <h3 className="m-0 text-2xl font-black text-slate-950">{isEditingTraveler ? translate('travelers.edit') : translate('travelers.add')}</h3>
          {isEditingTraveler ? (
            <button type="button" className={buttonClassName} onClick={() => setTravelerFormDraft(emptyTravelerFormDraft)}>
              {translate('travelers.cancelEdit')}
            </button>
          ) : null}
        </div>

        <section className="grid gap-4">
          <h4 className="m-0 text-xl font-black text-slate-950">基础信息</h4>
          <div className="grid gap-4 md:grid-cols-4">
            <FormField label="姓名">
              <input className={fieldClassName} value={travelerFormDraft.fullName} onChange={event => updateTravelerFormDraft('fullName', event.target.value)} required disabled={isGuestMode || isBusy} />
            </FormField>
            <FormField label="性别">
              <select className={fieldClassName} value={travelerFormDraft.gender} onChange={event => updateTravelerFormDraft('gender', event.target.value)} disabled={isGuestMode || isBusy}>
                <option value="未填写">未填写</option>
                <option value="男">男</option>
                <option value="女">女</option>
                <option value="其他">其他</option>
              </select>
            </FormField>
            <FormField label="出生日期">
              <input className={fieldClassName} type="date" max={todayInputValue} required disabled={isGuestMode || isBusy} value={travelerFormDraft.birthDate} onChange={event => updateTravelerFormDraft('birthDate', event.target.value)} />
            </FormField>
            <FormField label="旅客类型">
              <input className={fieldClassName} value={derivedTravelerTypeLabel} readOnly disabled />
            </FormField>
          </div>
          <FormField label="国籍">
            <input className={fieldClassName} value={travelerFormDraft.nationality} onChange={event => updateTravelerFormDraft('nationality', event.target.value)} disabled={isGuestMode || isBusy} />
          </FormField>
        </section>

        <section className="grid gap-4">
          <h4 className="m-0 text-xl font-black text-slate-950">证件信息</h4>
          <div className="grid gap-4 md:grid-cols-3">
            <FormField label="证件类型">
              <select className={fieldClassName} value={travelerFormDraft.documentType} onChange={event => updateTravelerFormDraft('documentType', event.target.value)} disabled={isGuestMode || isBusy}>
                <option value="passport">{translate('travelers.document.passport')}</option>
                <option value="identity-card">{translate('travelers.document.identity-card')}</option>
                <option value="residence-permit">{translate('travelers.document.residence-permit')}</option>
                <option value="other">{translate('travelers.document.other')}</option>
              </select>
            </FormField>
            <FormField label="证件号码">
              <input className={fieldClassName} value={travelerFormDraft.documentNumber} onChange={event => updateTravelerFormDraft('documentNumber', event.target.value)} required disabled={isGuestMode || isBusy} />
            </FormField>
            <FormField label="证件有效期">
              <input className={fieldClassName} type="date" value={travelerFormDraft.documentExpiryDate} onChange={event => updateTravelerFormDraft('documentExpiryDate', event.target.value)} disabled={isGuestMode || isBusy} />
            </FormField>
          </div>
        </section>

        <section className="grid gap-4">
          <h4 className="m-0 text-xl font-black text-slate-950">联系信息</h4>
          <div className="grid gap-4 md:grid-cols-2">
            <FormField label="手机号">
              <input className={fieldClassName} value={travelerFormDraft.phone} onChange={event => updateTravelerFormDraft('phone', event.target.value)} required disabled={isGuestMode || isBusy} />
            </FormField>
            <FormField label="邮箱">
              <input className={fieldClassName} type="email" value={travelerFormDraft.email} onChange={event => updateTravelerFormDraft('email', event.target.value)} disabled={isGuestMode || isBusy} />
            </FormField>
          </div>
        </section>

        <section className="grid gap-4">
          <h4 className="m-0 text-xl font-black text-slate-950">乘机偏好</h4>
          <div className="grid gap-4 md:grid-cols-3">
            <FormField label="座位偏好">
              <select className={fieldClassName} value={travelerFormDraft.seatPreference} onChange={event => updateTravelerFormDraft('seatPreference', event.target.value)} disabled={isGuestMode || isBusy}>
                <option value="window">{translate('travelers.seat.window')}</option>
                <option value="aisle">{translate('travelers.seat.aisle')}</option>
                <option value="middle">{translate('travelers.seat.middle')}</option>
                <option value="none">{translate('travelers.seat.none')}</option>
              </select>
            </FormField>
            <FormField label="餐食偏好">
              <select className={fieldClassName} value={travelerFormDraft.mealPreference} onChange={event => updateTravelerFormDraft('mealPreference', event.target.value)} disabled={isGuestMode || isBusy}>
                <option value="standard">{translate('travelers.meal.standard')}</option>
                <option value="vegetarian">{translate('travelers.meal.vegetarian')}</option>
                <option value="vegan">{translate('travelers.meal.vegan')}</option>
                <option value="halal">{translate('travelers.meal.halal')}</option>
              </select>
            </FormField>
            <label className="flex min-h-12 items-center gap-3 self-end border-2 border-slate-300 px-3 text-base font-bold text-slate-700">
              <input type="checkbox" checked={travelerFormDraft.quietSeatPreferred} onChange={event => updateTravelerFormDraft('quietSeatPreferred', event.target.checked)} disabled={isGuestMode || isBusy} />
              希望安静座位
            </label>
          </div>
        </section>

        <section className="grid gap-4">
          <h4 className="m-0 text-xl font-black text-slate-950">特殊要求</h4>
          <div className="grid gap-4 md:grid-cols-2">
            <FormField label="协助类型">
              <select className={fieldClassName} value={travelerFormDraft.assistanceType} onChange={event => updateTravelerFormDraft('assistanceType', event.target.value)} disabled={isGuestMode || isBusy}>
                <option value="无">无</option>
                <option value="老人协助">老人协助</option>
                <option value="儿童协助">儿童协助</option>
                <option value="轮椅协助">轮椅协助</option>
                <option value="孕妇协助">孕妇协助</option>
                <option value="其他">其他</option>
              </select>
            </FormField>
            <label className="flex min-h-12 items-center gap-3 self-end border-2 border-slate-300 px-3 text-base font-bold text-slate-700">
              <input type="checkbox" checked={travelerFormDraft.hasLargeLuggage} onChange={event => updateTravelerFormDraft('hasLargeLuggage', event.target.checked)} disabled={isGuestMode || isBusy} />
              携带大件行李
            </label>
          </div>
          <div className="grid gap-4 md:grid-cols-2">
            <FormField label="特殊要求说明">
              <textarea className={textareaClassName} value={travelerFormDraft.requirementNote} onChange={event => updateTravelerFormDraft('requirementNote', event.target.value)} disabled={isGuestMode || isBusy} />
            </FormField>
            <FormField label="行李说明">
              <textarea className={textareaClassName} value={travelerFormDraft.luggageNote} onChange={event => updateTravelerFormDraft('luggageNote', event.target.value)} disabled={isGuestMode || isBusy} />
            </FormField>
          </div>
        </section>

        <section className="grid gap-4">
          <h4 className="m-0 text-lg font-black text-slate-950">紧急联系人</h4>
          <div className="grid gap-4 md:grid-cols-2">
            <FormField label={translate('travelers.emergencyName')}>
              <input className={fieldClassName} value={travelerFormDraft.emergencyContactName} onChange={event => updateTravelerFormDraft('emergencyContactName', event.target.value)} disabled={isGuestMode || isBusy} />
            </FormField>
            <FormField label={translate('travelers.emergencyPhone')}>
              <input className={fieldClassName} value={travelerFormDraft.emergencyContactPhoneNumber} onChange={event => updateTravelerFormDraft('emergencyContactPhoneNumber', event.target.value)} disabled={isGuestMode || isBusy} />
            </FormField>
          </div>
        </section>

        {!isEditingTraveler ? (
          <label className="flex items-center gap-2 text-base font-bold text-slate-700">
            <input type="checkbox" checked={travelerFormDraft.isDefaultTraveler} onChange={event => updateTravelerFormDraft('isDefaultTraveler', event.target.checked)} disabled={isGuestMode || isBusy} />
            {translate('travelers.primaryToggle')}
          </label>
        ) : null}

        <button className="inline-flex min-h-12 w-fit items-center justify-center bg-pink-500 px-8 py-3 text-base font-bold text-white transition hover:bg-pink-600 disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isGuestMode || isBusy}>
          {isEditingTraveler ? translate('travelers.saveEdit') : translate('travelers.add')}
        </button>
      </form>

      <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
        {isGuestMode ? <p className="text-sm leading-6 text-slate-500">{translate('travelers.guest')}</p> : null}
        {travelers.length > 0 ? (
          <ul className="grid gap-3">
            {travelers.map(traveler => (
              <li key={traveler.travelerId} className="grid gap-3 border border-slate-200 p-4 md:grid-cols-[minmax(0,1fr)_auto]">
                <div>
                  <strong className="text-xl font-black text-slate-950">{renderTravelerLabel(traveler)}</strong>
                  <p className="m-0 text-sm text-slate-600">{`${localizeDocumentType(traveler.documentType, currentLanguage)} ${traveler.documentNumber}`}</p>
                  <p className="m-0 text-sm text-slate-600">{`${deriveTravelerTypeLabelFromBirthDate(traveler.birthDate, currentLanguage)} / ${traveler.basicInfo?.gender ?? '未填写'} / ${traveler.basicInfo?.nationality ?? '中国'}`}</p>
                  <p className="m-0 text-sm text-slate-600">{`${labelForPreference(traveler.preferenceInfo?.seatPreference ?? 'none')} / ${labelForMeal(traveler.preferenceInfo?.mealPreference ?? 'standard')}`}</p>
                </div>
                <div className="flex flex-wrap items-center gap-3">
                  <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">
                    {traveler.isDefault ? translate('travelers.primary') : deriveTravelerTypeLabelFromBirthDate(traveler.birthDate, currentLanguage)}
                  </span>
                  {!isGuestMode ? (
                    <>
                      <button type="button" className={buttonClassName} onClick={() => setTravelerFormDraft(createTravelerFormDraft(traveler))}>
                        {translate('travelers.edit')}
                      </button>
                      <button type="button" className={buttonClassName} disabled={isBusy} onClick={() => void onDeleteTraveler(traveler.travelerId)}>
                        {translate('travelers.delete')}
                      </button>
                    </>
                  ) : null}
                </div>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-sm leading-6 text-slate-500">{translate('travelers.empty')}</p>
        )}
      </div>
    </section>
  )
}

import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse, TravelerResponse } from '@/lib/mvp-types/index'
import { formatTravelerChipLabel } from '@/lib/presenters/tour-group-presenter'
import { localizeTourGroupItemType } from '@/lib/presenters/view-models'

type TourGroupSelectionDialogProps = {
  currentLanguage: AppLanguage
  isOpen: boolean
  isBusy: boolean
  planItem: GroupPlanItemResponse | null
  options: GroupPlanOptionResponse[]
  availableTravelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onClose: () => void
  onSaveDraft: (payload: { optionId: string; quantity: number; travelerIds: string[] }) => Promise<void>
  onSaveAndSubmit: (payload: { optionId: string; quantity: number; travelerIds: string[] }) => Promise<void>
}

export function TourGroupSelectionDialog({
  currentLanguage,
  isOpen,
  isBusy,
  planItem,
  options,
  availableTravelers,
  translate,
  onClose,
  onSaveDraft,
  onSaveAndSubmit,
}: TourGroupSelectionDialogProps) {
  const [selectedOptionId, setSelectedOptionId] = useState('')
  const [quantity, setQuantity] = useState(1)
  const [selectedTravelerIds, setSelectedTravelerIds] = useState<string[]>([])

  useEffect(() => {
    if (!isOpen) {
      setSelectedOptionId('')
      setQuantity(1)
      setSelectedTravelerIds([])
    }
  }, [isOpen])

  const selectedOption = useMemo(
    () => options.find(option => option.optionId === selectedOptionId) ?? null,
    [options, selectedOptionId],
  )

  if (!isOpen || !planItem) {
    return null
  }

  function toggleTraveler(travelerId: string) {
    setSelectedTravelerIds(currentIds =>
      currentIds.includes(travelerId) ? currentIds.filter(currentId => currentId !== travelerId) : [...currentIds, travelerId],
    )
  }

  return (
    <div className="fixed inset-0 z-50 grid place-items-center bg-slate-950/35 p-6" role="presentation">
      <div className="grid max-h-[90vh] w-full max-w-3xl gap-4 overflow-auto border border-slate-200 bg-white p-6 text-slate-950 shadow-2xl shadow-slate-950/20" role="dialog" aria-modal="true" aria-label={translate('tourGroups.createSelection')}>
        <div className="text-lg font-bold text-slate-950">
          <div>
            <p className="text-sm font-bold text-slate-500">{translate('tourGroups.selectionDialogEyebrow')}</p>
            <h3>{planItem.title}</h3>
            <p>{localizeTourGroupItemType(planItem.itemType, currentLanguage)}</p>
          </div>
          <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={onClose}>
            {translate('payment.close')}
          </button>
        </div>

        <div className="grid gap-4">
          <label>
            {translate('tourGroups.option')}
            <select value={selectedOptionId} onChange={event => setSelectedOptionId(event.target.value)}>
              <option value="" disabled>
                {translate('tourGroups.optionPlaceholder')}
              </option>
              {options.map(option => (
                <option key={option.optionId} value={option.optionId}>
                  {option.label}
                </option>
              ))}
            </select>
          </label>

          {selectedOption ? (
            <div className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
              <strong>{selectedOption.label}</strong>
              <p>{selectedOption.description}</p>
            </div>
          ) : null}

          <label>
            {translate('tourGroups.quantity')}
            <input type="number" min={1} value={quantity} onChange={event => setQuantity(Number(event.target.value || 1))} />
          </label>

          <div className="grid gap-2">
            <p className="text-sm font-medium text-slate-500">{translate('tourGroups.selectionTravelers')}</p>
            {availableTravelers.map(traveler => (
              <label key={traveler.travelerId} className="flex items-center gap-2">
                <input
                  type="checkbox"
                  checked={selectedTravelerIds.includes(traveler.travelerId)}
                  onChange={() => toggleTraveler(traveler.travelerId)}
                />
                {formatTravelerChipLabel(traveler)}
              </label>
            ))}
          </div>

          <div className="flex flex-wrap items-center gap-3">
            <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
              type="button"
              disabled={isBusy || !selectedOptionId || selectedTravelerIds.length === 0}
              onClick={() => void onSaveDraft({ optionId: selectedOptionId, quantity, travelerIds: selectedTravelerIds })}
            >
              {translate('tourGroups.saveDraft')}
            </button>
            <button
              type="button"
              className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
              disabled={isBusy || !selectedOptionId || selectedTravelerIds.length === 0}
              onClick={() => void onSaveAndSubmit({ optionId: selectedOptionId, quantity, travelerIds: selectedTravelerIds })}
            >
              {translate('tourGroups.submitSelection')}
            </button>
          </div>
        </div>
      </div>
    </div>
  )
}

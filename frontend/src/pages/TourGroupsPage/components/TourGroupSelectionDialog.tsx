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
    <div className="modal-backdrop" role="presentation">
      <div className="modal-card" role="dialog" aria-modal="true" aria-label={translate('tourGroups.createSelection')}>
        <div className="panel-heading">
          <div>
            <p className="eyebrow-label">{translate('tourGroups.selectionDialogEyebrow')}</p>
            <h3>{planItem.title}</h3>
            <p>{localizeTourGroupItemType(planItem.itemType, currentLanguage)}</p>
          </div>
          <button type="button" className="secondary-button" disabled={isBusy} onClick={onClose}>
            {translate('payment.close')}
          </button>
        </div>

        <div className="stack-form">
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
            <div className="list-surface">
              <strong>{selectedOption.label}</strong>
              <p>{selectedOption.description}</p>
            </div>
          ) : null}

          <label>
            {translate('tourGroups.quantity')}
            <input type="number" min={1} value={quantity} onChange={event => setQuantity(Number(event.target.value || 1))} />
          </label>

          <div className="checkbox-list">
            <p className="detail-label">{translate('tourGroups.selectionTravelers')}</p>
            {availableTravelers.map(traveler => (
              <label key={traveler.travelerId} className="checkbox-row">
                <input
                  type="checkbox"
                  checked={selectedTravelerIds.includes(traveler.travelerId)}
                  onChange={() => toggleTraveler(traveler.travelerId)}
                />
                {formatTravelerChipLabel(traveler)}
              </label>
            ))}
          </div>

          <div className="action-cluster">
            <button
              type="button"
              disabled={isBusy || !selectedOptionId || selectedTravelerIds.length === 0}
              onClick={() => void onSaveDraft({ optionId: selectedOptionId, quantity, travelerIds: selectedTravelerIds })}
            >
              {translate('tourGroups.saveDraft')}
            </button>
            <button
              type="button"
              className="secondary-button"
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

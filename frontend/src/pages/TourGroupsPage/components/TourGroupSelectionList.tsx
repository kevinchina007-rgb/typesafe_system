import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse, GroupSelectionOrderProjectionResponse, GroupPlanSelectionResponse, TourGroupMembershipResponse } from '@/lib/mvp-types/index'
import { useEffect, useMemo, useState } from 'react'
import { formatSelectionStatusLabel, formatSelectionTitle, getTourGroupConceptLabel } from '@/lib/presenters/tour-group-presenter'

type TourGroupSelectionListProps = {
  currentLanguage: AppLanguage
  title: string
  eyebrow: string
  isBusy: boolean
  selections: GroupPlanSelectionResponse[]
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  activeMembership: TourGroupMembershipResponse | null
  linkedSelectionIds?: string[]
  selectionOrderProjections?: GroupSelectionOrderProjectionResponse[]
  translate: (translationKey: string) => string
  onSubmitSelection?: (selectionId: string) => Promise<void>
  onConfirmSelection?: (selectionId: string, note: string) => Promise<void>
  onRejectSelection?: (selectionId: string, note: string) => Promise<void>
  onBatchConfirmSelections?: (selectionIds: string[]) => Promise<void>
  onBatchRejectSelections?: (selectionIds: string[], note: string) => Promise<void>
  onBatchPaySelections?: (selectionIds: string[]) => Promise<void>
  onOpenBookings?: () => void
}

export function TourGroupSelectionList({
  currentLanguage,
  title,
  eyebrow,
  isBusy,
  selections,
  planItems,
  planOptions,
  activeMembership,
  linkedSelectionIds = [],
  selectionOrderProjections = [],
  translate,
  onSubmitSelection,
  onConfirmSelection,
  onRejectSelection,
  onBatchConfirmSelections,
  onBatchRejectSelections,
  onBatchPaySelections,
  onOpenBookings,
}: TourGroupSelectionListProps) {
  const [selectedSelectionIds, setSelectedSelectionIds] = useState<string[]>([])
  const selectionOrderProjectionBySelectionId = useMemo(
    () => new Map(selectionOrderProjections.map(projection => [projection.selectionId, projection])),
    [selectionOrderProjections],
  )

  const batchConfirmableSelectionIds = useMemo(
    () => selections.filter(selection => selection.status === 'Submitted').map(selection => selection.selectionId),
    [selections],
  )
  const batchPayableSelectionIds = useMemo(
    () =>
      selections
        .filter(selection => selection.status === 'OrganizerConfirmed' && activeMembership?.membershipId === selection.membershipId)
        .map(selection => selection.selectionId),
    [activeMembership, selections],
  )

  const selectableSelectionIds =
    onBatchPaySelections ? batchPayableSelectionIds : onBatchConfirmSelections || onBatchRejectSelections ? batchConfirmableSelectionIds : []

  useEffect(() => {
    setSelectedSelectionIds(currentSelectionIds => currentSelectionIds.filter(selectionId => selectableSelectionIds.includes(selectionId)))
  }, [selectableSelectionIds])

  function toggleSelection(selectionId: string) {
    setSelectedSelectionIds(currentSelectionIds =>
      currentSelectionIds.includes(selectionId)
        ? currentSelectionIds.filter(currentSelectionId => currentSelectionId !== selectionId)
        : [...currentSelectionIds, selectionId],
    )
  }

  function selectAllCurrentSelections() {
    setSelectedSelectionIds(selectableSelectionIds)
  }

  return (
    <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{eyebrow || getTourGroupConceptLabel('selection', currentLanguage)}</p>
          <h3>{title}</h3>
        </div>
        {selectableSelectionIds.length > 0 ? (
          <div className="flex flex-wrap items-center gap-3">
            <button type="button" className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" disabled={isBusy} onClick={selectAllCurrentSelections}>
              {translate('tourGroups.selectAll')}
            </button>
            {onBatchConfirmSelections ? (
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                type="button"
                disabled={isBusy || selectedSelectionIds.length === 0}
                onClick={() => void onBatchConfirmSelections(selectedSelectionIds)}
              >
                {translate('tourGroups.batchConfirmSelections')}
              </button>
            ) : null}
            {onBatchRejectSelections ? (
              <button
                type="button"
                className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy || selectedSelectionIds.length === 0}
                onClick={() => void onBatchRejectSelections(selectedSelectionIds, translate('tourGroups.defaultRejectNote'))}
              >
                {translate('tourGroups.batchRejectSelections')}
              </button>
            ) : null}
            {onBatchPaySelections ? (
              <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                type="button"
                disabled={isBusy || selectedSelectionIds.length === 0}
                onClick={() => void onBatchPaySelections(selectedSelectionIds)}
              >
                {translate('tourGroups.batchPaySelections')}
              </button>
            ) : null}
          </div>
        ) : null}
      </div>

      {selections.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.noSelections')}</p>
      ) : (
        <ul className="grid gap-3">
          {selections.map(selection => {
            const planItem = planItems.find(item => item.planItemId === selection.planItemId)
            const planOption = planOptions.find(option => option.optionId === selection.optionId)
            const isMySelection = activeMembership?.membershipId === selection.membershipId
            const hasLinkedBooking = linkedSelectionIds.includes(selection.selectionId)
            const orderProjection = selectionOrderProjectionBySelectionId.get(selection.selectionId)
            const isSelectable = selectableSelectionIds.includes(selection.selectionId)

            return (
              <li key={selection.selectionId}>
                <div>
                  {isSelectable ? (
                    <label className="flex items-center gap-2">
                      <input
                        type="checkbox"
                        checked={selectedSelectionIds.includes(selection.selectionId)}
                        onChange={() => toggleSelection(selection.selectionId)}
                      />
                      <span>{translate('tourGroups.selection')}</span>
                    </label>
                  ) : null}
                  <strong>{formatSelectionTitle(selection, planItem, planOption)}</strong>
                  <p>{`${translate('tourGroups.quantity')}: ${selection.quantity}`}</p>
                  <p>{`${translate('tourGroups.selectionTravelers')}: ${selection.travelerIds.length}`}</p>
                  <p>{formatSelectionStatusLabel(selection, currentLanguage)}</p>
                  {hasLinkedBooking ? <p>{translate('tourGroups.linkedBookingReadyHint')}</p> : null}
                  {selection.reviewNote ? <p>{`${translate('tourGroups.reviewNote')}: ${selection.reviewNote}`}</p> : null}
                  {orderProjection ? (
                    <>
                      <p>{`${translate('tourGroups.orderStatusSummary')}: ${orderProjection.orderStatus}`}</p>
                      <p>{`${translate('tourGroups.paymentStatusSummary')}: ${orderProjection.paymentStatus}`}</p>
                      <p>{`${translate('tourGroups.supplierStatusSummary')}: ${orderProjection.supplierReviewStatus}`}</p>
                      {orderProjection.refundStatus ? <p>{`${translate('tourGroups.refundStatusSummary')}: ${orderProjection.refundStatus}`}</p> : null}
                      <p>{orderProjection.bookingSummaryLabel}</p>
                    </>
                  ) : null}
                </div>

                <div className="flex flex-wrap items-center gap-3">
                  {isMySelection && selection.status === 'Draft' && onSubmitSelection ? (
                    <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => void onSubmitSelection(selection.selectionId)}>
                      {translate('tourGroups.submitSelection')}
                    </button>
                  ) : null}

                  {selection.status === 'Submitted' && onConfirmSelection && onRejectSelection ? (
                    <>
                      <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => void onConfirmSelection(selection.selectionId, '')}>
                        {translate('tourGroups.confirmSelection')}
                      </button>
                      <button
                        type="button"
                        className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                        disabled={isBusy}
                        onClick={() => void onRejectSelection(selection.selectionId, translate('tourGroups.defaultRejectNote'))}
                      >
                        {translate('tourGroups.rejectSelection')}
                      </button>
                    </>
                  ) : null}

                  {isMySelection && selection.status === 'OrganizerConfirmed' && hasLinkedBooking && onOpenBookings ? (
                    <div className="flex flex-wrap items-center gap-3">
                      <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={onOpenBookings}>
                        {translate('tourGroups.openLinkedBooking')}
                      </button>
                    </div>
                  ) : null}
                </div>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}

import type {
  AppLanguage,
  GroupPlanItemResponse,
  GroupPlanOptionResponse,
  GroupPlanSelectionResponse,
  TourGroupMembershipResponse,
} from '../lib/mvp-types'
import { formatSelectionStatusLabel, formatSelectionTitle, getTourGroupConceptLabel } from '../lib/tour-group-presenter'

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
  translate: (translationKey: string) => string
  onSubmitSelection?: (selectionId: string) => Promise<void>
  onConfirmSelection?: (selectionId: string, note: string) => Promise<void>
  onRejectSelection?: (selectionId: string, note: string) => Promise<void>
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
  translate,
  onSubmitSelection,
  onConfirmSelection,
  onRejectSelection,
  onOpenBookings,
}: TourGroupSelectionListProps) {
  return (
    <section className="list-surface">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{eyebrow || getTourGroupConceptLabel('selection', currentLanguage)}</p>
          <h3>{title}</h3>
        </div>
      </div>

      {selections.length === 0 ? (
        <p className="empty-state">{translate('tourGroups.noSelections')}</p>
      ) : (
        <ul className="entity-list">
          {selections.map(selection => {
            const planItem = planItems.find(item => item.planItemId === selection.planItemId)
            const planOption = planOptions.find(option => option.optionId === selection.optionId)
            const isMySelection = activeMembership?.membershipId === selection.membershipId
            const hasLinkedBooking = linkedSelectionIds.includes(selection.selectionId)

            return (
              <li key={selection.selectionId}>
                <div>
                  <strong>{formatSelectionTitle(selection, planItem, planOption)}</strong>
                  <p>{`${translate('tourGroups.quantity')}: ${selection.quantity}`}</p>
                  <p>{`${translate('tourGroups.selectionTravelers')}: ${selection.travelerIds.length}`}</p>
                  <p>{formatSelectionStatusLabel(selection, currentLanguage)}</p>
                  {hasLinkedBooking ? <p>{translate('tourGroups.linkedBookingReadyHint')}</p> : null}
                  {selection.reviewNote ? <p>{`${translate('tourGroups.reviewNote')}: ${selection.reviewNote}`}</p> : null}
                </div>

                <div className="compact-action-block">
                  {isMySelection && selection.status === 'Draft' && onSubmitSelection ? (
                    <button type="button" disabled={isBusy} onClick={() => void onSubmitSelection(selection.selectionId)}>
                      {translate('tourGroups.submitSelection')}
                    </button>
                  ) : null}

                  {selection.status === 'Submitted' && onConfirmSelection && onRejectSelection ? (
                    <>
                      <button type="button" disabled={isBusy} onClick={() => void onConfirmSelection(selection.selectionId, '')}>
                        {translate('tourGroups.confirmSelection')}
                      </button>
                      <button
                        type="button"
                        className="secondary-button"
                        disabled={isBusy}
                        onClick={() => void onRejectSelection(selection.selectionId, translate('tourGroups.defaultRejectNote'))}
                      >
                        {translate('tourGroups.rejectSelection')}
                      </button>
                    </>
                  ) : null}

                  {isMySelection && selection.status === 'OrganizerConfirmed' && hasLinkedBooking && onOpenBookings ? (
                    <div className="action-cluster">
                      <button type="button" disabled={isBusy} onClick={onOpenBookings}>
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

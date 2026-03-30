import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse } from '../lib/mvp-types'
import { formatPlanItemSummary, formatPlanOptionSummary } from '../lib/tour-group-presenter'

type TourGroupPlanSectionProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  isOrganizer: boolean
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  activePlanItemId?: string | null
  translate: (translationKey: string) => string
  onSelectPlanItem?: (planItem: GroupPlanItemResponse) => void
  onOpenChoose?: (planItem: GroupPlanItemResponse) => void
}

export function TourGroupPlanSection({
  currentLanguage,
  isBusy,
  isOrganizer,
  planItems,
  planOptions,
  activePlanItemId,
  translate,
  onSelectPlanItem,
  onOpenChoose,
}: TourGroupPlanSectionProps) {
  return (
    <section className="list-surface">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('tourGroups.planSectionEyebrow')}</p>
          <h3>{translate('tourGroups.planItems')}</h3>
        </div>
      </div>

      {isOrganizer ? <p className="hero-copy">{translate('tourGroups.planManageHint')}</p> : null}

      {planItems.length === 0 ? (
        <p className="empty-state">{translate('tourGroups.noPlanItems')}</p>
      ) : (
        <ul className="entity-list">
          {planItems
            .slice()
            .sort((left, right) => left.sequenceNo - right.sequenceNo)
            .map(planItem => {
              const itemOptions = planOptions.filter(option => option.planItemId === planItem.planItemId)
              return (
                <li key={planItem.planItemId} className="tour-group-plan-item">
                  <div className="tour-group-plan-main">
                    <strong>{`${planItem.sequenceNo}. ${planItem.title}`}</strong>
                    <p>{formatPlanItemSummary(planItem, currentLanguage, translate)}</p>
                    <p>{planItem.description}</p>
                    {itemOptions.length > 0 ? (
                      <ul className="entity-list">
                        {itemOptions.map(option => (
                          <li key={option.optionId}>
                            <div>
                              <strong>{option.label}</strong>
                              <p>{formatPlanOptionSummary(option, currentLanguage)}</p>
                              <p>{option.description}</p>
                            </div>
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <p className="empty-state">{translate('tourGroups.noOptions')}</p>
                    )}
                  </div>

                  <div className="compact-action-block">
                    {isOrganizer && onSelectPlanItem ? (
                      <button
                        type="button"
                        className={activePlanItemId === planItem.planItemId ? 'secondary-button' : undefined}
                        disabled={isBusy}
                        onClick={() => onSelectPlanItem(planItem)}
                      >
                        {activePlanItemId === planItem.planItemId
                          ? translate('tourGroups.editOptionsAction')
                          : translate('tourGroups.addOptionAction')}
                      </button>
                    ) : null}
                    {!isOrganizer && onOpenChoose ? (
                      <button type="button" disabled={isBusy || itemOptions.length === 0} onClick={() => onOpenChoose(planItem)}>
                        {translate('tourGroups.chooseAction')}
                      </button>
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

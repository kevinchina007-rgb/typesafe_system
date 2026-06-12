// 本文件定义 TourGroupsPage 页面的页面分区，负责某一块独立内容的展示。

import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse } from '@/lib/mvp-types/index'
import { formatPlanItemSummary, formatPlanOptionSummary } from '@/lib/presenters/tour-group-presenter'

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
    <section className="grid gap-3 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-sky-700">{translate('tourGroups.planSectionEyebrow')}</p>
          <h3>{translate('tourGroups.planItems')}</h3>
        </div>
      </div>

      {isOrganizer ? <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.planManageHint')}</p> : null}

      {planItems.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.noPlanItems')}</p>
      ) : (
        <ul className="grid gap-3">
          {planItems
            .slice()
            .sort((left, right) => left.sequenceNo - right.sequenceNo)
            .map(planItem => {
              const itemOptions = planOptions.filter(option => option.planItemId === planItem.planItemId)
              return (
                <li key={planItem.planItemId} className="grid gap-2 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4">
                  <div className="grid gap-2">
                    <strong>{`${planItem.sequenceNo}. ${planItem.title}`}</strong>
                    <p>{formatPlanItemSummary(planItem, currentLanguage, translate)}</p>
                    <p>{planItem.description}</p>
                    {itemOptions.length > 0 ? (
                      <ul className="grid gap-3">
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
                      <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.noOptions')}</p>
                    )}
                  </div>

                  <div className="flex flex-wrap items-center gap-3">
                    {isOrganizer && onSelectPlanItem ? (
                      <button
                        type="button"
                        className={activePlanItemId === planItem.planItemId ? 'inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55' : undefined}
                        disabled={isBusy}
                        onClick={() => onSelectPlanItem(planItem)}
                      >
                        {activePlanItemId === planItem.planItemId
                          ? translate('tourGroups.editOptionsAction')
                          : translate('tourGroups.addOptionAction')}
                      </button>
                    ) : null}
                    {!isOrganizer && onOpenChoose ? (
                      <button className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy || itemOptions.length === 0} onClick={() => onOpenChoose(planItem)}>
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

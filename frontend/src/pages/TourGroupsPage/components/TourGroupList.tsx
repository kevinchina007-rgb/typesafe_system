import type { AppLanguage, TourGroupSummaryResponse, UserResponse } from '@/lib/mvp-types/index'
import { formatGroupCardSubtitle, getTourGroupConceptLabel } from '@/lib/presenters/tour-group-presenter'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'

type TourGroupListProps = {
  currentLanguage: AppLanguage
  groups: TourGroupSummaryResponse[]
  selectedGroupId: string | null
  signedInUser: UserResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onOpenCreateDialog: () => void
  onSelectGroup: (groupId: string) => void
}

export function TourGroupList({
  currentLanguage,
  groups,
  selectedGroupId,
  signedInUser,
  isBusy,
  translate,
  onOpenCreateDialog,
  onSelectGroup,
}: TourGroupListProps) {
  return (
    <section className="grid gap-3 border border-slate-200 bg-white p-4 text-slate-950 shadow-sm shadow-slate-200/50">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-slate-500">{getTourGroupConceptLabel('group', currentLanguage)}</p>
          <h3>{translate('tourGroups.groupList')}</h3>
        </div>
        {signedInUser ? (
          <button className="inline-flex min-h-11 items-center justify-center border border-slate-300 bg-white px-4 py-2 text-sm font-semibold text-slate-950 shadow-none transition hover:border-black hover:bg-black hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={onOpenCreateDialog}>
            {translate('tourGroups.createGroup')}
          </button>
        ) : null}
      </div>

      {groups.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.empty')}</p>
      ) : (
        <ul className="grid gap-3">
          {groups.map(group => (
            <li key={group.groupId}>
              <button
                type="button"
                className={`grid w-full gap-2 border border-slate-200 bg-white p-4 text-left text-slate-950 transition hover:border-black hover:bg-black hover:text-white ${selectedGroupId === group.groupId ? 'border-black bg-black text-white' : ''}`}
                onClick={() => onSelectGroup(group.groupId)}
              >
                <div>
                  <strong>{group.title}</strong>
                  <p>{formatGroupCardSubtitle(group)}</p>
                  <p>{`${group.startDate} - ${group.endDate}`}</p>
                </div>
                <span className="inline-flex min-h-9 items-center justify-center border border-slate-300 bg-white px-3 py-1 text-sm font-medium text-slate-950">{localizeTourGroupStatus(group.status, currentLanguage)}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

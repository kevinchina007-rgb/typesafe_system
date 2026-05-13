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
    <section className="list-surface">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{getTourGroupConceptLabel('group', currentLanguage)}</p>
          <h3>{translate('tourGroups.groupList')}</h3>
        </div>
        {signedInUser ? (
          <button type="button" disabled={isBusy} onClick={onOpenCreateDialog}>
            {translate('tourGroups.createGroup')}
          </button>
        ) : null}
      </div>

      {groups.length === 0 ? (
        <p className="empty-state">{translate('tourGroups.empty')}</p>
      ) : (
        <ul className="entity-list">
          {groups.map(group => (
            <li key={group.groupId}>
              <button
                type="button"
                className={`tour-group-list-button ${selectedGroupId === group.groupId ? 'is-active' : ''}`}
                onClick={() => onSelectGroup(group.groupId)}
              >
                <div>
                  <strong>{group.title}</strong>
                  <p>{formatGroupCardSubtitle(group)}</p>
                  <p>{`${group.startDate} - ${group.endDate}`}</p>
                </div>
                <span className="tag-chip">{localizeTourGroupStatus(group.status, currentLanguage)}</span>
              </button>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

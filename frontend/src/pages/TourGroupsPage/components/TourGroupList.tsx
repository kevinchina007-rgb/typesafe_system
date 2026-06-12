// 本文件定义 TourGroupsPage 页面的列表组件，负责展示条目集合。

import type { AppLanguage, TourGroupSummaryResponse, UserResponse } from '@/lib/mvp-types/index'
import { formatGroupCardSubtitle } from '@/lib/presenters/tour-group-presenter'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'

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
    <section className="grid gap-3 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <h3>{translate('tourGroups.groupList')}</h3>
        </div>
        {signedInUser ? (
            <button className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={onOpenCreateDialog}>
              {translate('tourGroups.createGroup')}
            </button>
        ) : null}
      </div>

      {groups.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.empty')}</p>
      ) : (
        <ul className="grid gap-4 md:grid-cols-2 xl:grid-cols-4">
          {groups.map(group => {
            const isSelected = selectedGroupId === group.groupId
            return (
              <li key={group.groupId}>
                <button
                  type="button"
                  className={`flex h-full w-full min-w-0 flex-col gap-3 border p-4 text-left transition ${
                    isSelected
                      ? 'border-slate-950 bg-black text-white shadow-lg shadow-sky-200/70 hover:bg-black hover:text-white'
                      : 'border-sky-200 bg-white text-slate-950 hover:border-sky-700 hover:bg-sky-50 hover:text-slate-950'
                  }`}
                  onClick={() => onSelectGroup(group.groupId)}
                >
                  {group.coverImageUrl ? <BackendAssetImage className="aspect-[4/3] w-full rounded-sm object-cover" assetUrl={group.coverImageUrl} alt={group.title} /> : null}
                  <div className="grid min-w-0 gap-1">
                    <strong className="min-w-0 break-words text-lg leading-6">{group.title}</strong>
                    <p className="min-w-0 break-words text-sm leading-5">{formatGroupCardSubtitle(group)}</p>
                    <p className="min-w-0 break-words text-sm leading-5">{`${group.startDate} - ${group.endDate}`}</p>
                    {group.tags.length > 0 ? <p className="min-w-0 break-words text-sm leading-5">{group.tags.join(' / ')}</p> : null}
                  </div>
                  <span className="inline-flex min-h-9 items-center justify-center border border-sky-200 bg-white/90 px-3 py-1 text-sm font-medium text-sky-800">{localizeTourGroupStatus(group.status, currentLanguage)}</span>
                </button>
              </li>
            )
          })}
        </ul>
      )}
    </section>
  )
}

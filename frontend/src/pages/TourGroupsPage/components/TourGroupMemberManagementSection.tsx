// 本文件定义 TourGroupsPage 页面的页面分区，负责某一块独立内容的展示。

import type { TourGroupBlacklistResponse, TourGroupMembershipResponse, UserResponse } from '@/lib/mvp-types/index'

type TourGroupMemberManagementSectionProps = {
  memberships: TourGroupMembershipResponse[]
  blacklists: TourGroupBlacklistResponse[]
  organizerUserId: string
  signedInUser: UserResponse | null
  isBusy: boolean
  translate: (translationKey: string) => string
  onKickMember: (targetUserId: string) => Promise<void>
  onBlacklistMember: (targetUserId: string) => Promise<void>
  onTransferOrganizer: (targetUserId: string) => Promise<void>
}

export function TourGroupMemberManagementSection({
  memberships,
  blacklists,
  organizerUserId,
  signedInUser,
  isBusy,
  translate,
  onKickMember,
  onBlacklistMember,
  onTransferOrganizer,
}: TourGroupMemberManagementSectionProps) {
  const activeMembers = memberships.filter(membership => membership.status === 'Active')

  if (!signedInUser || signedInUser.userId !== organizerUserId) {
    return null
  }

  return (
    <section className="grid gap-4 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
      <div className="grid gap-1">
        <p className="text-sm font-bold text-sky-700">{translate('tourGroups.memberManagementEyebrow')}</p>
        <h4 className="text-lg font-bold text-slate-950">{translate('tourGroups.memberManagementTitle')}</h4>
      </div>

      {activeMembers.length === 0 ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.memberManagementEmpty')}</p>
      ) : (
        <ul className="grid gap-3">
          {activeMembers.map(member => {
            const isSelf = member.userId === signedInUser.userId
            const isOrganizer = member.userId === organizerUserId
            return (
              <li key={member.membershipId} className="grid gap-3 border border-sky-200 bg-gradient-to-br from-white via-cyan-50 to-slate-50 p-4">
                <div className="grid gap-1">
                  <strong className="text-base font-semibold text-slate-950">
                    {member.userDisplayName ?? member.userId}
                    {isOrganizer ? ` (${translate('tourGroups.organizerTag')})` : null}
                  </strong>
                  <span className="text-sm text-slate-500">{member.userId}</span>
                </div>
                {!isSelf && !isOrganizer ? (
                  <div className="flex flex-wrap gap-2">
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-3 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      disabled={isBusy}
                      onClick={() => void onTransferOrganizer(member.userId)}
                    >
                      {translate('tourGroups.transferOrganizer')}
                    </button>
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-3 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55"
                      disabled={isBusy}
                      onClick={() => void onKickMember(member.userId)}
                    >
                      {translate('tourGroups.kickMember')}
                    </button>
                    <button
                      type="button"
                      className="inline-flex min-h-11 items-center justify-center border border-rose-300 bg-rose-50 px-3 py-2 text-sm font-semibold text-rose-700 shadow-none transition hover:border-rose-500 hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-55"
                      disabled={isBusy}
                      onClick={() => void onBlacklistMember(member.userId)}
                    >
                      {translate('tourGroups.blacklistMember')}
                    </button>
                  </div>
                ) : (
                  <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.organizerMemberHint')}</p>
                )}
              </li>
            )
          })}
        </ul>
      )}

      <div className="grid gap-3 border-t border-slate-200 pt-4">
        <div className="grid gap-1">
          <p className="text-sm font-bold text-slate-500">{translate('tourGroups.blacklistEyebrow')}</p>
          <h5 className="text-base font-bold text-slate-950">{translate('tourGroups.blacklistTitle')}</h5>
        </div>
        {blacklists.length === 0 ? (
          <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.blacklistEmpty')}</p>
        ) : (
          <ul className="grid gap-2">
            {blacklists.map(item => (
              <li key={item.blacklistId} className="grid gap-1 border border-rose-200 bg-rose-50 p-3 text-sm text-rose-900">
                <strong>{item.userId}</strong>
                <span>{translate('tourGroups.blacklistReason')}: {item.reason}</span>
                <span>{translate('tourGroups.blacklistCreatedAt')}: {item.createdAt}</span>
              </li>
            ))}
          </ul>
        )}
      </div>
    </section>
  )
}

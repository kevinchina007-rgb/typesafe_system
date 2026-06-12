// 本文件定义 TourGroupsPage 页面的页面分区，负责某一块独立内容的展示。

import type { AppLanguage, TourGroupDetailsResponse, TourGroupMembershipResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { formatTravelerChipLabel, getTourGroupConceptLabel } from '@/lib/presenters/tour-group-presenter'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'

type TourGroupMemberSectionProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  activeMembership: TourGroupMembershipResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onJoinGroup: () => Promise<void>
  onLeaveGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onRemoveMembershipTraveler: (travelerId: string) => Promise<void>
}

export function TourGroupMemberSection({
  currentLanguage,
  details,
  signedInUser,
  activeMembership,
  travelers,
  isBusy,
  translate,
  onJoinGroup,
  onLeaveGroup,
  onAddMembershipTraveler,
  onRemoveMembershipTraveler,
}: TourGroupMemberSectionProps) {
  const membershipTravelerIds = new Set(
    activeMembership
      ? details.membershipTravelers
          .filter(row => row.membershipId === activeMembership.membershipId && row.status === 'Active')
          .map(row => row.travelerId)
      : [],
  )

  const availableTravelers = travelers.filter(traveler => !membershipTravelerIds.has(traveler.travelerId))
  const joinedTravelers = travelers.filter(traveler => membershipTravelerIds.has(traveler.travelerId))

  return (
    <section className="grid gap-3 border border-sky-200 bg-white/85 p-4 text-slate-950 shadow-sm shadow-sky-100/40">
      <div className="text-lg font-bold text-slate-950">
        <div>
          <p className="text-sm font-bold text-sky-700">{getTourGroupConceptLabel('member', currentLanguage)}</p>
          <h3>{translate('tourGroups.memberSectionTitle')}</h3>
        </div>
      </div>

      {!signedInUser ? (
        <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.guest')}</p>
      ) : !activeMembership ? (
        <div className="grid gap-4">
          <p className="m-0 max-w-3xl text-base leading-7 text-slate-600">{translate('tourGroups.joinHint')}</p>
          <div className="flex flex-wrap items-center gap-3">
            <button className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => void onJoinGroup()}>
              {translate('tourGroups.joinGroup')}
            </button>
          </div>
        </div>
      ) : (
        <div className="grid gap-4">
          <div className="grid gap-3 md:grid-cols-2">
            <div>
              <span className="text-sm font-medium text-slate-500">{translate('tourGroups.membershipStatus')}</span>
              <strong>{localizeTourGroupStatus(activeMembership.status, currentLanguage)}</strong>
            </div>
            <div>
              <span className="text-sm font-medium text-slate-500">{translate('tourGroups.joinedAt')}</span>
              <strong>{activeMembership.joinedAt}</strong>
            </div>
          </div>

          <div className="grid gap-4 border border-sky-200 bg-white/85 p-5 text-slate-950 shadow-sm shadow-sky-100/40">
            <h4>{translate('tourGroups.myTravelers')}</h4>
            {joinedTravelers.length === 0 ? (
              <p className="text-sm leading-6 text-slate-500">{translate('tourGroups.noJoinedTravelers')}</p>
            ) : (
              <ul className="grid gap-3">
                {joinedTravelers.map(traveler => (
                  <li key={traveler.travelerId}>
                    <div className="flex flex-wrap items-center justify-between gap-3">
                      <div>
                        <strong>{traveler.fullName}</strong>
                        <p>{formatTravelerChipLabel(traveler)}</p>
                      </div>
                      <div className="flex flex-wrap items-center gap-3">
                        <span className="inline-flex min-h-9 items-center justify-center border border-sky-200 bg-sky-50 px-3 py-1 text-sm font-medium text-sky-800">{translate('tourGroups.readyForChoices')}</span>
                        <button
                          className="inline-flex min-h-9 items-center justify-center border border-cyan-300 bg-cyan-50 px-3 py-1 text-sm font-semibold text-cyan-700 shadow-none transition hover:border-cyan-500 hover:bg-cyan-100 disabled:cursor-not-allowed disabled:opacity-55"
                          type="button"
                          disabled={isBusy}
                          onClick={() => void onRemoveMembershipTraveler(traveler.travelerId)}
                        >
                          {translate('tourGroups.removeMembershipTraveler')}
                        </button>
                      </div>
                    </div>
                  </li>
                ))}
              </ul>
            )}
          </div>

          <form
            className="grid gap-4 border border-sky-200 bg-white/85 p-5 text-slate-950 shadow-sm shadow-sky-100/40 grid gap-4"
            onSubmit={async event => {
              event.preventDefault()
              const formData = new FormData(event.currentTarget)
              await onAddMembershipTraveler(String(formData.get('travelerId') ?? ''))
              event.currentTarget.reset()
            }}
          >
            <h4>{translate('tourGroups.addMembershipTraveler')}</h4>
            <label>
              {translate('tourGroups.selectTraveler')}
              <select name="travelerId" required defaultValue="">
                <option value="" disabled>
                  {translate('tourGroups.selectTravelerPlaceholder')}
                </option>
                {availableTravelers.map(traveler => (
                  <option key={traveler.travelerId} value={traveler.travelerId}>
                    {formatTravelerChipLabel(traveler)}
                  </option>
                ))}
              </select>
            </label>
            <button className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-white px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55" type="submit" disabled={isBusy || availableTravelers.length === 0}>
              {translate('tourGroups.addMembershipTraveler')}
            </button>
          </form>

          {signedInUser?.userId !== details.group.organizerUserId ? (
            <div className="flex flex-wrap items-center gap-3">
              <button className="inline-flex min-h-11 items-center justify-center border border-rose-300 bg-rose-50 px-4 py-2 text-sm font-semibold text-rose-700 shadow-none transition hover:border-rose-500 hover:bg-rose-100 disabled:cursor-not-allowed disabled:opacity-55" type="button" disabled={isBusy} onClick={() => void onLeaveGroup()}>
                {translate('tourGroups.leaveGroup')}
              </button>
            </div>
          ) : null}
        </div>
      )}
    </section>
  )
}

// 本文件定义 TourGroupsPage 页面的工作区组件，负责组织主要操作区和信息区。

import { useEffect } from 'react'

import type { AppLanguage, GroupPlanItemResponse, GroupPlanOptionResponse, TourGroupMembershipResponse, UserResponse } from '@/lib/mvp-types/index'
import { TourGroupMemberManagementSection } from '@/pages/TourGroupsPage/components/TourGroupMemberManagementSection'
import { TourGroupPlanSection } from '@/pages/TourGroupsPage/components/TourGroupPlanSection'

type TourGroupOrganizerWorkspaceProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  activePlanItem: GroupPlanItemResponse | null
  planItems: GroupPlanItemResponse[]
  planOptions: GroupPlanOptionResponse[]
  memberships: TourGroupMembershipResponse[]
  blacklists: import('@/lib/mvp-types/index').TourGroupBlacklistResponse[]
  organizerUserId: string
  signedInUser: UserResponse | null
  translate: (translationKey: string) => string
  onSelectPlanItem: (planItem: GroupPlanItemResponse) => void
  onKickMember: (targetUserId: string) => Promise<void>
  onBlacklistMember: (targetUserId: string) => Promise<void>
  onTransferOrganizer: (targetUserId: string) => Promise<void>
}

export function TourGroupOrganizerWorkspace({
  currentLanguage,
  isBusy,
  activePlanItem,
  planItems,
  planOptions,
  memberships,
  blacklists,
  organizerUserId,
  signedInUser,
  translate,
  onSelectPlanItem,
  onKickMember,
  onBlacklistMember,
  onTransferOrganizer,
}: TourGroupOrganizerWorkspaceProps) {
  useEffect(() => {
    if (!activePlanItem && planItems.length > 0) {
      onSelectPlanItem(
        [...planItems].sort((left, right) => left.sequenceNo - right.sequenceNo)[0],
      )
    }
  }, [activePlanItem, onSelectPlanItem, planItems])

  return (
    <div className="grid gap-5 xl:grid-cols-[minmax(0,1fr)_20rem] xl:items-start">
      <div className="grid gap-4">
        <TourGroupPlanSection
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          isOrganizer
          planItems={planItems}
          planOptions={planOptions}
          activePlanItemId={activePlanItem?.planItemId ?? null}
          translate={translate}
          onSelectPlanItem={onSelectPlanItem}
        />
      </div>

      <aside className="grid gap-4">
        <TourGroupMemberManagementSection
          memberships={memberships}
          blacklists={blacklists}
          organizerUserId={organizerUserId}
          signedInUser={signedInUser}
          isBusy={isBusy}
          translate={translate}
          onKickMember={onKickMember}
          onBlacklistMember={onBlacklistMember}
          onTransferOrganizer={onTransferOrganizer}
        />
      </aside>
    </div>
  )
}

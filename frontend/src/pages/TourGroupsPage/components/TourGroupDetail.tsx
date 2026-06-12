// 本文件定义 TourGroupsPage 页面的页面组件。

import { useEffect, useState } from 'react'

import type { AppLanguage, AppViewKey, GroupPlanItemResponse, TourGroupDetailsResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'
import { BackendAssetImage } from '@/pages/shared/base/BackendAssetImage'
import { TourGroupMemberWorkspace } from '@/pages/TourGroupsPage/components/TourGroupMemberWorkspace'
import { TourGroupOrganizerWorkspace } from '@/pages/TourGroupsPage/components/TourGroupOrganizerWorkspace'
import { TourGroupChatPanel } from '@/pages/TourGroupsPage/components/TourGroupChatPanel'

type TourGroupDetailProps = {
  currentLanguage: AppLanguage
  details: TourGroupDetailsResponse
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  isBusy: boolean
  translate: (translationKey: string) => string
  onJoinGroup: () => Promise<void>
  onLeaveGroup: () => Promise<void>
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onRemoveMembershipTraveler: (travelerId: string) => Promise<void>
  onNavigate: (viewKey: AppViewKey) => void
  activePlanItem: GroupPlanItemResponse | null
  onSelectPlanItem: (planItem: GroupPlanItemResponse) => void
  onOpenChoose: (planItem: GroupPlanItemResponse) => void
  onKickMember: (targetUserId: string) => Promise<void>
  onBlacklistMember: (targetUserId: string) => Promise<void>
  onTransferOrganizer: (targetUserId: string) => Promise<void>
  onLoadChatSettings: (groupId: string) => Promise<import('@/lib/mvp-types/index').TourGroupChatSettingsResponse>
  onUpdateChatSettings: (groupId: string, payload: { allowMemberDirectChat: boolean }) => Promise<import('@/lib/mvp-types/index').TourGroupChatSettingsResponse>
  onLoadConversations: (groupId: string) => Promise<import('@/lib/mvp-types/index').TourGroupConversationListResponse>
  onSearchConversations: (groupId: string, query: string) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse[]>
  onSearchMessages: (groupId: string, query: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageSearchResultResponse[]>
  onGetOrCreateDirectConversation: (groupId: string, payload: { targetUserId: string }) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
  onLoadMessages: (conversationId: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onSendMessage: (conversationId: string, payload: {
    messageType?: string
    content: string
    replyToMessageId?: string | null
    attachments?: import('@/lib/mvp-types/index').TourGroupUploadedAttachmentResponse[]
  }) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onUploadAttachment: (groupId: string, conversationId: string, attachmentFile: File) => Promise<import('@/lib/mvp-types/index').TourGroupUploadedAttachmentResponse>
  onMarkConversationRead: (conversationId: string) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
  onEditMessage: (messageId: string, payload: { content: string }) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onDeleteMessage: (messageId: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onRecallMessage: (messageId: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onAddReaction: (messageId: string, reactionType: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onRemoveReaction: (messageId: string, reactionType: string) => Promise<import('@/lib/mvp-types/index').TourGroupMessageResponse[]>
  onUpdateMuteState: (conversationId: string, muted: boolean) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
  onUpdateArchiveState: (conversationId: string, archived: boolean) => Promise<import('@/lib/mvp-types/index').TourGroupConversationSummaryResponse>
}

export function TourGroupDetail({
  currentLanguage,
  details,
  signedInUser,
  travelers,
  isBusy,
  translate,
  activePlanItem,
  onJoinGroup,
  onLeaveGroup,
  onAddMembershipTraveler,
  onRemoveMembershipTraveler,
  onNavigate,
  onSelectPlanItem,
  onOpenChoose,
  onKickMember,
  onBlacklistMember,
  onTransferOrganizer,
  onLoadChatSettings,
  onUpdateChatSettings,
  onLoadConversations,
  onSearchConversations,
  onSearchMessages,
  onGetOrCreateDirectConversation,
  onLoadMessages,
  onSendMessage,
  onUploadAttachment,
  onMarkConversationRead,
  onEditMessage,
  onDeleteMessage,
  onRecallMessage,
  onAddReaction,
  onRemoveReaction,
  onUpdateMuteState,
  onUpdateArchiveState,
}: TourGroupDetailProps) {
  const isOrganizer = signedInUser?.userId === details.group.organizerUserId
  const [workspaceMode, setWorkspaceMode] = useState<'manage' | 'member'>('manage')
  const activeMembership =
    details.memberships.find(
      membership => membership.userId === signedInUser?.userId && membership.status === 'Active',
    ) ?? null

  useEffect(() => {
    if (!isOrganizer) {
      setWorkspaceMode('member')
    }
  }, [isOrganizer])

  return (
    <section className="grid gap-4">
      <div className="relative overflow-hidden border border-sky-200 text-slate-950 shadow-sm shadow-sky-100/50">
        <div className="absolute inset-0">
          {details.group.coverImageUrl ? (
            <BackendAssetImage
              className="absolute inset-0 h-full w-full object-cover"
              assetUrl={details.group.coverImageUrl}
              alt={details.group.title}
            />
          ) : (
            <div className="absolute inset-0 bg-gradient-to-br from-slate-100 via-white to-slate-200" />
          )}
          <div className="absolute inset-0 bg-cyan-950/10" />
          <div className="absolute inset-0 bg-gradient-to-b from-white/10 via-white/20 to-slate-50/60" />
        </div>

        <div className="relative grid gap-6 p-6 md:p-8">
          <div className="flex items-start justify-between gap-4">
            <div className="max-w-4xl rounded-2xl border border-sky-100 bg-white/82 p-5 backdrop-blur-sm">
              <p className="text-sm font-bold text-sky-700">{translate('tourGroups.detailEyebrow')}</p>
              <h2 className="break-words text-3xl font-bold tracking-tight text-slate-950">{details.group.title}</h2>
              <p className="mt-2 break-words text-base leading-7 text-slate-700">{details.group.description}</p>
              {details.group.tags.length > 0 ? <p className="mt-3 break-words text-sm font-medium text-slate-600">{details.group.tags.join(' / ')}</p> : null}
            </div>
            <span className="inline-flex min-h-9 items-center justify-center border border-sky-100 bg-white/85 px-3 py-1 text-sm font-medium text-sky-800 shadow-sm backdrop-blur-sm">
              {localizeTourGroupStatus(details.group.status, currentLanguage)}
            </span>
          </div>

          <div className="grid gap-3 rounded-2xl border border-sky-100 bg-white/78 p-5 backdrop-blur-sm md:grid-cols-2">
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.destination')}</span>
              <strong className="break-words">{details.group.destination}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.dateRange')}</span>
              <strong className="break-words">{`${details.group.startDate} - ${details.group.endDate}`}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.capacity')}</span>
              <strong className="break-words">{`${details.group.usedCapacity} / ${details.group.capacity}`}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.organizer')}</span>
              <strong className="break-words">{details.group.organizerUserId}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.memberCount')}</span>
              <strong>{details.group.memberCount}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.pendingApprovals')}</span>
              <strong>{details.group.pendingSelectionCount}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.confirmedSelectionCount')}</span>
              <strong>{details.group.confirmedSelectionCount}</strong>
            </div>
            <div className="min-w-0">
              <span className="text-sm font-medium text-sky-700">{translate('tourGroups.convertedOrderCount')}</span>
              <strong>{details.group.convertedOrderCount}</strong>
            </div>
          </div>

          {isOrganizer ? (
            <div className="flex flex-wrap items-center gap-3">
              <button
                type="button"
                className={workspaceMode === 'manage' ? 'inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55' : undefined}
                disabled={isBusy}
                onClick={() => setWorkspaceMode('manage')}
              >
                {translate('tourGroups.manageGroup')}
              </button>
              <button
                type="button"
                className={workspaceMode === 'member' ? 'inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-800 shadow-none transition hover:border-sky-700 hover:bg-sky-700 hover:text-white disabled:cursor-not-allowed disabled:opacity-55' : undefined}
                disabled={isBusy}
                onClick={() => setWorkspaceMode('member')}
              >
                {translate('tourGroups.enterGroup')}
              </button>
              <button
                type="button"
                className="inline-flex min-h-11 items-center justify-center border border-sky-300 bg-sky-50 px-4 py-2 text-sm font-semibold text-sky-700 shadow-none transition hover:border-sky-500 hover:bg-sky-100 disabled:cursor-not-allowed disabled:opacity-55"
                disabled={isBusy}
                onClick={() => {
                  const nextUrl = new URL(window.location.href)
                  nextUrl.searchParams.set('groupId', details.group.groupId)
                  window.history.replaceState(window.history.state, '', nextUrl)
                  onNavigate('tourGroupPlanBuilder')
                }}
              >
                {translate('nav.tourGroupPlanBuilder')}
              </button>
            </div>
          ) : null}
        </div>
      </div>

      {isOrganizer && workspaceMode === 'manage' ? (
        <TourGroupOrganizerWorkspace
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          activePlanItem={activePlanItem}
          planItems={details.planItems}
          planOptions={details.planOptions}
          memberships={details.memberships}
          blacklists={details.blacklists}
          organizerUserId={details.group.organizerUserId}
          signedInUser={signedInUser}
          translate={translate}
          onSelectPlanItem={onSelectPlanItem}
          onKickMember={onKickMember}
          onBlacklistMember={onBlacklistMember}
          onTransferOrganizer={onTransferOrganizer}
        />
      ) : (
        <TourGroupMemberWorkspace
          currentLanguage={currentLanguage}
          details={details}
          signedInUser={signedInUser}
          travelers={travelers}
          activeMembership={activeMembership}
          isBusy={isBusy}
          translate={translate}
          onJoinGroup={onJoinGroup}
          onLeaveGroup={onLeaveGroup}
          onAddMembershipTraveler={onAddMembershipTraveler}
          onRemoveMembershipTraveler={onRemoveMembershipTraveler}
          onOpenChoose={onOpenChoose}
        />
      )}

      <TourGroupChatPanel
        currentLanguage={currentLanguage}
        groupId={details.group.groupId}
        organizerUserId={details.group.organizerUserId}
        memberships={details.memberships}
        signedInUser={signedInUser}
        isBusy={isBusy}
        translate={translate}
        onLoadChatSettings={onLoadChatSettings}
        onUpdateChatSettings={onUpdateChatSettings}
        onLoadConversations={onLoadConversations}
        onSearchConversations={onSearchConversations}
        onSearchMessages={onSearchMessages}
        onGetOrCreateDirectConversation={onGetOrCreateDirectConversation}
        onLoadMessages={onLoadMessages}
        onSendMessage={onSendMessage}
        onUploadAttachment={onUploadAttachment}
        onMarkConversationRead={onMarkConversationRead}
        onEditMessage={onEditMessage}
        onDeleteMessage={onDeleteMessage}
        onRecallMessage={onRecallMessage}
        onAddReaction={onAddReaction}
        onRemoveReaction={onRemoveReaction}
        onUpdateMuteState={onUpdateMuteState}
        onUpdateArchiveState={onUpdateArchiveState}
      />
    </section>
  )
}

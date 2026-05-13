import { useEffect, useState } from 'react'

import type { AppLanguage, AttractionResponse, FlightResponse, GroupPlanItemResponse, HotelResponse, TourGroupDetailsResponse, TrainResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { localizeTourGroupStatus } from '@/lib/presenters/view-models'
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
  onAddMembershipTraveler: (travelerId: string) => Promise<void>
  onCreatePlanItem: (payload: {
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<GroupPlanItemResponse | null>
  activePlanItem: GroupPlanItemResponse | null
  onSelectPlanItem: (planItem: GroupPlanItemResponse) => void
  onCreateOption: (
    planItemId: string,
    payload: {
      resourceType: string
      resourceId: string
      resourceVariantCode?: string | null
      resourceContext?: string | null
      label: string
      description: string
      defaultQuantity: number
    },
  ) => Promise<void>
  onOpenChoose: (planItem: GroupPlanItemResponse) => void
  onSubmitSelection: (selectionId: string) => Promise<void>
  onConfirmSelection: (selectionId: string, note: string) => Promise<void>
  onRejectSelection: (selectionId: string, note: string) => Promise<void>
  onBatchConfirmSelections: (selectionIds: string[]) => Promise<void>
  onBatchRejectSelections: (selectionIds: string[], note: string) => Promise<void>
  onBatchPaySelections: (selectionIds: string[]) => Promise<void>
  onOpenBookings: () => void
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
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
  onAddMembershipTraveler,
  onCreatePlanItem,
  onSelectPlanItem,
  onCreateOption,
  onOpenChoose,
  onSubmitSelection,
  onConfirmSelection,
  onRejectSelection,
  onBatchConfirmSelections,
  onBatchRejectSelections,
  onBatchPaySelections,
  onOpenBookings,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
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

  const mySelections = activeMembership
    ? details.selections.filter(selection => selection.membershipId === activeMembership.membershipId)
    : []
  const linkedSelectionIds = details.selectionOrderLinks.map(link => link.selectionId)
  const pendingApprovals = details.selections.filter(selection => selection.status === 'Submitted')

  useEffect(() => {
    if (!isOrganizer) {
      setWorkspaceMode('member')
    }
  }, [isOrganizer])

  return (
    <section className="tour-group-detail-shell">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('tourGroups.detailEyebrow')}</p>
          <h2>{details.group.title}</h2>
          <p>{details.group.description}</p>
        </div>
        <span className="tag-chip">{localizeTourGroupStatus(details.group.status, currentLanguage)}</span>
      </div>

      <div className="detail-grid">
        <div>
          <span className="detail-label">{translate('tourGroups.destination')}</span>
          <strong>{details.group.destination}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.dateRange')}</span>
          <strong>{`${details.group.startDate} - ${details.group.endDate}`}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.capacity')}</span>
          <strong>{`${details.group.usedCapacity} / ${details.group.capacity}`}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.organizer')}</span>
          <strong>{details.group.organizerUserId}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.memberCount')}</span>
          <strong>{details.group.memberCount}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.pendingApprovals')}</span>
          <strong>{details.group.pendingSelectionCount}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.confirmedSelectionCount')}</span>
          <strong>{details.group.confirmedSelectionCount}</strong>
        </div>
        <div>
          <span className="detail-label">{translate('tourGroups.convertedOrderCount')}</span>
          <strong>{details.group.convertedOrderCount}</strong>
        </div>
      </div>

      {isOrganizer ? (
        <div className="tour-group-mode-switch">
          <button
            type="button"
            className={workspaceMode === 'manage' ? 'secondary-button' : undefined}
            disabled={isBusy}
            onClick={() => setWorkspaceMode('manage')}
          >
            {translate('tourGroups.manageGroup')}
          </button>
          <button
            type="button"
            className={workspaceMode === 'member' ? 'secondary-button' : undefined}
            disabled={isBusy}
            onClick={() => setWorkspaceMode('member')}
          >
            {translate('tourGroups.enterGroup')}
          </button>
        </div>
      ) : null}

      {isOrganizer && workspaceMode === 'manage' ? (
        <TourGroupOrganizerWorkspace
          currentLanguage={currentLanguage}
          isBusy={isBusy}
          activePlanItem={activePlanItem}
          planItems={details.planItems}
          planOptions={details.planOptions}
          selectionOrderProjections={details.selectionOrderProjections}
          pendingApprovals={pendingApprovals}
          activeMembership={activeMembership}
          translate={translate}
          onCreatePlanItem={onCreatePlanItem}
          onSelectPlanItem={onSelectPlanItem}
          onSearchFlights={onSearchFlights}
          onSearchHotels={onSearchHotels}
          onSearchTrains={onSearchTrains}
          onSearchAttractions={onSearchAttractions}
          onCreateOption={onCreateOption}
          onConfirmSelection={onConfirmSelection}
          onRejectSelection={onRejectSelection}
          onBatchConfirmSelections={onBatchConfirmSelections}
          onBatchRejectSelections={onBatchRejectSelections}
        />
      ) : (
        <TourGroupMemberWorkspace
          currentLanguage={currentLanguage}
          details={details}
          signedInUser={signedInUser}
          travelers={travelers}
          activeMembership={activeMembership}
          mySelections={mySelections}
          linkedSelectionIds={linkedSelectionIds}
          selectionOrderProjections={details.selectionOrderProjections}
          isBusy={isBusy}
          translate={translate}
          onJoinGroup={onJoinGroup}
          onAddMembershipTraveler={onAddMembershipTraveler}
          onOpenChoose={onOpenChoose}
          onSubmitSelection={onSubmitSelection}
          onBatchPaySelections={onBatchPaySelections}
          onOpenBookings={onOpenBookings}
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

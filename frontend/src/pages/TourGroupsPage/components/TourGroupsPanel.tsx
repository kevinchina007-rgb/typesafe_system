import { useEffect, useMemo, useState } from 'react'

import type { AppLanguage, AttractionResponse, FlightResponse, GroupPlanItemResponse, TourGroupDetailsResponse, TourGroupSummaryResponse, TrainResponse, HotelResponse, TravelerResponse, UserResponse } from '@/lib/mvp-types/index'
import { CreateTourGroupDialog } from '@/pages/TourGroupsPage/components/CreateTourGroupDialog'
import { TourGroupDetail } from '@/pages/TourGroupsPage/components/TourGroupDetail'
import { TourGroupList } from '@/pages/TourGroupsPage/components/TourGroupList'
import { TourGroupSelectionDialog } from '@/pages/TourGroupsPage/components/TourGroupSelectionDialog'

type TourGroupsPanelProps = {
  currentLanguage: AppLanguage
  isBusy: boolean
  signedInUser: UserResponse | null
  travelers: TravelerResponse[]
  translate: (translationKey: string) => string
  onListGroups: () => Promise<TourGroupSummaryResponse[]>
  onLoadGroupDetails: (groupId: string) => Promise<TourGroupDetailsResponse>
  onCreateGroup: (payload: {
    organizerUserId: string
    title: string
    description: string
    destination: string
    startDate: string
    endDate: string
    capacity: number
  }) => Promise<TourGroupDetailsResponse>
  onJoinGroup: (groupId: string, payload: { userId: string }) => Promise<TourGroupDetailsResponse>
  onAddMembershipTraveler: (groupId: string, payload: { userId: string; travelerId: string }) => Promise<TourGroupDetailsResponse>
  onCreatePlanItem: (groupId: string, payload: {
    organizerUserId: string
    itemType: string
    title: string
    description: string
    scheduledAt: string
    endsAt?: string | null
    sequenceNo: number
  }) => Promise<TourGroupDetailsResponse>
  onCreatePlanOption: (planItemId: string, groupId: string, payload: {
    organizerUserId: string
    resourceType: string
    resourceId: string
    resourceVariantCode?: string | null
    resourceContext?: string | null
    label: string
    description: string
    defaultQuantity: number
  }) => Promise<TourGroupDetailsResponse>
  onCreateSelection: (planItemId: string, groupId: string, payload: {
    userId: string
    optionId: string
    quantity: number
    travelerIds: string[]
  }) => Promise<TourGroupDetailsResponse>
  onSubmitSelection: (selectionId: string, payload: { userId: string }) => Promise<TourGroupDetailsResponse>
  onConfirmSelection: (selectionId: string, payload: { organizerUserId: string; reviewNote?: string | null }) => Promise<TourGroupDetailsResponse>
  onRejectSelection: (selectionId: string, payload: { organizerUserId: string; reviewNote: string }) => Promise<TourGroupDetailsResponse>
  onBatchConfirmSelections: (payload: {
    organizerUserId: string
    selectionIds: string[]
    reviewNote?: string | null
  }) => Promise<TourGroupDetailsResponse>
  onBatchRejectSelections: (payload: {
    organizerUserId: string
    selectionIds: string[]
    reviewNote: string
  }) => Promise<TourGroupDetailsResponse>
  onBatchPaySelections: (payload: {
    userId: string
    selectionIds: string[]
    paymentMethod: string
  }) => Promise<{ group: TourGroupDetailsResponse; orders: import('@/lib/mvp-types/index').OrderResponse[] }>
  onSearchFlights: (payload: { departureAirport?: string; arrivalAirport?: string; date?: string }) => Promise<FlightResponse[]>
  onSearchHotels: (payload: { location?: string; checkInDate?: string; checkOutDate?: string }) => Promise<HotelResponse[]>
  onSearchTrains: (payload: { fromStation?: string; toStation?: string; date?: string }) => Promise<TrainResponse[]>
  onSearchAttractions: (payload: { city?: string }) => Promise<AttractionResponse[]>
  onOpenBookings: () => Promise<void>
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

function syncGroupSummary(groups: TourGroupSummaryResponse[], details: TourGroupDetailsResponse): TourGroupSummaryResponse[] {
  const nextGroups = groups.filter(group => group.groupId !== details.group.groupId)
  return [details.group, ...nextGroups].sort((left, right) => right.createdAt.localeCompare(left.createdAt))
}

export function TourGroupsPanel({
  currentLanguage,
  isBusy,
  signedInUser,
  travelers,
  translate,
  onListGroups,
  onLoadGroupDetails,
  onCreateGroup,
  onJoinGroup,
  onAddMembershipTraveler,
  onCreatePlanItem,
  onCreatePlanOption,
  onCreateSelection,
  onSubmitSelection,
  onConfirmSelection,
  onRejectSelection,
  onBatchConfirmSelections,
  onBatchRejectSelections,
  onBatchPaySelections,
  onSearchFlights,
  onSearchHotels,
  onSearchTrains,
  onSearchAttractions,
  onOpenBookings,
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
}: TourGroupsPanelProps) {
  const [groupSummaries, setGroupSummaries] = useState<TourGroupSummaryResponse[]>([])
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null)
  const [selectedGroupDetails, setSelectedGroupDetails] = useState<TourGroupDetailsResponse | null>(null)
  const [isCreateDialogOpen, setIsCreateDialogOpen] = useState(false)
  const [activeOrganizerPlanItem, setActiveOrganizerPlanItem] = useState<GroupPlanItemResponse | null>(null)
  const [selectionPlanItem, setSelectionPlanItem] = useState<GroupPlanItemResponse | null>(null)

  useEffect(() => {
    void (async () => {
      const groups = await onListGroups()
      setGroupSummaries(groups)
      if (groups.length > 0) {
        setSelectedGroupId(currentGroupId => currentGroupId ?? groups[0].groupId)
      }
    })()
  }, [onListGroups])

  useEffect(() => {
    if (!selectedGroupId) {
      setSelectedGroupDetails(null)
      setActiveOrganizerPlanItem(null)
      return
    }

    void (async () => {
      const details = await onLoadGroupDetails(selectedGroupId)
      applyUpdatedGroupDetails(details)
    })()
  }, [onLoadGroupDetails, selectedGroupId])

  const activeMembership = useMemo(() => {
    if (!selectedGroupDetails || !signedInUser) {
      return null
    }
    return (
      selectedGroupDetails.memberships.find(
        membership => membership.userId === signedInUser.userId && membership.status === 'Active',
      ) ?? null
    )
  }, [selectedGroupDetails, signedInUser])

  const membershipTravelerIds = new Set(
    activeMembership
      ? selectedGroupDetails?.membershipTravelers
          .filter(row => row.membershipId === activeMembership.membershipId && row.status === 'Active')
          .map(row => row.travelerId) ?? []
      : [],
  )

  const selectionDialogOptions =
    selectionPlanItem && selectedGroupDetails
      ? selectedGroupDetails.planOptions.filter(option => option.planItemId === selectionPlanItem.planItemId)
      : []
  const selectionDialogTravelers = travelers.filter(traveler => membershipTravelerIds.has(traveler.travelerId))

  function applyUpdatedGroupDetails(details: TourGroupDetailsResponse) {
    setSelectedGroupDetails(details)
    setSelectedGroupId(details.group.groupId)
    setGroupSummaries(currentGroups => syncGroupSummary(currentGroups, details))
    setActiveOrganizerPlanItem(currentPlanItem => {
      if (currentPlanItem) {
        return details.planItems.find(planItem => planItem.planItemId === currentPlanItem.planItemId) ?? currentPlanItem
      }
      return [...details.planItems].sort((left, right) => left.sequenceNo - right.sequenceNo)[0] ?? null
    })
  }

  async function createSelectionAndOptionallySubmit(
    planItemId: string,
    payload: { optionId: string; quantity: number; travelerIds: string[] },
    submitAfterCreate: boolean,
  ) {
    if (!selectedGroupDetails || !signedInUser) {
      return
    }

    const createdDetails = await onCreateSelection(planItemId, selectedGroupDetails.group.groupId, {
      userId: signedInUser.userId,
      optionId: payload.optionId,
      quantity: payload.quantity,
      travelerIds: payload.travelerIds,
    })

    let nextDetails = createdDetails

    if (submitAfterCreate) {
      const membershipId = activeMembership?.membershipId
      const newestSelection = [...createdDetails.selections]
        .filter(selection => selection.planItemId === planItemId && selection.optionId === payload.optionId && selection.membershipId === membershipId)
        .sort((left, right) => right.createdAt.localeCompare(left.createdAt))[0]

      if (newestSelection) {
        nextDetails = await onSubmitSelection(newestSelection.selectionId, { userId: signedInUser.userId })
      }
    }

    applyUpdatedGroupDetails(nextDetails)
    setSelectionPlanItem(null)
  }

  return (
    <section className="page-card">
      <div className="panel-heading">
        <div>
          <p className="eyebrow-label">{translate('nav.tourGroups')}</p>
          <h2>{translate('tourGroups.title')}</h2>
        </div>
        <button
          type="button"
          className="secondary-button"
          disabled={isBusy}
          onClick={async () => {
            const groups = await onListGroups()
            setGroupSummaries(groups)
            if (selectedGroupId) {
              const details = await onLoadGroupDetails(selectedGroupId)
              applyUpdatedGroupDetails(details)
            }
          }}
        >
          {translate('tourGroups.refresh')}
        </button>
      </div>

      <p className="hero-copy">{translate('tourGroups.description')}</p>

      <div className="tour-group-layout">
        <TourGroupList
          currentLanguage={currentLanguage}
          groups={groupSummaries}
          selectedGroupId={selectedGroupId}
          signedInUser={signedInUser}
          isBusy={isBusy}
          translate={translate}
          onOpenCreateDialog={() => setIsCreateDialogOpen(true)}
          onSelectGroup={setSelectedGroupId}
        />

        <div className="tour-group-detail-column">
          {selectedGroupDetails ? (
            <TourGroupDetail
              currentLanguage={currentLanguage}
              details={selectedGroupDetails}
              signedInUser={signedInUser}
              travelers={travelers}
              isBusy={isBusy}
              translate={translate}
              onJoinGroup={async () => {
                if (!signedInUser) return
                const details = await onJoinGroup(selectedGroupDetails.group.groupId, { userId: signedInUser.userId })
                applyUpdatedGroupDetails(details)
              }}
              onAddMembershipTraveler={async travelerId => {
                if (!signedInUser) return
                const details = await onAddMembershipTraveler(selectedGroupDetails.group.groupId, {
                  userId: signedInUser.userId,
                  travelerId,
                })
                applyUpdatedGroupDetails(details)
              }}
              onCreatePlanItem={async payload => {
                if (!signedInUser) return null
                const previousPlanItemIds = new Set(selectedGroupDetails.planItems.map(planItem => planItem.planItemId))
                const details = await onCreatePlanItem(selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  ...payload,
                })
                applyUpdatedGroupDetails(details)
                const createdPlanItem =
                  details.planItems
                    .filter(planItem => !previousPlanItemIds.has(planItem.planItemId))
                    .sort((left, right) => left.sequenceNo - right.sequenceNo)
                    .at(-1) ?? null
                if (createdPlanItem) {
                  setActiveOrganizerPlanItem(createdPlanItem)
                }
                return createdPlanItem
              }}
              activePlanItem={activeOrganizerPlanItem}
              onSelectPlanItem={setActiveOrganizerPlanItem}
              onCreateOption={async (planItemId, payload) => {
                if (!signedInUser || !selectedGroupDetails) return
                const details = await onCreatePlanOption(planItemId, selectedGroupDetails.group.groupId, {
                  organizerUserId: signedInUser.userId,
                  ...payload,
                })
                applyUpdatedGroupDetails(details)
              }}
              onOpenChoose={setSelectionPlanItem}
              onSubmitSelection={async selectionId => {
                if (!signedInUser) return
                const details = await onSubmitSelection(selectionId, { userId: signedInUser.userId })
                applyUpdatedGroupDetails(details)
              }}
              onConfirmSelection={async (selectionId, note) => {
                if (!signedInUser) return
                const details = await onConfirmSelection(selectionId, {
                  organizerUserId: signedInUser.userId,
                  reviewNote: note.trim() || null,
                })
                applyUpdatedGroupDetails(details)
              }}
              onRejectSelection={async (selectionId, note) => {
                if (!signedInUser) return
                const details = await onRejectSelection(selectionId, {
                  organizerUserId: signedInUser.userId,
                  reviewNote: note,
                })
                applyUpdatedGroupDetails(details)
              }}
              onBatchConfirmSelections={async selectionIds => {
                if (!signedInUser || selectionIds.length === 0) return
                const details = await onBatchConfirmSelections({
                  organizerUserId: signedInUser.userId,
                  selectionIds,
                  reviewNote: null,
                })
                applyUpdatedGroupDetails(details)
              }}
              onBatchRejectSelections={async (selectionIds, note) => {
                if (!signedInUser || selectionIds.length === 0) return
                const details = await onBatchRejectSelections({
                  organizerUserId: signedInUser.userId,
                  selectionIds,
                  reviewNote: note,
                })
                applyUpdatedGroupDetails(details)
              }}
              onOpenBookings={async () => {
                await onOpenBookings()
              }}
              onBatchPaySelections={async selectionIds => {
                if (!signedInUser || selectionIds.length === 0) return
                const result = await onBatchPaySelections({
                  userId: signedInUser.userId,
                  selectionIds,
                  paymentMethod: 'Wallet',
                })
                applyUpdatedGroupDetails(result.group)
              }}
              onSearchFlights={onSearchFlights}
              onSearchHotels={onSearchHotels}
              onSearchTrains={onSearchTrains}
              onSearchAttractions={onSearchAttractions}
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
          ) : (
            <section className="list-surface">
              <p className="empty-state">{translate('tourGroups.selectGroupHint')}</p>
            </section>
          )}
        </div>
      </div>

      <CreateTourGroupDialog
        isOpen={isCreateDialogOpen}
        isBusy={isBusy}
        translate={translate}
        onClose={() => setIsCreateDialogOpen(false)}
        onCreateGroup={async payload => {
          if (!signedInUser) return
          const details = await onCreateGroup({
            organizerUserId: signedInUser.userId,
            ...payload,
          })
          applyUpdatedGroupDetails(details)
          setIsCreateDialogOpen(false)
        }}
      />

      <TourGroupSelectionDialog
        currentLanguage={currentLanguage}
        isOpen={selectionPlanItem !== null}
        isBusy={isBusy}
        planItem={selectionPlanItem}
        options={selectionDialogOptions}
        availableTravelers={selectionDialogTravelers}
        translate={translate}
        onClose={() => setSelectionPlanItem(null)}
        onSaveDraft={async payload => {
          if (!selectionPlanItem) return
          await createSelectionAndOptionallySubmit(selectionPlanItem.planItemId, payload, false)
        }}
        onSaveAndSubmit={async payload => {
          if (!selectionPlanItem) return
          await createSelectionAndOptionallySubmit(selectionPlanItem.planItemId, payload, true)
        }}
      />
    </section>
  )
}
